package Turing;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.EnumSet;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.Date;

public class Client
{
  private static String DEFAULT_HOST = "localhost";
  private static int DEFAULT_PORT = 51811; //Porta di Default
  private static int DEFAULT_RMI_PORT = 51812; //Porta RMI
  private static String DEFAULT_DELIMITER = "#";
  private static String DEFAULT_INTERIOR_DELIMITER = ":";
  private static int DEFAULT_BUFFER_SIZE = 4096;
  private static String DEFAULT_PARENT_FOLDER = "518111_ClientDirs";
  private static InetAddress TEST_MULTICAST_ADDRESS;

  //TODO: Multicast
  private static MulticastSocket ms = null;



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
    ByteBuffer bufferDimensione = ByteBuffer.allocate(Long.BYTES);
    int resD;

    try
    {
      resD = client.read(bufferDimensione);
      if(resD < 0)
        return Op.ClosedConnection;

      bufferDimensione.flip();
      int dimensione = Integer.valueOf(new String(bufferDimensione.array(), 0,
              resD, StandardCharsets.ISO_8859_1));

      ByteBuffer buffer = ByteBuffer.allocate(dimensione);
      int res;
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
    println("FORMATO: " + joiner);
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

    int resNi, resD, resNe, resI, resS, res, counter;
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
        //TODO: cambiare le seguenti tre righe per riutilizzare i buffer
        ByteBuffer bufferDimensione = ByteBuffer.allocate(Long.BYTES);
        ByteBuffer bufferNumSezione = ByteBuffer.allocate(Long.BYTES);
        ByteBuffer bufferIndirizzoMulticast = ByteBuffer.allocate(Long.BYTES);
        ByteBuffer bufferStato      = ByteBuffer.allocate(Long.BYTES);

        resD = client.read(bufferDimensione);
        if(resD < 0)
          return Op.ClosedConnection;

        resNe = client.read(bufferNumSezione);
        if(resNe < 0)
          return Op.ClosedConnection;

        resI = client.read(bufferIndirizzoMulticast);
        if(resI < 0)
          return Op.ClosedConnection;

        resS = client.read(bufferStato);
        if(resS < 0)
          return Op.ClosedConnection;

        bufferDimensione.flip();
        int dimensioneFile = Integer.valueOf(new String(bufferDimensione.array(), 0,
                resD, StandardCharsets.ISO_8859_1));

        bufferNumSezione.flip();
        int numeroSezione = Integer.valueOf(new String(bufferNumSezione.array(), 0,
                resNe, StandardCharsets.ISO_8859_1));

        //TODO: da testare
        bufferIndirizzoMulticast.flip();

        long longIP = bufferIndirizzoMulticast.getLong();

        /*
         *  Trasforma IP da Long
         *  https://stackoverflow.com/a/53105157
         */
        StringBuilder sb = new StringBuilder(15);
        for (int j = 0; j < 4; j++)
        {
          sb.insert(0,Long.toString(longIP & 0xff));
          if (j < 3) {
            sb.insert(0,'.');
          }
          longIP = longIP >> 8;
        }
        try
        {
          InetAddress indirizzoMulticast =
                  InetAddress.getByName(sb.toString());
        }catch(UnknownHostException e1)
        {
          e1.printStackTrace();
        }

        try
        {
          TEST_MULTICAST_ADDRESS = InetAddress.getByName(sb.toString());
        }catch(UnknownHostException e)
        {
          e.printStackTrace();
        }

//        ms.joinGroup(TEST_MULTICAST_ADDRESS);


        bufferStato.flip();
        int stato = Integer.valueOf(new String(bufferStato.array(), 0,
                resS, StandardCharsets.ISO_8859_1));

        ByteBuffer buffer = ByteBuffer.allocate(dimensioneFile);
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

        counter = 0;
        res = 0;
        while(dimensioneFile > 0)
        {
          buffer.clear();
          printErr("DA RICEVERE " + dimensioneFile + " bytes");
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
        }
        fileChannel.close();
        printErr("Ricevuti " + res + " bytes");
        printErr("La sezione " + nomeDocumento + "_" + numeroSezione +
                (stato == 1 ?
                              " è attualmente sotto modifiche" :
                              " non è attualmente sotto modifiche"));
      }
    }
    catch(IOException e)
    {
      e.printStackTrace();
      return Op.Error;
    }
    return Op.SuccessfullyReceivedSections;
  }

  private static Op handleList(SocketChannel client)
  {
    //LIST TCP
    //list

    StringJoiner joiner = new StringJoiner(DEFAULT_DELIMITER);
    joiner.add(Op.List.toString());
    Op result = sendRequest(joiner.toString(), client);
    println(result);

    return receiveOutcome(client);
  }

  private static Op receiveList(SocketChannel client)
  {
    ByteBuffer bufferDimensione = ByteBuffer.allocate(Long.BYTES);
    int resD, res;

    try
    {
      resD = client.read(bufferDimensione);
      if(resD < 0)
        return Op.ClosedConnection;

      bufferDimensione.flip();
      int dimensione = Integer.valueOf(new String(bufferDimensione.array(), 0,
              resD, StandardCharsets.ISO_8859_1));

      ByteBuffer buffer = ByteBuffer.allocate(dimensione);
      res = client.read(buffer);
      if(res != dimensione)
        return Op.Error;

      buffer.flip();
      String toSplit = new String(buffer.array(), 0,
              dimensione, StandardCharsets.ISO_8859_1);
      String[] splittedDocs = toSplit.split(DEFAULT_DELIMITER);

      StringBuilder builder = new StringBuilder();
      for(String documento : splittedDocs)
      {
        if(documento.equals(""))
          continue;
        String[] splitted = documento.split(DEFAULT_INTERIOR_DELIMITER);
        builder.append("\n\t\t");
        builder.append(splitted[0]);
        builder.append(":");
        builder.append("\n\t\t\tCreatore: ");
        builder.append(splitted[1]);
        builder.append("\n\t\t\tCollaboratori: ");
        for(int i = 2; i < splitted.length; i++)
        {
          builder.append(splitted[i]);
          builder.append(" ");
        }
      }

      String result = builder.toString();
      if(result.equals(""))
        println("Non ci sono documenti di cui si è creatori o di cui si collabora");
      else
        println(builder.toString());
      return Op.SuccessfullyReceivedList;
    }
    catch(IOException e)
    {
      e.printStackTrace();
      return Op.Error;
    }
  }

  private static Op handleEdit(String[] splitted, SocketChannel client)
  {
    //Controllo il numero di parametri
    if(splitted.length != 3)
    {
      printErr("Usage: edit <doc> <sec>");
      return Op.UsageError;
    }

    String nomeDocumento = splitted[1];

    if(!isAValidString(nomeDocumento))
    {
      printErr("Usage: edit <doc> <sec>");
      return Op.UsageError;
    }

    int numSezione;
    try
    {
      numSezione = Integer.parseInt(splitted[2]);
    }
    catch(NumberFormatException e)
    {
      printErr("Usage: edit <doc> <sec>");
      return Op.UsageError;
    }
    if(numSezione < 0)
    {
      printErr("Usage: edit <doc> <sec>");
      return Op.UsageError;
    }

    //Richiesta modifica di una sezione
    //edit#nomedocumento#numsezione

    StringJoiner joiner = new StringJoiner(DEFAULT_DELIMITER);
    joiner.add(Op.Edit.toString()).add(nomeDocumento).add(splitted[2]);

    Op result = sendRequest(joiner.toString(), client);
    println(result);

    return receiveOutcome(client);

  }

  private static Op handleEndEdit(String[] splitted, SocketChannel client)
  {
    //Controllo il numero di parametri
    if(splitted.length != 3)
    {
      printErr("Usage: end-edit <doc> <sec>");
      return Op.UsageError;
    }

    String nomeDocumento = splitted[1];

    if(!isAValidString((nomeDocumento)))
    {
      printErr("Usage: end-edit <doc> <sec>");
      return Op.UsageError;
    }

    int numSezione;
    try
    {
      numSezione = Integer.parseInt(splitted[2]);
    }
    catch(NumberFormatException e)
    {
      printErr("Usage: end-edit <doc> <sec>");
      return Op.UsageError;
    }
    if(numSezione < 0)
    {
      printErr("Usage: end-edit <doc> <sec>");
      return Op.UsageError;
    }

    //Richiesta fine modifica di una sezione
    //end-edit#nomedocumento#numsezione

    StringJoiner joiner = new StringJoiner(DEFAULT_DELIMITER);
    joiner.add(Op.EndEdit.toString()).add(nomeDocumento).add(splitted[2]);

    Op result = sendRequest(joiner.toString(), client);
    println(result);

    return receiveOutcome(client);
  }

  private static Op sendSection(String[] splitted, SocketChannel client,
                                String loggedInNickname)
  {
    String nomeDocumento = splitted[1];
    int numSezione = Integer.parseInt(splitted[2]);
    Path filePath = Paths.get(System.getProperty("user.dir") +
            File.separator + DEFAULT_PARENT_FOLDER +
            File.separator + loggedInNickname +
            File.separator + nomeDocumento +
            File.separator + nomeDocumento +
            "_" + numSezione + ".txt");
    try
    {
      if(Files.notExists(filePath))
      {
        //FIXME: la sezione non esiste nella directory
        //       creo un file vuoto e lo invio lo stesso
        printErr("Filename " + nomeDocumento +
                "_" + numSezione + ".txt" +
                " does not exists in the current working directory: " +
                System.getProperty("user.dir"));
        //Creo un file vuoto
        Files.createFile(filePath);
      }
      FileChannel fileChannel = FileChannel.open(filePath);

      long dimensioneFile = fileChannel.size();
      String numBytesStr = String.format("%0" + Long.BYTES + "d", dimensioneFile);
      byte[] numBytes = numBytesStr.getBytes();
      ByteBuffer bufferDimensione = ByteBuffer.wrap(numBytes);

      //Decido di bufferizzare l'intero file
      ByteBuffer buffer = ByteBuffer.allocate(Math.toIntExact(dimensioneFile));
      while(buffer.hasRemaining())
        fileChannel.read(buffer);
      //FIXME: Non si sa se sia indispensabile più di una lettura.

      fileChannel.close();

      buffer.flip();

      printErr("Written " + client.write(bufferDimensione) + " bytes");
      printErr("Written " + client.write(buffer) + " bytes");
      return Op.SuccessfullySentSection;
    }
    catch(IOException e)
    {
      e.printStackTrace();
      return Op.Error;
    }
  }

  private static Op handleSend(String[] splitted, SocketChannel client,
                               String loggedInNickname)
  {
    //Controllo il numero di parametri
    if(splitted.length != 2)
    {
      printErr("Usage: send <msg>");
      return Op.UsageError;
    }

    String messaggio = splitted[1];

    StringBuilder builder = new StringBuilder();
    builder.append(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date()));
    builder.append(" ");
    builder.append(loggedInNickname);
    builder.append(": ");
    builder.append(messaggio);

    DatagramPacket packetToSend = new DatagramPacket(
            builder.toString().getBytes(StandardCharsets.UTF_8),
            builder.toString().getBytes(StandardCharsets.UTF_8).length,
            TEST_MULTICAST_ADDRESS, DEFAULT_PORT);
    try
    {
      ms.send(packetToSend);
    }catch(IOException e)
    {
      e.printStackTrace();
      return Op.Error;
    }

    return Op.SuccessfullySentMessage;
  }

  private static Op handleReceive(SocketChannel client)
  {
    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    DatagramPacket packetToReceive = new DatagramPacket(buffer, buffer.length);
    try
    {
      ms.receive(packetToReceive);
    }catch(IOException e)
    {
      e.printStackTrace();
      return Op.Error;
    }

    String s = new String(packetToReceive.getData(), 0,
            packetToReceive.getLength(), StandardCharsets.UTF_8);
    println("RECEIVED MESSAGE: " + s);

    return Op.SuccessfullyReceivedMessage;
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

    //TODO: Multicast

    try
    {
      ms = new MulticastSocket(DEFAULT_PORT);
      ms.setSoTimeout(1);
      ms.joinGroup(InetAddress.getByName("239.1.10.1"));
    }
    catch(IOException e)
    {
      e.printStackTrace();
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
              assert(client != null);
              result_2 = receiveSections(splitted, client, loggedInNickname);
              println("Result2 = " + result_2);
            }
            break;

          case "list" :
            result = handleList(client);
            println("Result = " + result);
            if(result == Op.SuccessfullyListed)
            {
              assert(client != null);
              result_2 = receiveList(client);
              println("Result2 = " + result_2);
            }
            break;

          case "edit" :
            result = handleEdit(splitted, client);
            println("Result = " + result);
            if(result == Op.SuccessfullyStartedEditing)
            {
              assert(client != null);
              result_2 = receiveSections(splitted, client, loggedInNickname);
              println("Result2 = " + result_2);
            }
            break;

          case "end-edit" :
            result = handleEndEdit(splitted, client);
            println("Result = " + result);
            if(result == Op.SuccessfullyEndedEditing)
            {
              assert(client != null);
              result_2 = sendSection(splitted, client, loggedInNickname);
              println("Result2 = " + result_2);
            }
            break;

          case "send" :
            splitted = stdin.split("\\s+", 2);
            result = handleSend(splitted, client, loggedInNickname);
            println("Result = " + result);
            break;

          case "receive" :
            result = handleReceive(client);
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
