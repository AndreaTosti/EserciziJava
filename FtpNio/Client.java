package FtpNio;

import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

public class Client
{

  private static int DEFAULT_PORT = 51811;
  private  static String DEFAULT_HOST = "localhost";

  public static void main(String[] args) throws InterruptedException
  {
    int port;
    try
    {
      port = Integer.parseInt(args[0]);
    }
    catch(RuntimeException ex)
    {
      port = DEFAULT_PORT;
    }
    try
    {
      SocketAddress address = new InetSocketAddress(port);
      SocketChannel client;
      try
      {
        client = SocketChannel.open(address);
      }
      catch(ConnectException e)
      {
        System.out.println("Connessione rifiutata");
        System.out.println("Uscita ...");
        return;
      }
      String name = new String("settings.jar");
      byte[] nomefile = name.getBytes();
      ByteBuffer buffer = ByteBuffer.wrap(nomefile);
      client.write(buffer);
      System.out.println("Invio del nomefile " + name);
      buffer.clear();
      Thread.sleep(2000);
      handleRead(client);
      client.close();
    }catch(IOException ex)
    {
      ex.printStackTrace();
    }
  }

  private static void handleRead(SocketChannel channel)
  {
    String outputfile = "SEEE.jar";
    int bufferSize = 10240;
    Path path = Paths.get(outputfile);
    try
    {
      FileChannel fileChannel = FileChannel.open(path, EnumSet.of(
              StandardOpenOption.CREATE,
              StandardOpenOption.TRUNCATE_EXISTING,
              StandardOpenOption.WRITE));
      ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
      int res = 0;
      int counter = 0;
      System.out.println("[CLIENT] Inizio lettura");
      /*while((res = channel.read(buffer)) != -1)
      {
        Thread.sleep(200);
      }
    */
      do
      {
        buffer.clear();
        res = channel.read(buffer);
        System.out.println(res);
        buffer.flip();
        if(res > 0)
        {
          fileChannel.write(buffer);
          counter += res;
        }
      }while(res >= 0);
      //channel.close();
      fileChannel.close();
      System.out.println("CLIENT: " + counter);
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
  }
}