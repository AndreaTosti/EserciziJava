package Turing;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
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
  private static String DEFAULT_PARENT_FOLDER = "518111_ServerDirectories";


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

    Documento documento = new Documento(nomeDocumento, numSezioni, utente);
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
                nomeDocumento + File.separator + sezione.getNome() + ".txt");
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

    int numSezione;
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
                      buffer = ByteBuffer.wrap(result.toString().getBytes());
                      attachments.set(0, buffer.array().length);
                      attachments.set(1, buffer);
                      attachments.set(2, Step.SendingOutcome);
                      attachments.set(3, buffer.array().length);

                      if(result == Op.SuccessfullyShown)
                      {
                        attachments.set(4, splitted); //Parametri
                      }
                      else
                      {
                        attachments.set(4, null); //Parametri
                      }

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

            int res;
            SocketChannel channel = (SocketChannel)key.channel();
            ArrayList<Object> attachments = (ArrayList) key.attachment();
            int remainingBytes = (int) attachments.get(0);
            ByteBuffer buffer = (ByteBuffer) attachments.get(1);
            Step step = (Step) attachments.get(2);
            int totalSize = (int) attachments.get(3);
            String[] parameters = (String[]) attachments.get(4);


            switch(step)
            {
              case SendingOutcome :
                res = channel.write(buffer);

                println("Written " + res + " bytes");
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
                  attachments.set(0, Long.BYTES); //Remaining Bytes
                  attachments.set(1, ByteBuffer.allocate(Long.BYTES));

                  if(parameters != null)
                  {
                    Op requestedOperation = Op.valueOf(parameters[0]);
                    if(requestedOperation == Op.Show)
                    {
                      attachments.set(2, Step.SendingNumberOfSections);
                      channel.register(selector, SelectionKey.OP_WRITE, attachments);
                    }
                  }
                  else
                  {
                    // torno nello stato WaitingForMessageSize
                    attachments.set(2, Step.WaitingForMessageSize);
                    channel.register(selector, SelectionKey.OP_READ, attachments);
                  }


                  attachments.set(3, Long.BYTES); //Total Size
                }
                break;

              case SendingNumberOfSections :

                String nomeDocumento = parameters[1];
                Documento documento = documents.get(nomeDocumento);
                Sezione[] sezioni;
                int numSezioni;

                if(parameters.length == 3)
                {
                  //singola sezione
                  int numSezione = Integer.parseInt(parameters[2]);
                  Sezione sezione = documento.getSezioni()[numSezione];
                  sezioni = new Sezione[]{sezione};
                  numSezioni = 1;
                }
                else if(parameters.length == 2)
                {
                  //tutte le sezioni
                  sezioni = documento.getSezioni();
                  numSezioni = documento.getSezioni().length;
                }
                else
                {
                  sezioni = null;
                  numSezioni = -1;
                }
                //Invio il numero di sezioni
                String numBytesStr = String.format("%0" + Long.BYTES + "d", numSezioni);
                byte[] numBytes = numBytesStr.getBytes();
                buffer = ByteBuffer.wrap(numBytes);
                attachments.set(0, buffer.array().length);
                attachments.set(1, buffer);
                attachments.set(2, Step.SendingSections);
                attachments.set(3, buffer.array().length);
                attachments.set(4, sezioni); //Parametri
                break;

              default :
                println("nessuna delle precedenti");
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
