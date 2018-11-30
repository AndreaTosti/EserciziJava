package FtpNio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;

public class Server
{
  private static int DEFAULT_PORT = 51811; //Porta
  private static int BUFFER_SIZE  = 4096;   //Dimensione del buffer

  public static void main(String[] args)
  {
    int port;
    if(args.length == 0)
    {
      //Non ho passato il numero di porta
      port = DEFAULT_PORT;
      System.out.println("[SERVER] No arguments specified, using default " +
              "port number " + DEFAULT_PORT);
    }
    else if(args.length == 1)
    {
      //Ho passato il numero di porta
      port = Integer.parseInt(args[0]);
      System.out.println("[SERVER] Listening to chosen port number " + port);
    }
    else
    {
      System.err.print("[SERVER] Only one argument accepted : port number");
      return;
    }
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
    long fileSize = 0;              //Dimensione del file richiesto
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
            //Nuova richiesta di connessione
            System.out.println("[SERVER-Selector] key.isAcceptable");
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            SocketChannel client = server.accept();
            System.out.println("[SERVER] New connection from client " + client);
            client.configureBlocking(false);
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            client.register(selector, SelectionKey.OP_READ, buffer);
          }
          else if(key.isReadable())
          {
            //Nuovo evento in lettura
            System.out.println("[SERVER-Selector] key.isReadable");
            SocketChannel channel = (SocketChannel)key.channel();
            channel.register(selector, SelectionKey.OP_WRITE, key.attachment());

            //Se il nome del file non è mai stato ricevuto, allora leggilo,
            //altrimenti controlla che il numero di bytes letti dal client
            //sia effettivamente uguale alla dimensione in bytes del file spedito

            if(receivedFileName == null)
            {
              receivedFileName = handleRead(key);
            }
            else
            {
              ByteBuffer buffer = (ByteBuffer) key.attachment();
              int res;
              buffer.clear();
              res = channel.read(buffer);
              int messaggio = Integer.valueOf(new String(buffer.array(), 0, res, StandardCharsets.ISO_8859_1));
              if(messaggio - fileSize == 0)
              {
                System.out.println("[SERVER] File sent correctly (Sent: " + messaggio +
                        "/" + fileSize + " bytes)");
                System.out.println("[SERVER] Waiting for new client requests...");
                System.out.println();
                channel.close();
                receivedFileName = null;
              }
              else
              {
                System.out.println("[SERVER] Error during file transmission:");
                System.out.println("Sent: " + messaggio + "/" + fileSize + " bytes");
              }
            }
          }
          else if(key.isWritable())
          {
            //Nuovo evento in scrittura
            System.out.println("[SERVER-Selector] key.isWritable");
            SocketChannel channel = (SocketChannel) key.channel();
            fileSize = handleWrite(key, receivedFileName);
            if(key.isValid())
              key.interestOps(SelectionKey.OP_READ);
            else
              receivedFileName = null;
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
      receivedFileName = new String(buffer.array(), 0, res, StandardCharsets.ISO_8859_1);
      System.out.println("[SERVER] Received file name: " + receivedFileName);
      buffer.flip();
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
    return receivedFileName;
  }

  private static long handleWrite(SelectionKey key, String receivedFileName)
  {
    SocketChannel channel = (SocketChannel)key.channel();
    ByteBuffer buffer = (ByteBuffer) key.attachment();
    FileChannel fileChannel = null;
    long dimensioneFile = 0;
    Path path = Paths.get(receivedFileName);
    try
    {
      if(Files.notExists(path))
      {
        System.out.println("[SERVER] Filename " + receivedFileName +
                " does not exists, sending error to client");
        //Invio l'errore riempiendo di zeri (Padding) oltre a -1
        String numBytesStr = String.format("%0" + Long.BYTES + "d", -1);
        byte[] numBytes = numBytesStr.getBytes();
        buffer.clear();
        buffer =  ByteBuffer.wrap(numBytes);
        int sentBytes = channel.write(buffer);
        System.out.println("[SERVER] Sent " + sentBytes + " bytes.");
        channel.close();
        System.out.println("[SERVER] Waiting for new client requests...");
        System.out.println();
        return -1;
      }
      fileChannel = FileChannel.open(path);
      dimensioneFile = fileChannel.size();
      System.out.println("[SERVER] Reading from file " + path.toAbsolutePath());
      //Invio il numero di bytes del file riempiendo di zeri (Padding)
      String numBytesStr = String.format("%0" + Long.BYTES + "d", dimensioneFile);
      byte[] numBytes = numBytesStr.getBytes();
      buffer.clear();
      buffer =  ByteBuffer.wrap(numBytes);
      int sentBytes = channel.write(buffer);
      System.out.println("[SERVER] Sent " + sentBytes + " bytes.");
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
      System.out.println("[SERVER] Sent " + counter + " bytes.");
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
    return dimensioneFile;
  }
}
