package GestioneCongresso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.ConnectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client
{
  private Client()
  {

  }

  public static void main(String[] args)
  {
    int DEFAULT_PORT = 3000; //Porta

    try
    {
      // Getting the registry
      Registry registry = LocateRegistry.getRegistry(DEFAULT_PORT);

      // Looking up the registry for the remote object
      Congresso stub = (Congresso) registry.lookup("Congresso");

      // Calling the remote method using the obtained object
      BufferedReader reader = new BufferedReader(new InputStreamReader(
              System.in));
      String readVal;
      System.out.println("[CLIENT] Digitare uno dei seguenti (case insensitive) ");
      System.out.println("[CLIENT] considerando questi vincoli: ");
      System.out.println("[CLIENT] Sessioni disponibili: 1 a " + stub.);
      System.out.println("[CLIENT] stop : termina il processo client");
      System.out.println("[CLIENT] register X Y : registra lo speaker X alla sessione Y");
      // catch the possible IOException by the readLine() method
      try {
        while(!( readVal = reader.readLine() ).equalsIgnoreCase("stop"))
        {
          // print the text read by the BufferedReader
          System.out.println("String read from console input:" + readVal);
          // close the BufferedReader object
        }
        reader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }


      System.out.println(stub.getProgrammaCongresso());
    }
    catch(ConnectException ex)
    {
      System.err.println("Connessione rifiutata su localhost");
      //ex.printStackTrace();
    }
    catch(Exception e)
    {
      System.err.println("Client exception: " + e.toString());
      e.printStackTrace();
    }
  }
}