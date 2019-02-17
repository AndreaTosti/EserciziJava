package Turing;

import com.sun.org.apache.bcel.internal.generic.Select;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Server
{
  private static int DEFAULT_PORT = 51811; //Porta di Default
  private static int DEFAULT_RMI_PORT = 51812; //Porta RMI
  private static int BUFFER_SIZE  = 4096; //Dimensione del buffer
  private static String DEFAULT_DELIMITER = "#";
  private static String DEFAULT_PARENT_FOLDER = "518111_ServerDirs";


  //Utenti
  private static final ConcurrentMap<String, Utente> users = new ConcurrentHashMap<>();

  //Sessione utente
  private static final Map<SocketChannel, Sessione> sessions = new HashMap<>();

  //Documenti
  private static final Map<String, Documento> documents = new HashMap<>();


  private static void println(Object o)
  {
    System.out.println("[Server] " + o);
  }

  private static void printErr(Object o)
  {
    System.err.println("[Server-Error] " + o);
  }


  private static Op handleClosedConnection(SocketChannel channel)
  {
    Op returnValue;
    if(sessions.remove(channel) == null)
    {
      returnValue = Op.UnknownSession;
    }
    else
    {
      returnValue = Op.SuccessfullyRemovedSession;
    }
    try
    {
      channel.close();
    }
    catch(IOException e)
    {
      e.printStackTrace();
      return returnValue;
    }

    return returnValue;
  }

  private static Op handleLogin(String[] splitted, SocketChannel channel)
  {
    String nickname = splitted[1];
    String password = splitted[2];

    Sessione sessione = sessions.get(channel);

    if(sessione == null)
      return Op.UnknownSession;

    if(sessione.getStato() == Sessione.Stato.Logged ||
       sessione.getStato() == Sessione.Stato.Editing)
      return Op.AlreadyLoggedIn;

    Utente utente = users.get(nickname);
    if(utente == null)
      return Op.UserDoesNotExists;

    if(utente.getPassword().compareTo(password) != 0)
      return Op.WrongPassword;

    sessione.setUtente(utente);
    sessione.setStato(Sessione.Stato.Logged);

    return Op.SuccessfullyLoggedIn;
  }

  private static Op handleLogout(SocketChannel channel)
  {
    Sessione sessione = sessions.get(channel);
    if(sessione == null)
      return Op.UnknownSession;

    if(sessione.getStato() == Sessione.Stato.Started ||
       sessione.getStato() == Sessione.Stato.Editing)
      return Op.CannotLogout;

    sessione.setStato(Sessione.Stato.Started);

    return Op.SuccessfullyLoggedOut;
  }

  private static Op handleCreate(String[] splitted, SocketChannel channel)
  {
    String nomeDocumento = splitted[1];

    if(nomeDocumento == null)
      return Op.UsageError;

    int numSezioni;
    try
    {
      numSezioni = Integer.parseInt(splitted[2]);
    }
    catch(NumberFormatException e)
    {
      return Op.UsageError;
    }

    Sessione sessione = sessions.get(channel);

    if(sessione == null)
      return Op.UnknownSession;

    if(sessione.getStato() == Sessione.Stato.Started ||
       sessione.getStato() == Sessione.Stato.Editing)
      return Op.MustBeInLoggedState;

    Utente utente = sessione.getUtente();

    if(utente == null)
      return Op.UserDoesNotExists;

    Sezione[] newSezioni = new Sezione[numSezioni];
    for(int i = 0; i < numSezioni; i++)
    {
      newSezioni[i] = new Sezione(nomeDocumento + "_" + i, nomeDocumento, i);
    }
    Documento documento = new Documento(nomeDocumento, numSezioni, utente, newSezioni);

    if(documents.putIfAbsent(nomeDocumento, documento) != null)
      return Op.DocumentAlreadyExists;

    //Creo una cartella avente come nome il nome del documento
    Path directoryPath = Paths.get(DEFAULT_PARENT_FOLDER + File.separator + nomeDocumento);
    if(Files.exists(directoryPath))
      return Op.DirectoryAlreadyExists;

    try
    {
      Files.createDirectories(directoryPath);
      Sezione[] sezioni = documento.getSezioni();
      for(Sezione sezione: sezioni)
      {
        Path filePath = Paths.get(DEFAULT_PARENT_FOLDER + File.separator +
                nomeDocumento + File.separator + sezione.getNomeSezione() + ".txt");
        Files.createFile(filePath);
      }
    }
    catch(IOException e)
    {
      e.printStackTrace();
      return Op.Error;
    }

    return Op.SuccessfullyCreated;
  }

  private static Op handleShare(String[] splitted, SocketChannel channel)
  {
    String nomeDocumento = splitted[1];
    String nickname = splitted[2];

    Sessione sessione = sessions.get(channel);

    if(sessione == null)
      return Op.UnknownSession;

    if(sessione.getStato() == Sessione.Stato.Started ||
       sessione.getStato() == Sessione.Stato.Editing)
      return Op.MustBeInLoggedState;

    Utente utente = sessione.getUtente();

    if(utente == null)
      return Op.UserDoesNotExists;

    Documento documento = documents.get(nomeDocumento);
    if(documento == null)
      return Op.DocumentDoesNotExists;

    if(!documento.getCreatore().equals(utente))
      return Op.NotDocumentCreator;

    Utente utenteCollaboratore = users.get(nickname);

    if(utenteCollaboratore == null)
      return Op.UserDoesNotExists;

    if(documento.getCreatore().equals(utenteCollaboratore))
      return Op.CreatorCannotBeCollaborator;

    if(documento.isCollaboratore(utenteCollaboratore))
      return Op.AlreadyCollaborates;

    documento.addCollaboratore(utenteCollaboratore);
    return Op.SuccessfullyShared;
  }

  private static Op handleShow(String[] splitted, SocketChannel channel)
  {
    String nomeDocumento = splitted[1];

    int numSezione = -1;
    if(splitted.length == 3)
    {
      try
      {
        numSezione = Integer.parseInt(splitted[2]);
      }
      catch(NumberFormatException e)
      {
        return Op.UsageError;
      }
    }

    Sessione sessione = sessions.get(channel);

    if(sessione == null)
      return Op.UnknownSession;

    if(sessione.getStato() == Sessione.Stato.Started ||
       sessione.getStato() == Sessione.Stato.Editing)
      return Op.MustBeInLoggedState;

    Utente utente = sessione.getUtente();

    if(utente == null)
      return Op.UserDoesNotExists;

    Documento documento = documents.get(nomeDocumento);
    if(documento == null)
      return Op.DocumentDoesNotExists;

    if(!documento.getCreatore().equals(utente) &&
       !documento.isCollaboratore(utente))
      return Op.NotDocumentCreatorNorCollaborator;

    if(numSezione != -1)
    {
      printErr("LENGTH: " + documento.getSezioni().length + " NUM SEZIONE: " + numSezione);
      if(documento.getSezioni().length <= numSezione)
        return Op.SectionDoesNotExists;
    }

    return Op.SuccessfullyShown;
  }


  //Metodo costruttore
  public static void main(String[] args)
  {
    //Porta su cui il server si mette in ascolto
    int serverPort;

    if(args.length == 0)
    {
      //Non ho passato il numero di porta
      serverPort = DEFAULT_PORT;
      println("No arguments specified, using default " +
              "port number " + DEFAULT_PORT);
    }
    else if(args.length == 1)
    {
      //Ho passato il numero di porta
      serverPort = Integer.parseInt(args[0]);
      println("Listening to chosen port number " + serverPort);
    }
    else
    {
      printErr("Only one argument accepted : port number");
      return;
    }

    try
    {
      //Registrazione RMI
      RegUtenteImplementation object = new RegUtenteImplementation(users);
      RegUtenteInterface stub =
              (RegUtenteInterface) UnicastRemoteObject.exportObject(object, 0);
      Registry registry = LocateRegistry.createRegistry(DEFAULT_RMI_PORT);
      registry.bind("RegUtente", stub);
    }
    catch(ExportException e1)
    {
      printErr(e1.toString());
      System.exit(1);
    }
    catch(Exception e2)
    {
      printErr("exception: " + e2.toString());
      e2.printStackTrace();
      System.exit(1);
    }

    ServerSocketChannel serverChannel;
    Selector selector = null;
    try
    {
      serverChannel = ServerSocketChannel.open();
      ServerSocket ss = serverChannel.socket();
      InetSocketAddress address = new InetSocketAddress(serverPort);
      ss.bind(address);
      serverChannel.configureBlocking(false);
      selector = Selector.open();
      serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }

    Op OP = null;

    while(true)
    {
      try
      {
        assert selector != null;
        selector.select();
      }
      catch(IOException | NullPointerException e)
      {
        e.printStackTrace();
      }
      Set<SelectionKey> readyKeys = selector.selectedKeys();
      Iterator<SelectionKey> iterator = readyKeys.iterator();
      while(iterator.hasNext())
      {
        SelectionKey key = iterator.next();
        iterator.remove();
        try
        {
          if(key.isValid() && key.isAcceptable())
          {
            //Nuova richiesta di connessione
            println("key.isAcceptable");

            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            SocketChannel client = server.accept();
            println("New connection from client " + client.getRemoteAddress());
            client.configureBlocking(false);

            ArrayList<Object> attachments = new ArrayList<>();
            //Bisogna leggere Long.BYTES che è la dimensione del messaggio
            attachments.add(0, Long.BYTES); //Remaining Bytes
            attachments.add(1, ByteBuffer.allocate(Long.BYTES));
            attachments.add(2, Step.WaitingForMessageSize);
            attachments.add(3, Long.BYTES); //Total Size
            attachments.add(4, null);  //Parametri

            client.register(selector, SelectionKey.OP_READ, attachments);

            //Crea una nuova sessione per il client
            sessions.put(client, new Sessione());

          }
          if(key.isValid() && key.isReadable())
          {
            //Nuovo evento in lettura
            println("key.isReadable ");
            SocketChannel channel = (SocketChannel)key.channel();

            ArrayList<Object> attachments = (ArrayList) key.attachment();
            int remainingBytes = (int) attachments.get(0);
            ByteBuffer buffer = (ByteBuffer) attachments.get(1);
            Step step = (Step) attachments.get(2);
            int totalSize = (int) attachments.get(3);
            String[] parameters = (String[]) attachments.get(4);

            int res;

            switch(step)
            {
              case WaitingForMessageSize :
                res = channel.read(buffer);
                printErr("Read " + res + " bytes");

                if(res < 0)
                {
                  handleClosedConnection(channel);
                }
                else
                {
                  if(res < remainingBytes)
                  {
                    //Non abbiamo ancora la dimensione del messaggio
                    remainingBytes -= res;
                    attachments.set(0, remainingBytes);
                    println("res: " + res);
                    continue;
                  }
                  else
                  {
                    //Abbiamo la dimensione del messaggio nel buffer
                    //Ora bisogna scaricare il messaggio
                    buffer.flip();
                    int messageSize = Integer.valueOf(new String(buffer.array(),
                            0, Long.BYTES, StandardCharsets.ISO_8859_1));
                    println("Message size: " + messageSize);
                    attachments.set(0, messageSize); //Remaining Size
                    attachments.set(1, ByteBuffer.allocate(messageSize));
                    attachments.set(2, Step.WaitingForMessage);
                    attachments.set(3, messageSize); //Total Size
                    attachments.set(4, null); //Parametri
                  }
                }
                break;

              case WaitingForMessage :
                try
                {
                  res = channel.read(buffer);

                  if(res < 0)
                  {
                    handleClosedConnection(channel);
                  }
                  else
                  {
                    println("Read " + res + " bytes");
                    if(res < remainingBytes)
                    {
                      //Non abbiamo ancora ricevuto il messaggio per intero
                      remainingBytes -= res;
                      attachments.set(0, remainingBytes);
                      println("res: " + res);
                      continue;
                    }
                    else
                    {
                      //Abbiamo il messaggio
                      buffer.flip();
                      String toSplit = new String(buffer.array(), 0, totalSize, StandardCharsets.ISO_8859_1);
                      String[] splitted = toSplit.split(DEFAULT_DELIMITER);
                      Op requestedOperation = Op.valueOf(splitted[0]);
                      println("Requested Operation : " + requestedOperation.toString() +
                              " from Client IP : " + channel.socket().getInetAddress().getHostAddress() +
                              " port : " + channel.socket().getPort());
                      Op result;
                      printErr(toSplit);
                      switch(requestedOperation)
                      {
                        case Login:
                          result = handleLogin(splitted, channel);
                          break;

                        case Logout:
                          result = handleLogout(channel);
                          break;

                        case Create:
                          result = handleCreate(splitted, channel);
                          break;

                        case Share:
                          result = handleShare(splitted, channel);
                          break;

                        case Show:
                          result = handleShow(splitted, channel);
                          break;

                        default:
                          result = Op.UsageError;
                          break;
                      }

                      println("Result = " + result);
                      byte[] resultBytes = result.toString().getBytes(StandardCharsets.ISO_8859_1);
                      String numBytesStr = String.format("%0" + Long.BYTES + "d", resultBytes.length);
                      byte[] numBytes = numBytesStr.getBytes();
                      buffer = ByteBuffer.wrap(numBytes);
                      attachments.set(0, buffer.array().length);
                      attachments.set(1, buffer);
                      attachments.set(2, Step.SendingOutcomeSize);
                      attachments.set(3, buffer.array().length);

                      String[] newSplitted = new String[splitted.length + 1];
                      System.arraycopy(splitted, 0, newSplitted,
                              0, splitted.length);
                      newSplitted[splitted.length] = result.toString();
                      attachments.set(4, newSplitted); //Parametri

                      channel.register(selector, SelectionKey.OP_WRITE, attachments);

                    }
                  }
                }
                catch(IOException e)
                {
                  e.printStackTrace();
                }


                break;
              default :
                println("nessuna delle precedenti");
                break;
            }
          }
          if(key.isValid() && key.isWritable())
          {
            //Nuovo evento in scrittura
            println("key.isWritable ");

            SocketChannel channel = (SocketChannel)key.channel();
            ArrayList<Object> attachments = (ArrayList) key.attachment();
            int remainingBytes = (int) attachments.get(0);
            ByteBuffer buffer = (ByteBuffer) attachments.get(1);
            Step step = (Step) attachments.get(2);
            int totalSize = (int) attachments.get(3);

            int res;
            LinkedList<Sezione> sezioni;

            switch(step)
            {
              case SendingOutcomeSize :
                res = channel.write(buffer);

                println("SendingOutcomeSize Written " + res + " bytes");
                if(res < remainingBytes)
                {
                  //Non abbiamo finito di inviare la dimensione dell'esito
                  remainingBytes -= res;
                  attachments.set(0, remainingBytes);
                  println("res: " + res);
                  continue;
                }
                else
                {
                  //Abbiamo inviato la dimensione dell'esito
                  //Invio l'esito
                  String[] parameters = (String[]) attachments.get(4);
                  Op result = Op.valueOf(parameters[parameters.length - 1]);

                  buffer = ByteBuffer.wrap(result.toString().getBytes());
                  attachments.set(0, buffer.array().length);
                  attachments.set(1, buffer);
                  attachments.set(2, Step.SendingOutcome);
                  attachments.set(3, buffer.array().length);
                  attachments.set(4, parameters);

                  channel.register(selector, SelectionKey.OP_WRITE, attachments);
                }
                break;

              case SendingOutcome :
                res = channel.write(buffer);

                println("SendingOutcome Written " + res + " bytes");
                if(res < remainingBytes)
                {
                  //Non abbiamo finito ad inviare l'esito
                  remainingBytes -= res;
                  attachments.set(0, remainingBytes);
                  println("res: " + res);
                  continue;
                }
                else
                {
                  // Abbiamo inviato l'esito
                  String[] parameters = (String[]) attachments.get(4);

                  Op requestedOperation = Op.valueOf(parameters[0]);
                  Op result = Op.valueOf(parameters[parameters.length - 1]);

                  if(requestedOperation == Op.Show && result == Op.SuccessfullyShown)
                  {
                    //Devo inviare il numero di sezioni
                    String nomeDocumento = parameters[1];
                    Documento documento = documents.get(nomeDocumento);

                    int numSezioni;

                    if(parameters.length == 3 + 1)
                    {
                      //singola sezione
                      int numSezione = Integer.parseInt(parameters[2]);
                      Sezione sezione = documento.getSezioni()[numSezione];
                      LinkedList<Sezione> sezioni_ = new LinkedList<>();
                      sezioni_.add(sezione);
                      attachments.set(4, sezioni_); //Parametri
                      numSezioni = 1;
                    }
                    else
                    {
                      //tutte le sezioni
                      Sezione[] arraySezioni = documento.getSezioni();
                      LinkedList<Sezione> sezioni_ = new LinkedList<>(Arrays.asList(arraySezioni));
                      numSezioni = sezioni_.size();
                      attachments.set(4, sezioni_); //Parametri
                    }

                    String numBytesStr = String.format("%0" + Long.BYTES + "d", numSezioni);
                    byte[] numBytes = numBytesStr.getBytes();
                    buffer = ByteBuffer.wrap(numBytes);
                    attachments.set(0, buffer.array().length);
                    attachments.set(1, buffer);
                    attachments.set(2, Step.SendingNumberOfSections);
                    attachments.set(3, buffer.array().length);

                    channel.register(selector, SelectionKey.OP_WRITE, attachments);
                  }
                  else
                  {
                    // torno nello stato WaitingForMessageSize
                    attachments.set(0, Long.BYTES); //Remaining Bytes
                    attachments.set(1, ByteBuffer.allocate(Long.BYTES));
                    attachments.set(2, Step.WaitingForMessageSize);
                    attachments.set(3, Long.BYTES); //Total Size
                    attachments.set(4, null); //Parametri
                    channel.register(selector, SelectionKey.OP_READ, attachments);
                  }
                }
                break;

              case SendingNumberOfSections :
                res = channel.write(buffer);
                println("SendingNumberOfSections Written " + res + " bytes");
                if(res < remainingBytes)
                {
                  //Non abbiamo finito di mandare il numero di sezioni
                  remainingBytes -= res;
                  attachments.set(0, remainingBytes);
                  println("res: " + res);
                  continue;
                }
                else
                {
                  //Abbiamo inviato il numero di sezioni
                  //Prelevo una sezione dalla lista e
                  //invio la dimensione di questa
                  sezioni = (LinkedList<Sezione>) attachments.get(4);
                  Sezione sezione = sezioni.element();
                  assert(sezione != null);
                  Path filePath = Paths.get(DEFAULT_PARENT_FOLDER + File.separator +
                          sezione.getNomeDocumento() + File.separator +
                          sezione.getNomeSezione() + ".txt");
                  try
                  {
                    if(Files.notExists(filePath))
                    {
                      //FIXME: la sezione non esiste nella directory
                      //       creo un file vuoto e lo invio lo stesso
                      printErr("Filename " + sezione.getNomeSezione() + ".txt" +
                              " does not exists in the current working directory: " +
                              System.getProperty("user.dir"));
                      //Creo un file vuoto
                      Files.createFile(filePath);
                    }
                    FileChannel fileChannel = FileChannel.open(filePath);
                    long dimensioneFile = fileChannel.size();
                    String numBytesStr = String.format("%0" + Long.BYTES + "d", dimensioneFile);
                    byte[] numBytes = numBytesStr.getBytes();
                    buffer = ByteBuffer.wrap(numBytes);
                    attachments.set(0, buffer.array().length);
                    attachments.set(1, buffer);
                    attachments.set(2, Step.SendingSectionSize);
                    attachments.set(3, buffer.array().length);
                    attachments.set(4, sezioni); //Parametri
                    channel.register(selector, SelectionKey.OP_WRITE, attachments);
                    fileChannel.close();
                  }
                  catch(IOException e)
                  {
                    e.printStackTrace();
                  }
                }
                break;

              case SendingSectionSize :
                res = channel.write(buffer);
                println("SendingSectionSize Written " + res + " bytes");
                if(res < remainingBytes)
                {
                  //Non abbiamo finito di mandare la dimensione della sezione
                  remainingBytes -= res;
                  attachments.set(0, remainingBytes);
                  println("res: " + res);
                  continue;
                }
                else
                {
                  //Abbiamo inviato la dimensione della sezione
                  //Invio il numero identificativo della sezione
                  sezioni = (LinkedList<Sezione>) attachments.get(4);

                  Sezione sezione = sezioni.element();
                  Path filePath = Paths.get(DEFAULT_PARENT_FOLDER + File.separator +
                          sezione.getNomeDocumento() + File.separator +
                          sezione.getNomeSezione() + ".txt");
                  try
                  {
                    FileChannel fileChannel = FileChannel.open(filePath);
                    long numeroSezione = sezione.getNumeroSezione();
                    println("INVIO NUMERO SEZIONE " + numeroSezione);
                    String numBytesStr = String.format("%0" + Long.BYTES + "d", numeroSezione);
                    byte[] numBytes = numBytesStr.getBytes();
                    buffer = ByteBuffer.wrap(numBytes);
                    attachments.set(0, buffer.array().length);
                    attachments.set(1, buffer);
                    attachments.set(2, Step.SendingSectionNumber);
                    attachments.set(3, buffer.array().length);
                    attachments.set(4, sezioni); //Parametri
                    channel.register(selector, SelectionKey.OP_WRITE, attachments);
                    fileChannel.close();
                  }
                  catch(IOException e)
                  {
                    e.printStackTrace();
                  }
                }
                break;

              case SendingSectionNumber :
                res = channel.write(buffer);
                println("SendingSectionNumber Written " + res + " bytes");
                if(res < remainingBytes)
                {
                  //Non abbiamo finito di mandare il numero identificativo di sezione
                  remainingBytes -= res;
                  attachments.set(0, remainingBytes);
                  println("res: " + res);
                  continue;
                }
                else
                {
                  //Abbiamo inviato il numero identificativo di sezione
                  //Invio la sezione
                  sezioni = (LinkedList<Sezione>) attachments.get(4);
                  Sezione sezione = sezioni.element();
                  assert(sezione != null);
                  Path filePath = Paths.get(DEFAULT_PARENT_FOLDER + File.separator +
                          sezione.getNomeDocumento() + File.separator +
                          sezione.getNomeSezione() + ".txt");
                  try
                  {
                    FileChannel fileChannel = FileChannel.open(filePath);
                    long dimensioneFile = fileChannel.size();
                    //Decido di bufferizzare l'intero file
                    buffer = ByteBuffer.allocate(Math.toIntExact(dimensioneFile));
                    //Il buffer è sufficientemente grande per contenere l'intero file
                    println("LETTI DAL FILE " + fileChannel.read(buffer) + " bytes");
                    buffer.flip();
                    attachments.set(0, buffer.array().length);
                    attachments.set(1, buffer);
                    attachments.set(2, Step.SendingSection);
                    attachments.set(3, buffer.array().length);
                    attachments.set(4, sezioni); //Parametri
                    channel.register(selector, SelectionKey.OP_WRITE, attachments);
                    fileChannel.close();
                  }
                  catch(IOException e)
                  {
                    e.printStackTrace();
                  }
                }

                break;
              case SendingSection :
                res = channel.write(buffer);
                println("SendingSection Written " + res + " bytes");
                if(res < remainingBytes)
                {
                  //Non abbiamo finito di mandare la sezione
                  remainingBytes -= res;
                  attachments.set(0, remainingBytes);
                  println("res: " + res);
                  continue;
                }
                else
                {
                  //Abbiamo inviato l'intera sezione
                  //Vedo se bisogna o meno inviare un'altra sezione
                  sezioni = (LinkedList<Sezione>) attachments.get(4);
                  //Rimuovo la sezione perchè è stata appena inviata SOPRA
                  printErr("SIZE BEFORE: " + sezioni.size());
                  sezioni.remove();
                  printErr("SIZE AFTER: " + sezioni.size());
                  if(sezioni.size() > 0)
                  {
                    //Bisogna inviare un'altra sezione
                    Sezione sezione = sezioni.element();
                    //Prelevo una sezione dalla lista e
                    //invio la dimensione di questa
                    Path filePath = Paths.get(DEFAULT_PARENT_FOLDER + File.separator +
                            sezione.getNomeDocumento() + File.separator +
                            sezione.getNomeSezione() + ".txt");
                    try
                    {
                      if(Files.notExists(filePath))
                      {
                        //FIXME: la sezione non esiste nella directory
                        //       creo un file vuoto e lo invio lo stesso
                        printErr("Filename " + sezione.getNomeSezione() + ".txt" +
                                " does not exists in the current working directory: " +
                                System.getProperty("user.dir"));
                        //Creo un file vuoto
                        Files.createFile(filePath);
                      }
                      FileChannel fileChannel = FileChannel.open(filePath);
                      long dimensioneFile = fileChannel.size();
                      String numBytesStr = String.format("%0" + Long.BYTES + "d", dimensioneFile);
                      byte[] numBytes = numBytesStr.getBytes();
                      buffer = ByteBuffer.wrap(numBytes);
                      attachments.set(0, buffer.array().length);
                      attachments.set(1, buffer);
                      attachments.set(2, Step.SendingSectionSize);
                      attachments.set(3, buffer.array().length);
                      attachments.set(4, sezioni); //Parametri
                      channel.register(selector, SelectionKey.OP_WRITE, attachments);
                      fileChannel.close();
                    }
                    catch(IOException e)
                    {
                      e.printStackTrace();
                    }
                  }
                  else
                  {
                    // ho finito di inviare sezioni,
                    // torno nello stato WaitingForMessageSize
                    attachments.set(0, Long.BYTES); //Remaining Bytes
                    attachments.set(1, ByteBuffer.allocate(Long.BYTES));
                    attachments.set(2, Step.WaitingForMessageSize);
                    attachments.set(3, Long.BYTES); //Total Size
                    attachments.set(4, null); //Parametri
                    channel.register(selector, SelectionKey.OP_READ, attachments);
                  }
                }
                break;
              default :
                println("nessuna delle precedenti(write)");
                break;
            }

          }
        }
        catch(IOException e)
        {
          key.cancel();
          try
          {
            key.channel().close();
          }
          catch(IOException ex)
          {
            ex.printStackTrace();
          }
        }
      }
    }

  }

}
