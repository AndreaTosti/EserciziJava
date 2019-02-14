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

  private static Pattern validPattern = Pattern.compile("[A-Za-z0-9_]+");

  private static void println(String string)
  {
    System.out.println("[Client] " + string);
  }

  private static void printErr(String string)
  {
    System.err.println("[Client-Error] " + string);
  }

  private static boolean isAValidString(String string)
  {
    return validPattern.matcher(string).matches();
  }

  private static void handleRegister(String[] splitted)
  {
    //Controllo il numero di parametri
    if(splitted.length != 3)
    {
      printErr("Usage: register <username> <password>");
      return;
    }

    String username = splitted[1];
    String password = splitted[2];

    if(!isAValidString(username) || !isAValidString(password))
    {
      printErr("Usage: register <username> <password>");
      return;
    }
    println("REGISTERING User " + username + " with password " +
            password);

    //REGISTRAZIONE RMI
    try
    {
      Registry registry = LocateRegistry.getRegistry(DEFAULT_HOST, DEFAULT_RMI_PORT);
      RegUtenteInterface stub = (RegUtenteInterface) registry.lookup("RegUtente");
      Op result = stub.registerUser(username, password);
      if(result == Op.SuccessfullyRegistered)
      {
        println("Registrazione eseguita con successo");
      }
      else if(result == Op.Error)
      {
        printErr("Registrazione non andata a buon fine");
      }
    }
    catch(Exception e)
    {
      printErr("exception: " + e.toString());
      e.printStackTrace();
    }
  }

  private static void handleLogin(String[] splitted, SocketChannel client)
  {
    //Controllo il numero di parametri
    if(splitted.length != 3)
    {
      printErr("Usage: login <username> <password>");
      return;
    }

    String username = splitted[1];
    String password = splitted[2];

    println("LOGGING User " + username + " with password " +
            password);

    //LOGIN TCP
    //login#username#password

    StringJoiner joiner = new StringJoiner(DEFAULT_DELIMITER);
    joiner.add(Op.Login.toString()).add(username).add(password);
    String joinedString = joiner.toString();

    byte[] operation = joinedString.getBytes();
    ByteBuffer buffer = ByteBuffer.wrap(operation);

    try
    {
      client.write(buffer);

      //TODO: da migliorare buffer e dimensione
      buffer = ByteBuffer.allocate(4096);

      //Leggi l'esito
      int res;
//      buffer.flip();
      res = client.read(buffer);
      //TODO: gestire entrambi i casi
      if(res < 0)
      {
        //Il server è stato chiuso?
        printErr("Res < 0");
      }
      else
      {
        buffer.flip();
        String result = new String(buffer.array(), 0,
                res, StandardCharsets.ISO_8859_1);
        println("RESPONSE: " + result);
      }
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }

  }

  private static void handleLogout()
  {
    println("LOGOUT!");
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

    BufferedReader reader = new BufferedReader(new InputStreamReader(
            System.in));
    String stdin;
    boolean stopped = false;

    try
    {
      //Input da linea di comando
      while(!stopped)
      {
        stdin = reader.readLine();
        String[] splitted = stdin.split("\\s+");

        switch(splitted[0].toLowerCase())
        {
          case "register" :
            handleRegister(splitted);
            break;
          case "login" :
            handleLogin(splitted, client);
            break;
          case "logout" :
            handleLogout();
            stopped = true;
            break;
          default:
            printErr("Comando non riconosciuto");
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
