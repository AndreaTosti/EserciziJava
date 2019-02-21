package Turing;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;

public class ReceivePacketsTask implements Runnable
{
  private static int DEFAULT_BUFFER_SIZE = 4096;
  private byte[] buffer;
  private LinkedBlockingQueue<DatagramPacket> receivedPacketsQueue;
  private EditingRoom editingRoom;

  ReceivePacketsTask(LinkedBlockingQueue<DatagramPacket> receivedPacketsQueue,
                     EditingRoom editingRoom)
  {
    this.receivedPacketsQueue = receivedPacketsQueue;
    this.editingRoom = editingRoom;
  }

  @Override
  public void run()
  {
    while(true)
    {
      try
      {
        buffer = new byte[DEFAULT_BUFFER_SIZE];
        DatagramPacket packetToReceive = new DatagramPacket(buffer, buffer.length);
        editingRoom.getMulticastSocket().receive(packetToReceive);
        receivedPacketsQueue.put(packetToReceive);
      }
      catch(IOException | InterruptedException e)
      {
        e.printStackTrace();
        System.exit(1);
      }
    }
  }
}
