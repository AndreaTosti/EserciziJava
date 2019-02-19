package Turing;

import javax.print.Doc;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.IntStream;

public class Server
{
  private static int DEFAULT_PORT = 51811; //Porta di Default
  private static int DEFAULT_RMI_PORT = 51812; //Porta RMI
  private static int BUFFER_SIZE  = 4096; //Dimensione del buffer
  private static String DEFAULT_DELIMITER = "#";
  private static String DEFAULT_INTERIOR_DELIMITER = ":";
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

  private static Op handleList(SocketChannel channel)
  {
    Sessione sessione = sessions.get(channel);
    if(sessione == null)
      return Op.UnknownSession;

    if(sessione.getStato() == Sessione.Stato.Started ||
       sessione.getStato() == Sessione.Stato.Editing)
      return Op.MustBeInLoggedState;

    Utente utente = sessione.getUtente();

    if(utente == null)
      return Op.UserDoesNotExists;

    return Op.SuccessfullyListed;
  }

  private static Op handleEdit(String[] splitted, SocketChannel channel)
  {
    String nomeDocumento = splitted[1];
    int numSezione = -1;
    try
    {
      numSezione = Integer.parseInt(splitted[2]);
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

    Documento documento = documents.get(nomeDocumento);
    if(documento == null)
      return Op.DocumentDoesNotExists;

    if(!documento.getCreatore().equals(utente) &&
       !documento.isCollaboratore((utente)))
      return Op.NotDocumentCreatorNorCollaborator;


    if(numSezione != -1)
    {
      if(documento.getSezioni().length <= numSezione)
        return Op.SectionDoesNotExists;
    }

    Sezione sezione = documento.getSezioni()[numSezione];
    if(sezione.getUserEditing() != null)
      return Op.SectionUnderModification;

    sezione.edit(utente);
    sessione.setStato(Sessione.Stato.Editing);

    return Op.SuccessfullyStartedEditing;
  }

  private static Op handleEndEdit(String[] splitted, SocketChannel channel)
  {
    String nomeDocumento = splitted[1];
    int numSezione = -1;
    try
    {
      numSezione = Integer.parseInt(splitted[2]);
    }
    catch(NumberFormatException e)
    {
      return Op.Error;
    }

    Sessione sessione = sessions.get(channel);

    if(sessione == null)
      return Op.UnknownSession;

    if(sessione.getStato() == Sessione.Stato.Started ||
       sessione.getStato() == Sessione.Stato.Logged)
      return Op.MustBeInEditingState;

    Utente utente = sessione.getUtente();

    if(utente == null)
      return Op.UserDoesNotExists;

    Documento documento = documents.get(nomeDocumento);
    if(documento == null)
      return Op.DocumentDoesNotExists;

    if(!documento.getCreatore().equals(utente) &&
            !documento.isCollaboratore((utente)))
      return Op.NotDocumentCreatorNorCollaborator;


    if(numSezione != -1)
    {
      if(documento.getSezioni().length <= numSezione)
        return Op.SectionDoesNotExists;
    }

    Sezione sezione = documento.getSezioni()[numSezione];
    if(!sezione.getUserEditing().equals(utente))
      return Op.Error;

    sezione.endEdit();
    sessione.setStato(Sessione.Stato.Logged);

    return Op.SuccessfullyEndedEditing;
  }

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

            Attachment attachments = new Attachment(
                    Long.BYTES,
                    ByteBuffer.allocate(Long.BYTES),
                    Step.WaitingForMessageSize,
                    Long.BYTES,
                    null,
                    null,
                    null
            );

            client.register(selector, SelectionKey.OP_READ, attachments);

            //Crea una nuova sessione per il client
            sessions.put(client, new Sessione());

          }
          if(key.isValid() && key.isReadable())
          {
            //Nuovo evento in lettura
            println("key.isReadable ");
            SocketChannel channel = (SocketChannel)key.channel();

            Attachment attachments = (Attachment) key.attachment();
            int remainingBytes     =  attachments.getRemainingBytes();
            ByteBuffer buffer      =  attachments.getBuffer();
            Step step              =  attachments.getStep();
            int totalSize          =  attachments.getTotalSize();
            String[] parameters    =  attachments.getParameters();
            String list            =  attachments.getList();

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
                    //Non abbiamo ancora tutta la dimensione del messaggio
                    remainingBytes -= res;
                    attachments.setRemainingBytes(remainingBytes);
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
                    attachments.setRemainingBytes(messageSize);
                    attachments.setBuffer(ByteBuffer.allocate(messageSize));
                    attachments.setStep(Step.WaitingForMessage);
                    attachments.setTotalSize(messageSize);
                    attachments.setParameters(null);
                    attachments.setList(null);
                    //FIXME: Aggiunta -> da controllare se funziona
                    channel.register(selector, SelectionKey.OP_READ, attachments);
                  }
                }
                break;

              case WaitingForMessage :
                try
                {
                  res = channel.read(buffer);
                  println("Read " + res + " bytes");

                  if(res < 0)
                  {
                    handleClosedConnection(channel);
                  }
                  else
                  {
                    if(res < remainingBytes)
                    {
                      //Non abbiamo ancora ricevuto il messaggio per intero
                      remainingBytes -= res;
                      attachments.setRemainingBytes(remainingBytes);
                      println("res: " + res);
                      continue;
                    }
                    else
                    {
                      //Abbiamo il messaggio
                      buffer.flip();
                      String toSplit = new String(buffer.array(), 0,
                              totalSize, StandardCharsets.ISO_8859_1);
                      String[] splitted = toSplit.split(DEFAULT_DELIMITER);
                      Op requestedOperation = Op.valueOf(splitted[0]);
                      println("Requested Operation : " +
                              requestedOperation.toString() +
                              " from Client IP : " +
                              channel.socket().getInetAddress().getHostAddress() +
                              " port : " + channel.socket().getPort());
                      Op result;
                      printErr(toSplit);
                      switch(requestedOperation)
                      {
                        case Login :
                          result = handleLogin(splitted, channel);
                          break;

                        case Logout :
                          result = handleLogout(channel);
                          break;

                        case Create :
                          result = handleCreate(splitted, channel);
                          break;

                        case Share :
                          result = handleShare(splitted, channel);
                          break;

                        case Show :
                          result = handleShow(splitted, channel);
                          break;

                        case List :
                          result = handleList(channel);
                          break;

                        case Edit :
                          result = handleEdit(splitted, channel);
                          break;

                        case EndEdit :
                          result = handleEndEdit(splitted, channel);
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

                      String[] newSplitted = new String[splitted.length + 1];
                      System.arraycopy(splitted, 0, newSplitted,
                              0, splitted.length);
                      newSplitted[splitted.length] = result.toString();

                      attachments.setRemainingBytes(buffer.array().length);
                      attachments.setBuffer(buffer);
                      attachments.setStep(Step.SendingOutcomeSize);
                      attachments.setTotalSize(buffer.array().length);
                      attachments.setParameters(newSplitted);

                      channel.register(selector, SelectionKey.OP_WRITE, attachments);

                    }
                  }
                }
                catch(IOException e)
                {
                  e.printStackTrace();
                }

                break;

              case GettingSectionSize :
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
                    //Non abbiamo ancora tutta la dimensione della sezione
                    remainingBytes -= res;
                    attachments.setRemainingBytes(remainingBytes);
                    println("res: " + res);
                    continue;
                  }
                  else
                  {
                    //Abbiamo la dimensione della sezione nel buffer
                    //Ora bisogna scaricare la sezione
                    buffer.flip();
                    int sectionSize = Integer.valueOf(new String(buffer.array(),
                            0, Long.BYTES, StandardCharsets.ISO_8859_1));
                    println("Section size: " + sectionSize);
                    attachments.setRemainingBytes(sectionSize);
                    attachments.setBuffer(ByteBuffer.allocate(sectionSize));
                    attachments.setStep(Step.GettingSection);
                    attachments.setTotalSize(sectionSize);
                    attachments.setParameters(parameters); //Propagare i parametri
                    attachments.setList(null);
                    //FIXME: Aggiunta -> da controllare se funziona
                    channel.register(selector, SelectionKey.OP_READ, attachments);
                  }
                }
                break;

              case GettingSection :
                try
                {
                  res = channel.read(buffer);
                  println("Read " + res + " bytes");

                  if(res < 0)
                  {
                    handleClosedConnection(channel);
                  }
                  else
                  {
                    if(res < remainingBytes)
                    {
                      //Non abbiamo ancora ricevuto la sezione per intero
                      remainingBytes -= res;
                      attachments.setRemainingBytes(remainingBytes);
                      println("res: " + res);
                      continue;
                    }
                    else
                    {
                      //Abbiamo la sezione
                      buffer.flip();
                      String nomeDocumento = parameters[1];
                      Documento documento = documents.get(nomeDocumento);
                      int numSezione = Integer.parseInt(parameters[2]);
                      Sezione sezione = documento.getSezioni()[numSezione];

                      Path filePath = Paths.get(DEFAULT_PARENT_FOLDER + File.separator +
                              sezione.getNomeDocumento() + File.separator +
                              sezione.getNomeSezione() + ".txt");
                      FileChannel fileChannel = FileChannel.open(filePath, EnumSet.of(
                              StandardOpenOption.CREATE,
                              StandardOpenOption.TRUNCATE_EXISTING,
                              StandardOpenOption.WRITE));

                      while(buffer.hasRemaining())
                        fileChannel.write(buffer);
                      //FIXME: Non si sa se sia indispensabile più di una scrittura.

                      //Abbiamo memorizzato la sezione
                      //torno nello stato WaitingForMessageSize
                      attachments.setRemainingBytes(Long.BYTES);
                      attachments.setBuffer(ByteBuffer.allocate(Long.BYTES));
                      attachments.setStep(Step.WaitingForMessageSize);
                      attachments.setTotalSize(Long.BYTES);
                      attachments.setParameters(null);
                      attachments.setSections(null);
                      attachments.setList(null);

                      channel.register(selector, SelectionKey.OP_READ, attachments);
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

            Attachment attachments = (Attachment) key.attachment();
            int remainingBytes     =  attachments.getRemainingBytes();
            ByteBuffer buffer      =  attachments.getBuffer();
            Step step              =  attachments.getStep();
            int totalSize          =  attachments.getTotalSize();
            String list            =  attachments.getList();

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
                  attachments.setRemainingBytes(remainingBytes);
                  println("res: " + res);
                  continue;
                }
                else
                {
                  //Abbiamo inviato la dimensione dell'esito
                  //Invio l'esito
                  String[] parameters = attachments.getParameters();
                  Op result = Op.valueOf(parameters[parameters.length - 1]);

                  buffer = ByteBuffer.wrap(result.toString().getBytes());
                  attachments.setRemainingBytes(buffer.array().length);
                  attachments.setBuffer(buffer);
                  attachments.setStep(Step.SendingOutcome);
                  attachments.setTotalSize(buffer.array().length);
                  attachments.setParameters(parameters);

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
                  attachments.setRemainingBytes(remainingBytes);
                  println("res: " + res);
                  continue;
                }
                else
                {
                  // Abbiamo inviato l'esito
                  String[] parameters = attachments.getParameters();

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
                      attachments.setSections(sezioni_);
                      numSezioni = 1;
                    }
                    else
                    {
                      //tutte le sezioni
                      Sezione[] arraySezioni = documento.getSezioni();
                      LinkedList<Sezione> sezioni_ =
                              new LinkedList<>(Arrays.asList(arraySezioni));
                      numSezioni = sezioni_.size();
                      attachments.setSections(sezioni_);
                    }

                    String numBytesStr = String.format("%0" + Long.BYTES + "d", numSezioni);
                    byte[] numBytes = numBytesStr.getBytes();
                    buffer = ByteBuffer.wrap(numBytes);
                    attachments.setRemainingBytes(buffer.array().length);
                    attachments.setBuffer(buffer);
                    attachments.setStep(Step.SendingNumberOfSections);
                    attachments.setTotalSize(buffer.array().length);
                    attachments.setParameters(null);
                    attachments.setList(null);

                    channel.register(selector, SelectionKey.OP_WRITE, attachments);
                  }
                  else if(requestedOperation == Op.List && result == Op.SuccessfullyListed)
                  {
                    //Devo inviare la lista dei documenti in cui collabora e quelli creati
                    //LIST TCP
                    //documentoX:creatoreX:collaboratore1_X: ... : collaboratoreN_X#
                    //documentoY:creatoreY:collaboratore1_Y: ... : collaboratoreM_Y#
                    // ...
                    //documentoZ:creatoreZ:collaboratore1_Z: ... : collaboratoreK_Z

                    Sessione sessione = sessions.get(channel);
                    Utente utente = sessione.getUtente();

                    StringJoiner joiner = new StringJoiner(DEFAULT_DELIMITER);

                    for(Documento documento : documents.values())
                    {
                      if(documento.getCreatore().equals(utente))
                      {
                        //È creatore di quel documento
                        StringJoiner interiorJoiner = new StringJoiner(DEFAULT_INTERIOR_DELIMITER);
                        interiorJoiner.add(documento.getNome()).add(utente.getNickname());
                        for(Utente collaboratore : documento.getCollaborators().values())
                        {
                          interiorJoiner.add(collaboratore.getNickname());
                        }
                        joiner.add(interiorJoiner.toString());
                      }
                      else
                      {
                        for(Utente collaboratore : documento.getCollaborators().values())
                        {
                          if(collaboratore.equals(utente))
                          {
                            //è collaboratore di quel documento
                            StringJoiner interiorJoiner = new StringJoiner(DEFAULT_INTERIOR_DELIMITER);
                            interiorJoiner.add(documento.getNome()).add(documento.getCreatore().getNickname());
                            for(Utente collaboratore_ : documento.getCollaborators().values())
                            {
                              interiorJoiner.add(collaboratore_.getNickname());
                            }
                            joiner.add(interiorJoiner.toString());
                          }
                        }
                      }
                    }

                    byte[] listBytes = joiner.toString().getBytes(StandardCharsets.ISO_8859_1);
                    String numBytesStr = String.format("%0" + Long.BYTES + "d", listBytes.length);
                    byte[] numBytes = numBytesStr.getBytes();
                    buffer = ByteBuffer.wrap(numBytes);
                    attachments.setRemainingBytes(buffer.array().length);
                    attachments.setBuffer(buffer);
                    attachments.setStep(Step.SendingListSize);
                    attachments.setTotalSize(buffer.array().length);
                    attachments.setParameters(null);
                    attachments.setList(joiner.toString());

                    channel.register(selector, SelectionKey.OP_WRITE, attachments);
                  }
                  else if(requestedOperation == Op.Edit && result == Op.SuccessfullyStartedEditing)
                  {
                    //Devo inviare una sezione, quindi sia la dimensione che la sezione stessa
                    String nomeDocumento = parameters[1];
                    Documento documento = documents.get(nomeDocumento);

                    if(documento.getMulticastAddress() == null)
                    {
                      //Indirizzo per reti private (Local-Scope Organization)
                      //239.0.0.0/8
                      boolean generateAnotherIP;
                      Long generatedIP;
                      do
                      {
                        int[] ipAddressInArray = new Random().ints(3,
                                0, 255).toArray();
                        //Trasforma IP a Long https://stackoverflow.com/a/53105157
                        long longIP = 0;
                        longIP += 239 * Math.pow(256, 3);
                        for (int i = 1; i <= 3; i++)
                        {
                          int power = 3 - i;
                          int ip = ipAddressInArray[i - 1];
                          longIP += ip * Math.pow(256, power);
                        }
                        generatedIP = longIP;
                        generateAnotherIP = false;
                        for(Documento doc : documents.values())
                        {
                          if(doc.getMulticastAddress() != null)
                          {
                            if(doc.getMulticastAddress().equals(generatedIP))
                              generateAnotherIP = true;
                          }
                        }
                      }while(generateAnotherIP);
                      documento.setMulticastAddress(generatedIP);
                    }

                    int numSezione = Integer.parseInt(parameters[2]);
                    Sezione sezione = documento.getSezioni()[numSezione];
                    LinkedList<Sezione> sezioni_ = new LinkedList<>();
                    sezioni_.add(sezione);
                    attachments.setSections(sezioni_);

                    int numSezioni = 1;
                    String numBytesStr = String.format("%0" + Long.BYTES + "d", numSezioni);
                    byte[] numBytes = numBytesStr.getBytes();
                    buffer = ByteBuffer.wrap(numBytes);
                    attachments.setRemainingBytes(buffer.array().length);
                    attachments.setBuffer(buffer);
                    attachments.setStep(Step.SendingNumberOfSections);
                    attachments.setTotalSize(buffer.array().length);
                    attachments.setParameters(null);
                    attachments.setList(null);

                    channel.register(selector, SelectionKey.OP_WRITE, attachments);
                  }
                  else if(requestedOperation == Op.EndEdit && result == Op.SuccessfullyEndedEditing)
                  {
                    //Devo ricevere una sezione, sia la dimensione che la sezione stessa
                    // vado nello stato gettingSectionSize
                    attachments.setRemainingBytes(Long.BYTES);
                    attachments.setBuffer(ByteBuffer.allocate(Long.BYTES));
                    attachments.setStep(Step.GettingSectionSize);
                    attachments.setTotalSize(Long.BYTES);
                    attachments.setParameters(parameters);  //PROPAGARE I PARAMETRI
                    attachments.setSections(null);
                    attachments.setList(null);

                    channel.register(selector, SelectionKey.OP_READ, attachments);
                  }
                  else
                  {
                    // torno nello stato WaitingForMessageSize
                    attachments.setRemainingBytes(Long.BYTES);
                    attachments.setBuffer(ByteBuffer.allocate(Long.BYTES));
                    attachments.setStep(Step.WaitingForMessageSize);
                    attachments.setTotalSize(Long.BYTES);
                    attachments.setParameters(null);
                    attachments.setSections(null);
                    attachments.setList(null);

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
                  attachments.setRemainingBytes(remainingBytes);
                  println("res: " + res);
                  continue;
                }
                else
                {
                  //Abbiamo inviato il numero di sezioni
                  //Prelevo una sezione dalla lista e
                  //invio la dimensione di questa
                  sezioni = attachments.getSections();
                  Sezione sezione = sezioni.element();
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
                    attachments.setRemainingBytes(buffer.array().length);
                    attachments.setBuffer(buffer);
                    attachments.setStep(Step.SendingSectionSize);
                    attachments.setTotalSize(buffer.array().length);
                    attachments.setSections(sezioni); //Parametri

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
                  attachments.setRemainingBytes(remainingBytes);
                  println("res: " + res);
                  continue;
                }
                else
                {
                  //Abbiamo inviato la dimensione della sezione
                  //Invio il numero identificativo della sezione
                  sezioni = attachments.getSections();

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
                    attachments.setRemainingBytes(buffer.array().length);
                    attachments.setBuffer(buffer);
                    attachments.setStep(Step.SendingSectionNumber);
                    attachments.setTotalSize(buffer.array().length);
                    attachments.setSections(sezioni);

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
                  attachments.setRemainingBytes(remainingBytes);
                  println("res: " + res);
                  continue;
                }
                else
                {
                  //Abbiamo inviato il numero identificativo di sezione
                  //Invia l'indirizzo Multicast

                  sezioni = attachments.getSections();
                  Sezione sezione = sezioni.element();
                  String nomeDocumento = sezione.getNomeDocumento();
                  Documento documento = documents.get(nomeDocumento);
                  Long multicastAddress = documento.getMulticastAddress();
                  buffer = ByteBuffer.allocate(Long.BYTES);
                  buffer.putLong(multicastAddress);
                  buffer.flip();

                  attachments.setRemainingBytes(buffer.array().length);
                  attachments.setBuffer(buffer);
                  attachments.setStep(Step.SendingMulticastAddress);
                  attachments.setTotalSize(buffer.array().length);
                  attachments.setSections(sezioni);

                  channel.register(selector, SelectionKey.OP_WRITE, attachments);
                }

                break;

              case SendingMulticastAddress :
                res = channel.write(buffer);
                println("SendingMulticastAddress Written " + res + " bytes");

                if(res < remainingBytes)
                {
                  //Non abbiamo finito di mandare l'indirizzo Multicast
                  remainingBytes -= res;
                  attachments.setRemainingBytes(remainingBytes);
                  println("res: " + res);
                  continue;
                }
                else
                {
                  //Abbiamo inviato l'indirizzo Multicast
                  //Invio lo stato di modifica sezione
                  sezioni = attachments.getSections();
                  Sezione sezione = sezioni.element();
                  long stato;
                  if(sezione.getUserEditing() == null)
                    stato = 0;
                  else
                    stato = 1;
                  String numBytesStr = String.format("%0" + Long.BYTES + "d", stato);
                  byte[] numBytes = numBytesStr.getBytes();
                  buffer = ByteBuffer.wrap(numBytes);
                  attachments.setRemainingBytes(buffer.array().length);
                  attachments.setBuffer(buffer);
                  attachments.setStep(Step.SendingSectionStatus);
                  attachments.setTotalSize(buffer.array().length);
                  attachments.setSections(sezioni);

                  channel.register(selector, SelectionKey.OP_WRITE, attachments);
                }
                break;

              case SendingSectionStatus :
                res = channel.write(buffer);
                println("SendingSectionStatus Written " + res + " bytes");

                if(res < remainingBytes)
                {
                  //Non abbiamo finito di mandare lo stato di modifica sezione
                  remainingBytes -= res;
                  attachments.setRemainingBytes(remainingBytes);
                  println("res: " + res);
                  continue;
                }
                else
                {
                  //Abbiamo inviato lo stato di modifica sezione
                  //Invio la sezione
                  sezioni = attachments.getSections();
                  Sezione sezione = sezioni.element();
                  Path filePath = Paths.get(DEFAULT_PARENT_FOLDER + File.separator +
                          sezione.getNomeDocumento() + File.separator +
                          sezione.getNomeSezione() + ".txt");
                  try
                  {
                    FileChannel fileChannel = FileChannel.open(filePath);
                    long dimensioneFile = fileChannel.size();
                    //Decido di bufferizzare l'intero file
                    buffer = ByteBuffer.allocate(Math.toIntExact(dimensioneFile));

                    while(buffer.hasRemaining())
                      fileChannel.read(buffer);
                    //FIXME: Non si sa se sia indispensabile più di una lettura.

                    buffer.flip();

                    //Il buffer è sufficientemente grande per contenere l'intero file
                    attachments.setRemainingBytes(buffer.array().length);
                    attachments.setBuffer(buffer);
                    attachments.setStep(Step.SendingSection);
                    attachments.setTotalSize(buffer.array().length);
                    attachments.setSections(sezioni);

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
                  printErr("REMAINING BYTES: " + remainingBytes + "RES: " + res);
                  //Non abbiamo finito di mandare la sezione
                  remainingBytes -= res;
                  attachments.setRemainingBytes(remainingBytes);
                  println("res: " + res);
                  continue;
                }
                else
                {
                  //Abbiamo inviato l'intera sezione
                  //Vedo se bisogna o meno inviare un'altra sezione
                  sezioni = attachments.getSections();
                  //Rimuovo la sezione perchè è stata appena inviata SOPRA
                  sezioni.remove();
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
                      attachments.setRemainingBytes(buffer.array().length);
                      attachments.setBuffer(buffer);
                      attachments.setStep(Step.SendingSectionSize);
                      attachments.setTotalSize(buffer.array().length);
                      attachments.setSections(sezioni);

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
                    attachments.setRemainingBytes(Long.BYTES);
                    attachments.setBuffer(ByteBuffer.allocate(Long.BYTES));
                    attachments.setStep(Step.WaitingForMessageSize);
                    attachments.setTotalSize(Long.BYTES);
                    attachments.setSections(null);
                    attachments.setList(null);

                    channel.register(selector, SelectionKey.OP_READ, attachments);
                  }
                }
                break;

              case SendingListSize :
                res = channel.write(buffer);
                println("SendingListSize Written " + res + " bytes");

                if(res < remainingBytes)
                {
                  //Non abbiamo finito di mandare la dimensione della lista
                  remainingBytes -= res;
                  attachments.setRemainingBytes(remainingBytes);
                  println("res: " + res);
                  continue;
                }
                else
                {
                  //Abbiamo inviato la dimensione della lista
                  //Invio la lista
                  list = attachments.getList();
                  byte[] listBytes = list.getBytes(StandardCharsets.ISO_8859_1);
                  buffer = ByteBuffer.wrap(listBytes);

                  attachments.setRemainingBytes(buffer.array().length);
                  attachments.setBuffer(buffer);
                  attachments.setStep(Step.SendingList);
                  attachments.setTotalSize(buffer.array().length);
                  attachments.setSections(null);
                  attachments.setList(null);

                  channel.register(selector, SelectionKey.OP_WRITE, attachments);
                }
                break;


              case SendingList :
                res = channel.write(buffer);
                println("SendingList Written " + res + " bytes");

                if(res < remainingBytes)
                {
                  //Non abbiamo finito di mandare la lista
                  remainingBytes -= res;
                  attachments.setRemainingBytes(remainingBytes);
                  println("res: " + res);
                  continue;
                }
                else
                {
                  //Abbiamo inviato la lista
                  //torno nello stato WaitingForMessageSize
                  attachments.setRemainingBytes(Long.BYTES);
                  attachments.setBuffer(ByteBuffer.allocate(Long.BYTES));
                  attachments.setStep(Step.WaitingForMessageSize);
                  attachments.setTotalSize(Long.BYTES);
                  attachments.setSections(null);
                  attachments.setList(null);

                  channel.register(selector, SelectionKey.OP_READ, attachments);
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
