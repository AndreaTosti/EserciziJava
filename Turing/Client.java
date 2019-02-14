package Turing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.StringJoiner;

public class Client
{
  private static String DEFAULT_HOST = "localhost";
  private static int DEFAULT_PORT = 51811; //Porta di Default
  private static int DEFAULT_RMI_PORT = 51812; //Porta RMI
  private static String DEFAULT_DELIMITER = "#";

  public static void main(String[] args)
  {
    //REGISTRAZIONE RMI
    try
    {
      Registry registry = LocateRegistry.getRegistry(DEFAULT_HOST, DEFAULT_RMI_PORT);
      RegUtenteInterface stub = (RegUtenteInterface) registry.lookup("RegUtente");
      stub.registerUser();
    }
    catch(Exception e)
    {
      System.err.println("Client exception: " + e.toString());
      e.printStackTrace();
    }
    SocketAddress address = new InetSocketAddress(DEFAULT_HOST, DEFAULT_PORT);
    SocketChannel client = null;
    try
    {
      client = SocketChannel.open(address);
    }
    catch(ConnectException e1)
    {
      System.out.println("Connection refused on port " + DEFAULT_PORT);
      System.out.println("Exiting...");
      return;
    }
    catch(IOException e2)
    {
      e2.printStackTrace();
    }

    BufferedReader reader = new BufferedReader(new InputStreamReader(
            System.in));
    String stdin;

    try
    {
      //Input da linea di comando
      while(true)
      {
        stdin = reader.readLine();
        String[] splitted = stdin.split("\\s+");


        switch(splitted[0].toLowerCase())
        {
          case "register":
            System.out.println("REGISTER!");
            break;
          case "login":
            System.out.println("LOGIN!");
            break;
          default:
            System.out.println("[Client] Comando non riconosciuto");
        }
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }


//      StringJoiner joiner = new StringJoiner(DEFAULT_DELIMITER);
//      joiner.add(Op.Login.toString()).add("Andrea").add("Tosti");
//      String joinedString = joiner.toString();
//
//      byte[] operation = joinedString.getBytes();
//      ByteBuffer buffer = ByteBuffer.wrap(operation);
//      System.out.println("[CLIENT] Sending Operation " + Op.Login.toString());
//      client.write(buffer);
//    }

  }

}
