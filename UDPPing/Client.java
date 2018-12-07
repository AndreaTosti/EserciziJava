package UDPPing;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;

public class Client
{
  private static int DEFAULT_PORT = -1;
  private static int BUFFER_SIZE = 2048;
  private static String HOST = null;

  public static void main(String[] args)
  {
    int port;
    InetAddress address;

    if(args.length == 2)
    {
      HOST = args[0];
      try
      {
        address = InetAddress.getByName(HOST);
      }
      catch(UnknownHostException e)
      {
        System.out.println("[CLIENT] ERR -arg 0");
        return;
      }
      try
      {
        port = Integer.parseInt(args[1]);
      }
      catch(NumberFormatException e)
      {
        System.out.println("[CLIENT] ERR -arg 1");
        return;
      }
      System.out.println("[CLIENT] Specified host: " + HOST);
      System.out.println("[CLIENT] Specified port: " + port);
    }
    else if(args.length == 1)
    {
      //Non ho passato il numero di porta
      System.out.println("[CLIENT] ERR -arg 1");
      return;
    }
    else
    {
      //Non ho passato host e numero di porta
      System.out.println("[CLIENT] ERR -arg 0");
      return;
    }

    try
    {
      SocketAddress serverSocket = new InetSocketAddress(HOST, port);
      DatagramSocket clientSocket = new DatagramSocket();

      for(int i = 0; i < 10; i++)
      {
        Long savedTime = System.currentTimeMillis();
        String stringa  = "PING " + i + " " + savedTime;
        byte[] buffer = stringa.getBytes("US-ASCII");
        DatagramPacket packetToSend = new DatagramPacket(buffer, buffer.length, serverSocket);
        clientSocket.setSoTimeout(2000);
        clientSocket.send(packetToSend);
        DatagramPacket packetToReceive = new DatagramPacket(buffer, buffer.length, serverSocket);
        long rtt = -1;
        String stringReceived = null;
        try
        {
          clientSocket.receive(packetToReceive);
          rtt = System.currentTimeMillis() - savedTime;
          stringReceived = new String(packetToReceive.getData(), 0,
                  packetToReceive.getLength(), "US-ASCII");
          //System.out.println("[CLIENT] Received " + packetToReceive.getLength() + " bytes ");
          System.out.println(stringReceived + " RTT: " + rtt + " ms");
        }
        catch(SocketTimeoutException e)
        {
          //System.out.println("[CLIENT] Timed out");
          System.out.println(stringa + " RTT: *");
        }
      }


    }
    catch(SocketException e1)
    {
      e1.printStackTrace();
    }
    catch(IOException e2)
    {
      e2.printStackTrace();
    }

  }

}
