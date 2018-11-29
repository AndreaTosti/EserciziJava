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

import static java.lang.System.exit;

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
      ByteBuffer buffer2 = ByteBuffer.allocate(Long.BYTES);
      buffer.clear();
      res = client.read(buffer2);
      int numFileBytes = Integer.valueOf(new String(buffer2.array(), 0, res, StandardCharsets.ISO_8859_1));
      //long numFileBytes = buffer2.getLong();
      System.out.println("[CLIENT] Dimensione del file : " + numFileBytes);

      numBytesLetti = handleRead(client, numFileBytes, "new" + sentFileName);
      System.out.println("[CLIENT] Attesi: " + numFileBytes +
              " Ricevuti: " + numBytesLetti);
      byte[] numbytes = numBytesLetti.getBytes();
      buffer = ByteBuffer.wrap(numbytes);

      System.out.println("[CLIENT] Invio la richiesta di terminazione");
      client.write(buffer);
      client.close();
    }catch(IOException ex)
    {
      ex.printStackTrace();
    }
  }

  private static String handleRead(SocketChannel channel, long numFileBytes, String sentFileName)
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
        numFileBytes -= res;
        System.out.println(numFileBytes);
      }while(numFileBytes > 0);
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