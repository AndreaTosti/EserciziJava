package UDPPing;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.security.SecureRandom;

public class Server
{
  private static int DEFAULT_PORT = -1; //Porta
  private static int BUFFER_SIZE  = 4096;   //Dimensione del buffer
  public static void main(String[] args)
  {
    int port;
    if(args.length == 1)
    {
      //Ho passato il numero di porta
      try
      {
        port = Integer.parseInt(args[0]);
      }
      catch(NumberFormatException e)
      {
        System.out.println("[SERVER] ERR -arg 0");
        return;
      }
      System.out.println("[SERVER] Listening to chosen port number " + port);
    }
    else
    {
      //Non ho passato il numero di porta
      System.out.println("[SERVER] ERR -arg 0");
      return;
    }

    double lossProbability = 0.25; //da 0 a 1, 0.25 = 25%
    double lossIndex = 0;

    try
    {
      DatagramSocket serverSocket = new DatagramSocket(port);
      byte[] buffer = new byte[BUFFER_SIZE];
      DatagramPacket packetToReceive = new DatagramPacket(buffer, buffer.length);
      SecureRandom randomNumbers = new SecureRandom();

      while(true)
      {
        packetToReceive.setLength(BUFFER_SIZE);
        serverSocket.receive(packetToReceive);
        String stringReceived = new String(packetToReceive.getData(), 0,
                packetToReceive.getLength(), "US-ASCII");
        //System.out.println("[SERVER] Received " + packetToReceive.getLength() + " bytes ");
        if(lossIndex + lossProbability >= 1)
        {
          //Il pacchetto è andato perso
          System.out.println("127.0.0.1> " + stringReceived + " ACTION: not sent");
          lossIndex = 0;
          continue;
        }
        else
        {
          //Il pacchetto non è andato perso
          int randomValue = randomNumbers.nextInt(500);
          System.out.println("127.0.0.1> " + stringReceived + " ACTION: delayed " + randomValue + " ms");
          try
          {
            Thread.sleep(randomValue);
          }catch(InterruptedException e)
          {
            e.printStackTrace();
          }
        }
        lossIndex = lossIndex + lossProbability;
        DatagramPacket packetToSend = new DatagramPacket(packetToReceive.getData(),
                packetToReceive.getLength(), packetToReceive.getSocketAddress());
        serverSocket.send(packetToSend);
      }
    }
    catch(IOException e1)
    {
      e1.printStackTrace();
    }

  }
}
