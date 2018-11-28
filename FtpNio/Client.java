package FtpNio;

import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

public class Client
{

  private static int DEFAULT_PORT = 51811;
  private static int BUFFER_SIZE = 2048;
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
      String numBytesLetti = null;
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
      String sentFileName = new String("settings.jar");
      byte[] nomefile = sentFileName.getBytes();
      ByteBuffer buffer = ByteBuffer.wrap(nomefile);
      System.out.println("[CLIENT] Invio del nomefile " + sentFileName);
      client.write(buffer);

      //Ricevo la dimensione del file
      int res = 0;
      buffer.clear();
      res = client.read(buffer);
      int numFileBytes = Integer.valueOf(new String(buffer.array(), 0, res, StandardCharsets.UTF_8));
      System.out.println("[CLIENT] Dimensione del file : " + numFileBytes);
      buffer.flip();

      numBytesLetti = handleRead(client, "new" + sentFileName);
      System.out.println("[CLIENT] Attesi: " + numFileBytes +
              " Ricevuti: " + numBytesLetti);
      byte[] numbytes = numBytesLetti.getBytes();
      buffer.clear();
      buffer = ByteBuffer.wrap(numbytes);
      buffer.flip();
      System.out.println("[CLIENT] Invio la richiesta di terminazione");
      client.write(buffer);
      client.close();
    }catch(IOException ex)
    {
      ex.printStackTrace();
    }
  }

  private static String handleRead(SocketChannel channel, String sentFileName)
  {
    ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
    Path path = Paths.get(sentFileName);
    int counter = 0;
    try
    {
      FileChannel fileChannel = FileChannel.open(path, EnumSet.of(
              StandardOpenOption.CREATE,
              StandardOpenOption.TRUNCATE_EXISTING,
              StandardOpenOption.WRITE));
      int res = 0;
      System.out.println("[CLIENT] Inizio lettura");
      do
      {
        buffer.clear();
        res = channel.read(buffer);
        buffer.flip();
        if(res > 0)
        {
          fileChannel.write(buffer);
          counter += res;
        }
      }while(res >= 0);
      fileChannel.close();
      System.out.println("[CLIENT] Letti " + counter + " bytes.");
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
    return String.valueOf(counter);
  }
}