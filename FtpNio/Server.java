package FtpNio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

public class Server
{
  private static int DEFAULT_PORT = 51811;
  private static int BUFFER_SIZE = 4096;

  public static void main(String[] args)
  {
    int port;
    try
    {
      port = Integer.parseInt(args[0]);
    }
    catch(RuntimeException e)
    {
      port = DEFAULT_PORT;
    }
    System.out.println("Il server è ora in ascolto sulla porta " + port);
    ServerSocketChannel serverChannel;
    Selector selector = null;
    try
    {
      serverChannel = ServerSocketChannel.open();
      ServerSocket ss = serverChannel.socket();
      InetSocketAddress address = new InetSocketAddress(port);
      ss.bind(address);
      serverChannel.configureBlocking(false);
      selector = Selector.open();
      serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
    String receivedFileName = null; //Nome del file ricevuto
    while(true)
    {
      try
      {
        assert selector != null;
        selector.select();
      }
      catch(IOException | NullPointerException e)
      {
        e.printStackTrace();
      }
      Set<SelectionKey> readyKeys = selector.selectedKeys();
      Iterator<SelectionKey> iterator = readyKeys.iterator();
      while(iterator.hasNext())
      {
        SelectionKey key = iterator.next();
        iterator.remove();
        try
        {
          if(key.isAcceptable())
          {
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            SocketChannel client = server.accept();
            System.out.println("[SERVER] Nuova connessione da parte dal client " + client);
            client.configureBlocking(false);
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            client.register(selector, SelectionKey.OP_READ, buffer);
          }
          else if(key.isReadable())
          {
            System.out.println("[SERVER-Selector] key.isReadable");
            SocketChannel channel = (SocketChannel)key.channel();
            channel.register(selector, SelectionKey.OP_WRITE, key.attachment());

            //Se il nome del file non è mai stato ricevuto..
            if(receivedFileName == null)
            {
              receivedFileName = handleRead(key);
            }
            else
            {
              System.out.println("[SERVER] Controllo il numero di byte");
              ByteBuffer buffer = ByteBuffer.allocate(10000);
              int res;
              buffer.clear();
              res = channel.read(buffer);
              System.out.println(res);
              return;
              //buffer.flip();
              //System.out.println(new String(buffer.array(), 0, res, StandardCharsets.UTF_8));
              /*if(numFileBytes > 0)
              {
                System.out.println("[SERVER] Richiesta di terminazione ricevuta");
                key.cancel();
                channel.close();
              }
              else
              {
                System.out.println("Errore.");
              }*/
            }
          }
          else if(key.isWritable())
          {
            System.out.println("[SERVER-Selector] key.isWritable");
            SocketChannel channel = (SocketChannel) key.channel();
            handleWrite(key, receivedFileName);
            key.interestOps(SelectionKey.OP_READ);
            //((ByteBuffer)key.attachment()).flip();
            //channel.register(selector, SelectionKey.OP_READ, key.attachment());
            //channel.close();
          }
        }
        catch(IOException e)
        {
          key.cancel();
          try
          {
            key.channel().close();
          }
          catch(IOException ex)
          {
            ex.printStackTrace();
          }
        }
      }
    }
  }

  private static String handleRead(SelectionKey key)
  {
    SocketChannel channel = (SocketChannel)key.channel();
    ByteBuffer buffer = (ByteBuffer) key.attachment();
    String receivedFileName = null;
    FileChannel fileChannel = null;
    try
    {

      int res = 0;
        buffer.clear();
        res = channel.read(buffer);
        receivedFileName = new String(buffer.array(), 0, res, StandardCharsets.UTF_8);
        System.out.println("[SERVER] Nome del file ricevuto: " + receivedFileName);
        buffer.flip();
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
    return receivedFileName;
  }

  private static void handleWrite(SelectionKey key, String receivedFileName)
  {
    SocketChannel channel = (SocketChannel)key.channel();
    ByteBuffer buffer = (ByteBuffer) key.attachment();
    Path path = Paths.get(receivedFileName);
    try
    {
      FileChannel fileChannel = FileChannel.open(path);

      //Invio il numero di bytes del file
      String numBytesStr = String.valueOf(fileChannel.size());
      byte[] numBytes = numBytesStr.getBytes();
      buffer.clear();
      buffer =  ByteBuffer.wrap(numBytes);
      //buffer.flip();
      System.out.println(channel.write(buffer));
      buffer.flip();

      int noOfBytesRead = 0;
      int counter = 0;
      do
      {
        noOfBytesRead = fileChannel.read(buffer);
        if(noOfBytesRead <= 0)
          break;
        counter += noOfBytesRead;
        buffer.flip();
        do
        {
          noOfBytesRead -= channel.write(buffer);
        }while(noOfBytesRead > 0);
        buffer.clear();
      }while(true);
      fileChannel.close();
      System.out.println("[SERVER] inviati " + counter + " bytes.");
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
  }
}
