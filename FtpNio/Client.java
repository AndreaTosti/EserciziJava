package FtpNio;

import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.io.IOException;
import java.nio.charset.Charset;

public class Client
{

  private static int DEFAULT_PORT = 51811;
  private  static String DEFAULT_HOST = "localhost";

  public static void main(String[] args) throws InterruptedException
  {
    if(args.length != 1)
    {
      System.out.println("Usage: java Client host [port]");
      return;
    }

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
      SocketAddress address = new InetSocketAddress(DEFAULT_HOST, port);
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
      String nomefile = "file.txt";
      Charset charset = Charset.forName("UTF-8");
      CharBuffer charBuffer = CharBuffer.wrap(nomefile);
      ByteBuffer buffer = charset.encode(charBuffer);
      buffer.compact();
      buffer.flip();
      int numWrittenBytes = client.write(buffer);
      System.out.println(numWrittenBytes);
      client.close();
      /*ByteBuffer buffer  = ByteBuffer.allocate(1024);
      client.read(buffer);
      System.out.println("Arrivata la stringa");
      buffer.flip();
      CharBuffer c = Charset.forName("UTF-8").decode(buffer);
      System.out.println(c);*/
    }catch(IOException ex)
    {
      ex.printStackTrace();
    }
  }
}