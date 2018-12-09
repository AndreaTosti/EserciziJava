package UDPMulticast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class TimeClient
{
  public static void main(String[] args)
  {
    InetAddress group = null;
    int port = 0;

    if(args.length == 2)
    {
      try
      {
        group = InetAddress.getByName(args[0]);
        port = Integer.parseInt(args[1]);
      }
      catch(NumberFormatException | IndexOutOfBoundsException | UnknownHostException e)
      {
        System.out.println("[CLIENT] Usage: java TimeClient multicast_address port");
        return;
      }
    }
    else
    {
      System.out.println("[CLIENT] You must insert the following 2 arguments: " +
              "dategroup ip address and port number");
      System.out.println("[CLIENT] Usage: java TimeClient multicast_address port");
      return;
    }
    System.out.println("[CLIENT] Using: multicast_address: " + group + " port: " + port);
    MulticastSocket ms = null;
    try
    {
      ms = new MulticastSocket(port);
      ms.joinGroup(group);
      byte[] buffer = new byte[8192];
      int counter = 0;
      do
      {
        DatagramPacket packetToReceive = new DatagramPacket(buffer, buffer.length);
        ms.receive(packetToReceive);
        String s = new String(packetToReceive.getData(), 0,
                packetToReceive.getLength(), "UTF-8");
        System.out.println(s);
        counter++;
      }while(counter < 10);
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
    finally
    {
      if(ms != null)
      {
        try
        {
          ms.leaveGroup(group);
          ms.close();
        }
        catch(IOException e)
        {
        }
      }
    }
  }
}
