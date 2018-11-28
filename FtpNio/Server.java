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
    while(true)
    {
      System.out.println("Nuova iterazione del select");
      try
      {
        assert selector != null;
        selector.select();
      }
      catch(IOException | NullPointerException e)
      {
        e.printStackTrace();
      }

      String receivedFileName = null; //Nome del file ricevuto
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
          if(key.isReadable())
          {
            System.out.println("[SERVER-Selector] key.isReadable");
            SocketChannel channel = (SocketChannel)key.channel();
            SelectionKey channelKey = channel.register(selector, SelectionKey.OP_WRITE, key.attachment());
            receivedFileName = handleRead(key);
          }
          if(key.isWritable())
          {
            System.out.println("[SERVER-Selector] key.isWritable");
            SocketChannel channel = (SocketChannel) key.channel();
            handleWrite(key);
            channel.close();
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

        //Path currentRelativePath = Paths.get(receivedFileName);
        //String s = currentRelativePath.toAbsolutePath().toString();

        //Path path = Paths.get(receivedFileName);
        //System.out.println(s);
        //fileChannel = FileChannel.open(currentRelativePath.toAbsolutePath(), EnumSet.of(
        //        StandardOpenOption.CREATE,
        //        StandardOpenOption.TRUNCATE_EXISTING,
        //        StandardOpenOption.WRITE));

        //if(res > 0)
        //{
          //fileChannel.write(buffer);
          //counter += res;
        //}
      //}while(res > 0);
      //channel.close();
      //fileChannel.close();
      //System.out.println("SERVER: " + counter);
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
    return receivedFileName;
  }

  private static void handleWrite(SelectionKey key)
  {
    //252 kb
    SocketChannel channel = (SocketChannel)key.channel();
    ByteBuffer buffer = (ByteBuffer) key.attachment();
    String fname = "settings.jar";
    Path path = Paths.get(fname);
    try
    {
      FileChannel fileChannel = FileChannel.open(path);
      //ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
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
      System.out.println("Inviati " + counter);
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
  }
}
