package UDPMulticast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

public class TimeServer
{
  public static void main(String[] args)
  {
    InetAddress ia = null;
    int port = 0;
    byte ttl = (byte) 64;  //All sites on the same continent

    if(args.length == 2)
    {
      try
      {
        ia = InetAddress.getByName(args[0]);
        port = Integer.parseInt(args[1]);
      }
      catch(NumberFormatException | IndexOutOfBoundsException | UnknownHostException e)
      {
        System.out.println("[SERVER] Usage: java TimeServer multicast_address port");
        return;
      }
    }
    else
    {
      System.out.println("[SERVER] You must insert the following 2 arguments: " +
              "dategroup ip address and port number");
      System.out.println("[SERVER] Usage: java TimeServer multicast_address port");
      return;
    }
    System.out.println("[SERVER] Using: multicast_address: " + ia + " port: " + port);
    String readline = "";
    try(MulticastSocket ms = new MulticastSocket())
    {
      ms.setTimeToLive(ttl);
      ms.joinGroup(ia);
      String time = null;
      System.out.println("[SERVER] Sending Date and Hour every second from now");
      while(true)
      {
        time = LocalDateTime.now().toString();
        System.out.println(time);
        DatagramPacket packetToSend = new DatagramPacket(
                time.getBytes("UTF-8"),
                time.getBytes("UTF-8").length,
                ia, port);
        ms.send(packetToSend);
        Thread.sleep(1000);
      }
    }
    catch(IOException | InterruptedException e)
    {
      e.printStackTrace();
    }
  }
}
