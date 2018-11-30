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
  private static String sentFileName = null;

  public static void main(String[] args) throws InterruptedException
  {
    int port;
    if(args.length == 2)
    {
      sentFileName = args[0];
      port = Integer.parseInt(args[1]);
      System.out.println("[CLIENT] Specified filename: " + sentFileName);
      System.out.println("[CLIENT] Specified port: " + port);
    }
    else if(args.length == 1)
    {
      sentFileName = args[0];
      port = DEFAULT_PORT;
      System.out.println("[CLIENT] Specified filename: " + sentFileName);
      System.out.println("[CLIENT] No port specified, using default " +
              "port number " + DEFAULT_PORT);
    }
    else
    {
      System.err.print("[CLIENT] You must put the 2 following arguments : " +
              "filename followed by the port number");
      return;
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
        System.out.println("Connection refused on port " + port);
        System.out.println("Exiting...");
        return;
      }
      byte[] nomefile = sentFileName.getBytes();
      ByteBuffer buffer = ByteBuffer.wrap(nomefile);
      System.out.println("[CLIENT] Sending filename " + sentFileName);
      client.write(buffer);

      //Ricevo la dimensione del file
      int res = 0;
      ByteBuffer buffer2 = ByteBuffer.allocate(Long.BYTES);
      res = client.read(buffer2);
      int numFileBytes = Integer.valueOf(new String(buffer2.array(), 0, res, StandardCharsets.ISO_8859_1));
      System.out.println("[CLIENT] Received " + res + " bytes.");
      //Se il file esiste, ritorna il numero di bytes del file, altrimenti -1
      if(numFileBytes == -1)
      {
        System.out.println("[CLIENT] Received error (file " + sentFileName +
                " does not exists, errno: " + numFileBytes + ")");
        System.out.println("[CLIENT] Closing ...");
        client.close();
        return;
      }

      System.out.println("[CLIENT] Received filesize: " + numFileBytes + " bytes");
      buffer.clear();
      //Creo un nuovo file nella directory corrente con nome newNOMEFILE
      numBytesLetti = handleRead(client, numFileBytes, "new" + sentFileName);
      System.out.println("[CLIENT] Received " + numBytesLetti + "/" +
              numFileBytes + " bytes.");
      if(numFileBytes - Integer.parseInt(numBytesLetti) == 0)
      {
        System.out.println("[CLIENT] File received correctly, saved to filename: " +
                "new" + sentFileName);
      }
      byte[] numbytes = numBytesLetti.getBytes();
      buffer = ByteBuffer.wrap(numbytes);

      System.out.println("[CLIENT] Sending number of bytes read");
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
      System.out.println("[CLIENT] Reading from socket, it may take some time...");
      System.out.println("[CLIENT] Creating file " + path.toAbsolutePath());
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
      }while(numFileBytes > 0);
      fileChannel.close();
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
    return String.valueOf(counter);
  }
}