package Turing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.StringJoiner;
import java.util.regex.Pattern;

public class Client
{
  private static String DEFAULT_HOST = "localhost";
  private static int DEFAULT_PORT = 51811; //Porta di Default
  private static int DEFAULT_RMI_PORT = 51812; //Porta RMI
  private static String DEFAULT_DELIMITER = "#";
  private static int DEFAULT_BUFFER_SIZE = 4096;

  private static Pattern validPattern = Pattern.compile("[A-Za-z0-9_]+");

  private static void println(Object o)
  {
    System.out.println("[Client] " + o);
  }

  private static void printErr(Object o)
  {
    System.err.println("[Client-Error] " + o);
  }

  private static boolean isAValidString(String string)
  {
    return validPattern.matcher(string).matches();
  }


  private static Op sendRequest(String joinedString, SocketChannel client)
  {
    byte[] operation = joinedString.getBytes(StandardCharsets.ISO_8859_1);
    //Invio il numero di bytes dell'operazione facendo il Padding
    String numBytesStr = String.format("%0" + Long.BYTES + "d", operation.length);
    byte[] numBytes = numBytesStr.getBytes();
    ByteBuffer buffer0 = ByteBuffer.wrap(numBytes);
    ByteBuffer buffer1 = ByteBuffer.wrap(operation);
    try
    {
      printErr("Written " + client.write(buffer0) + " bytes");
      printErr("Written " + client.write(buffer1) + " bytes");
      return Op.SuccessfullySent;
    }
    catch(IOException e)
    {
      e.printStackTrace();
      return Op.Error;
    }
  }

  private static Op receiveOutcome(SocketChannel client)
  {
    ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
    int res;
    try
    {
      res = client.read(buffer);
      if(res < 0)
      {
        return Op.ClosedConnection;
      }
      else
      {
        buffer.flip();
        String result = new String(buffer.array(), 0,
                res, StandardCharsets.ISO_8859_1);
        return Op.valueOf(result);
      }
    }
    catch(IOException e)
    {
      //TODO: Qui il server potrebbe aver chiuso brutalmente il server
      //      restituire ClosedConnection invece di stampare lo stacktrace
      e.printStackTrace();
      return Op.Error;
    }
  }


  private static Op handleRegister(String[] splitted)
  {
    //Controllo il numero di parametri
    if(splitted.length != 3)
    {
      printErr("Usage: register <username> <password>");
      return Op.UsageError;
    }

    String username = splitted[1];
    String password = splitted[2];

    if(!isAValidString(username) || !isAValidString(password))
    {
      printErr("Usage: register <username> <password>");
      return Op.UsageError;
    }

    //REGISTRAZIONE RMI
    try
    {
      Registry registry = LocateRegistry.getRegistry(DEFAULT_HOST, DEFAULT_RMI_PORT);
      RegUtenteInterface stub = (RegUtenteInterface) registry.lookup("RegUtente");
      return stub.registerUser(username, password);
    }
    catch(RemoteException e1)
    {
      return Op.ClosedConnection;
    }
    catch(NotBoundException e)
    {
      e.printStackTrace();
      return Op.Error;
    }
  }

  private static Op handleLogin(String[] splitted, SocketChannel client)
  {
    //Controllo il numero di parametri
    if(splitted.length != 3)
    {
      printErr("Usage: login <username> <password>");
      return Op.UsageError;
    }

    String username = splitted[1];
    String password = splitted[2];

    //LOGIN TCP
    //login#username#password
    StringJoiner joiner = new StringJoiner(DEFAULT_DELIMITER);
    joiner.add(Op.Login.toString()).add(username).add(password);
    Op resultSend = sendRequest(joiner.toString(), client);
    println(resultSend);

    return receiveOutcome(client);
  }

  private static Op handleLogout(SocketChannel client)
  {
    //LOGOUT TCP
    //logout

    StringJoiner joiner = new StringJoiner(DEFAULT_DELIMITER);
    joiner.add(Op.Logout.toString());
    Op result = sendRequest(joiner.toString(), client);
    println(result);

    return receiveOutcome(client);
  }

