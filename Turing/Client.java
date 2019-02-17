package Turing;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.EnumSet;
import java.util.StringJoiner;
import java.util.regex.Pattern;

public class Client
{
  private static String DEFAULT_HOST = "localhost";
  private static int DEFAULT_PORT = 51811; //Porta di Default
  private static int DEFAULT_RMI_PORT = 51812; //Porta RMI
  private static String DEFAULT_DELIMITER = "#";
  private static int DEFAULT_BUFFER_SIZE = 4096;
  private static String DEFAULT_PARENT_FOLDER = "518111_ClientDirs";

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
      if(numSezione < 0)
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

  private static Op receiveSections(String[] splitted, SocketChannel client,
                                    String loggedInNickname)
  {
    ByteBuffer bufferNumSezioni = ByteBuffer.allocate(Long.BYTES);
    ByteBuffer bufferDimensione = ByteBuffer.allocate(Long.BYTES);
    ByteBuffer bufferNumSezione = ByteBuffer.allocate(Long.BYTES);
    int resNi, resD, resNe, res, counter;
    try
    {
      resNi = client.read(bufferNumSezioni);
      if(resNi < 0)
        return Op.ClosedConnection;

      bufferNumSezioni.flip();
      int numeroSezioni = Integer.valueOf(new String(bufferNumSezioni.array(), 0,
              resNi, StandardCharsets.ISO_8859_1));


      printErr("----NUMERO SEZIONI: " + numeroSezioni);

      for(int i = 0; i < numeroSezioni; i++)
      {
        //TODO: cambiare le seguenti due righe per riutilizzare i buffer
        bufferDimensione = ByteBuffer.allocate(Long.BYTES);
        bufferNumSezione = ByteBuffer.allocate(Long.BYTES);

        resD = client.read(bufferDimensione);
        if(resD < 0)
          return Op.ClosedConnection;

        resNe = client.read(bufferNumSezione);
        if(resNe < 0)
          return Op.ClosedConnection;


        bufferDimensione.flip();
        int dimensioneFile = Integer.valueOf(new String(bufferDimensione.array(), 0,
                resD, StandardCharsets.ISO_8859_1));

        bufferNumSezione.flip();
        int numeroSezione = Integer.valueOf(new String(bufferNumSezione.array(), 0,
                resNe, StandardCharsets.ISO_8859_1));

        println("NUMERO SEZIONE : " + numeroSezione);
        //Decido di non allocare il buffer con dimensione pari a quella del file
        ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
        //Per il testing in localhost, il nome della cartella conterrà anche
        //l'username per distinguerlo da altri client sullo stesso host

        //Si assume che il nomeSezione sia nome_numSezione
        String nomeDocumento = splitted[1];
        Path filePath = Paths.get(System.getProperty("user.dir") +
                File.separator + DEFAULT_PARENT_FOLDER +
                File.separator + loggedInNickname +
                File.separator + nomeDocumento +
                File.separator + nomeDocumento +
                "_" + numeroSezione + ".txt");

        Path directoryPath = Paths.get(System.getProperty("user.dir") +
                File.separator + DEFAULT_PARENT_FOLDER +
                File.separator + loggedInNickname +
                File.separator + nomeDocumento);

        if (!Files.exists(directoryPath))
          Files.createDirectories(directoryPath);

        FileChannel fileChannel = FileChannel.open(filePath, EnumSet.of(
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE));
        res = 0;
        counter = 0;
        do
        {
          buffer.clear();
          res = client.read(buffer);
          if(res < 0)
          {
            return Op.ClosedConnection;
          }
          else
          {
            buffer.flip();
            if(res > 0)
            {
              fileChannel.write(buffer);
              counter += res;
            }
            dimensioneFile -= res;
          }
        }while(dimensioneFile > 0);
        fileChannel.close();
      }
    }
    catch(IOException e)
    {
      e.printStackTrace();
      return Op.Error;
    }
    return Op.SuccessfullyReceivedSections;
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

    String loggedInNickname = null;

    try
    {
      //Input da linea di comando
      Op result = null;
      Op result_2 = null;

      while(result != Op.ClosedConnection)
      {
        stdin = reader.readLine();
        String[] splitted = stdin.split("\\s+");

        switch(splitted[0].toLowerCase())
        {
          case "register" :
            if(loggedInNickname != null)
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
              loggedInNickname = splitted[1];
            break;

          case "logout" :
            result = handleLogout(client);
            println("Result = " + result);
            if(result == Op.SuccessfullyLoggedOut)
              loggedInNickname = null;
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
            if(result == Op.SuccessfullyShown)
            {
              result_2 = receiveSections(splitted, client, loggedInNickname);
              println("Result2 = " + result_2);
            }

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
