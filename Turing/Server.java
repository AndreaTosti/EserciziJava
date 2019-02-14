package Turing;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Server
{
  private static int DEFAULT_PORT = 51811; //Porta di Default
  private static int DEFAULT_RMI_PORT = 51812; //Porta RMI
  private static int BUFFER_SIZE  = 4096; //Dimensione del buffer
  private static String DEFAULT_DELIMITER = "#";

  //Utenti
  private static final ConcurrentMap<String, Utente> users = new ConcurrentHashMap<>();

  //Sessione utente
  private static final Map<SocketChannel, Sessione> sessions = new HashMap<>();

  private static void println(String string)
  {
    System.out.println("[Server] " + string);
  }

  private static void printErr(String string)
  {
    System.err.println("[Server-Error] " + string);
  }

  private static void handleLogin(String[] splitted, SocketChannel channel)
  {
    String nickname = splitted[1];
    String password = splitted[2];

    //TODO: Restituire un messaggio TCP al client
    Utente utente = users.get(nickname);

    if(utente != null)
    {
      if(utente.getPassword().compareTo(password) == 0)
      {
        //La password è corretta
        Sessione sessione = sessions.get(channel);
        if(sessione != null)
        {
          sessione.setUtente(utente);
          sessione.setStato(Sessione.Stato.Logged);
          println("Sessione utente va in Logged");
        }
        else
        {
          printErr("Sessione inesistente");
        }
      }
      else
      {
        //Password non corretta
        printErr("Password " + password + " non corrisponde a " +
                utente.getPassword());
      }
    }
    else
    {
      //L'utente non esiste
      printErr("L'utente " + nickname + " non esiste");
    }
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
      RegUtenteInterface stub = (RegUtenteInterface) UnicastRemoteObject.exportObject(object, 0);
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
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            client.register(selector, SelectionKey.OP_READ, buffer);

            //Crea una nuova sessione per il client
            sessions.put(client, new Sessione());

          }
          if(key.isValid() && key.isReadable())
          {
            //Nuovo evento in lettura
            println("key.isReadable ");
            SocketChannel channel = (SocketChannel)key.channel();
            ByteBuffer buffer = (ByteBuffer) key.attachment();
            int res;
            buffer.clear();
            res = channel.read(buffer);
            if(res < 0)
            {
              //TODO: Il client si è disconnesso
              if(sessions.remove(channel) == null)
              {
                printErr("Sessione inesistente");
              }
              else
              {
                println("Sessione rimossa con successo per il client " +
                        channel.getRemoteAddress());
              }
              channel.close();
            }
            else
            {
              buffer.flip();
              String toSplit = new String(buffer.array(), 0, res, StandardCharsets.ISO_8859_1);
              String[] splitted = toSplit.split(DEFAULT_DELIMITER);
              Op requestedOperation = Op.valueOf(splitted[0]);
              println("Requested Operation : " + requestedOperation.toString() +
                      " from Client IP : " + channel.socket().getInetAddress().getHostAddress() +
                      " port : " + channel.socket().getPort());
              switch(requestedOperation)
              {
                case Login :
                  handleLogin(splitted, channel);
                  break;
                case Logout :
                  println("logout");
                  break;
                default :
                  println("nessuna delle precedenti");
                  break;
              }
            }
          }
        }
        catch(IOException e)
        {
          printErr("Exception");
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
