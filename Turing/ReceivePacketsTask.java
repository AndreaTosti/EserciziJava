package Turing;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;
import java.util.concurrent.LinkedBlockingQueue;

public class ReceivePacketsTask implements Runnable
{
  private static int DEFAULT_BUFFER_SIZE = 4096;
  private byte[] buffer;
  private LinkedBlockingQueue<DatagramPacket> receivedPacketsQueue;
  private EditingRoom editingRoom;
  private PauseControl pauseControl;


  ReceivePacketsTask(LinkedBlockingQueue<DatagramPacket> receivedPacketsQueue,
                     EditingRoom editingRoom, PauseControl pauseControl)
  {
    this.receivedPacketsQueue = receivedPacketsQueue;
    this.editingRoom = editingRoom;
    this.pauseControl = pauseControl;
  }

  @Override
  public void run()
  {
    while(true)
    {
      try
      {
        pauseControl.pausePoint();
        buffer = new byte[DEFAULT_BUFFER_SIZE];
        DatagramPacket packetToReceive = new DatagramPacket(buffer, buffer.length);
        editingRoom.getMulticastSocket().receive(packetToReceive);
        receivedPacketsQueue.put(packetToReceive);
      }
      catch(SocketTimeoutException ignored){}
      catch(IOException | InterruptedException e2)
      {
        e2.printStackTrace();
        System.exit(1);
      }
    }
  }
}