  private static Op handleCreate(String[] splitted, SocketChannel client)
  {
    //Controllo il numero di parametri
    if(splitted.length != 3)
    {
      printErr("Usage: create <doc> <numsezioni>");
      return Op.UsageError;
    }

    String nomeDocumento = splitted[1];

    if(!isAValidString(nomeDocumento))
    {
      printErr("Usage: create <doc> <numsezioni>");
      return Op.UsageError;
    }

    int numSezioni;
    try
    {
      numSezioni = Integer.parseInt(splitted[2]);
    }
    catch(NumberFormatException e)
    {
      printErr("Usage: create <doc> <numsezioni>");
      return Op.UsageError;
    }

    if(numSezioni < 1)
    {
      printErr("Usage: create <doc> <numsezioni>");
      return Op.UsageError;
    }

    //CREAZIONE DOCUMENTO TCP
    //create#nomedocumento#numsezioni

    StringJoiner joiner = new StringJoiner(DEFAULT_DELIMITER);
    joiner.add(Op.Create.toString()).add(nomeDocumento).add(splitted[2]);

    Op result = sendRequest(joiner.toString(), client);
    println(result);

    return receiveOutcome(client);

  }

  private static Op handleShare(String[] splitted, SocketChannel client)
  {
    //Controllo il numero di parametri
    if(splitted.length != 3)
    {
      printErr("Usage: share <doc> <username>");
      return Op.UsageError;
    }

    String nomeDocumento = splitted[1];
    String username = splitted[2];

    if(!isAValidString(nomeDocumento) || !isAValidString((username)))
    {
      printErr("Usage: share <doc> <username>");
      return Op.UsageError;
    }

    //CONDIVISIONE DOCUMENTO TCP
    //share#nomedocumento#username

    StringJoiner joiner = new StringJoiner(DEFAULT_DELIMITER);
    joiner.add(Op.Share.toString()).add(nomeDocumento).add(username);

    Op result = sendRequest(joiner.toString(), client);
    println(result);

    return receiveOutcome(client);

  }

  private static Op handleShow(String[] splitted, SocketChannel client)
  {
    //Controllo il numero di parametri
    if(splitted.length != 2 && splitted.length != 3)
    {
      printErr("Usage: show <doc> [<sec>]");
      return Op.UsageError;
    }

    String nomeDocumento = splitted[1];

    if(!isAValidString(nomeDocumento))
    {
      printErr("Usage: show <doc> [<sec>]");
      return Op.UsageError;
    }

    int numSezione;
    if(splitted.length == 3)
    {
      try
      {
        numSezione = Integer.parseInt(splitted[2]);
      }
      catch(NumberFormatException e)
      {
        printErr("Usage: show <doc> [<sec>]");
        return Op.UsageError;
      }
      if(numSezione < 1)
      {
        printErr("Usage: show <doc> [<sec>]");
        return Op.UsageError;
      }
    }

    //Visualizzazione di una sezione o dell'intero documento
    //show#nomedocumento[#numsezione]

    StringJoiner joiner = new StringJoiner(DEFAULT_DELIMITER);
    joiner.add(Op.Show.toString()).add(nomeDocumento);
    if(splitted.length == 3)
      joiner.add(splitted[2]);

    Op result = sendRequest(joiner.toString(), client);
    println(result);

    return receiveOutcome(client);

  }

  public static void main(String[] args)
  {
    SocketAddress address = new InetSocketAddress(DEFAULT_HOST, DEFAULT_PORT);
    SocketChannel client = null;
    try
    {
      client = SocketChannel.open(address);
    }
    catch(ConnectException e1)
    {
      printErr("Connection refused on port " + DEFAULT_PORT);
      printErr("Exiting...");
      return;
    }
    catch(IOException e2)
    {
      e2.printStackTrace();
    }

    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    String stdin;

    boolean loggedIn = false;

    try
    {
      //Input da linea di comando
      Op result = null;
      while(result != Op.ClosedConnection)
      {
        stdin = reader.readLine();
        String[] splitted = stdin.split("\\s+");

        switch(splitted[0].toLowerCase())
        {
          case "register" :
            if(loggedIn)
            {
              printErr("Non puoi registrarti mentre sei loggato");
            }
            else
            {
              result = handleRegister(splitted);
              println("Result = " + result);
            }
            break;

          case "login" :
            result = handleLogin(splitted, client);
            println("Result = " + result);
            if(result == Op.SuccessfullyLoggedIn)
              loggedIn = true;
            break;

          case "logout" :
            result = handleLogout(client);
            println("Result = " + result);
            if(result == Op.SuccessfullyLoggedOut)
              loggedIn = false;
            break;

          case "create" :
            result = handleCreate(splitted, client);
            println("Result = " + result);
            break;

          case "share" :
            result = handleShare(splitted, client);
            println("Result = " + result);
            break;

          case "show" :
            result = handleShow(splitted, client);
            println("Result = " + result);
            break;

          default:
            printErr("Comando non riconosciuto");
            break;
        }

      }
      reader.close();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }

  }

}
