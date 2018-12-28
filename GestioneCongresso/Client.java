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
    int DEFAULT_PORT = 51811; //Porta

    try
    {
      // Getting the registry
      Registry registry = LocateRegistry.getRegistry(DEFAULT_PORT);

      // Looking up the registry for the remote object
      Congresso stub = (Congresso) registry.lookup("Congresso");

      BufferedReader reader = new BufferedReader(new InputStreamReader(
              System.in));
      String readVal;
      System.out.println("[CLIENT] Digitare uno dei seguenti comandi (case insensitive) ");
      System.out.println("[CLIENT] considerando questi vincoli: ");
      System.out.println("[CLIENT] Sessioni disponibili: da 1 a " + stub.getNumSessioniPerGiorno());
      System.out.println("[CLIENT] Giorni disponibili: da 1 a " + stub.getNumGiornate());
      System.out.println("[CLIENT] Lunghezza nome dello speaker: MAX 17 caratteri");
      System.out.println("[CLIENT] --- Comandi ---");
      System.out.println("[CLIENT] stop : termina il processo client");
      System.out.println("[CLIENT] register X Y Z : registra lo speaker X alla sessione Y nel giorno Z");
      System.out.println("[CLIENT] program : restituisce il programma del congresso");
      try
      {
        while(!( readVal = reader.readLine() ).equalsIgnoreCase("stop"))
        {
          System.out.println("String read from console input:" + readVal);
          if(readVal.equalsIgnoreCase("PROGRAM"))
          {
            System.out.println(stub.getProgrammaCongresso());
          }
          else if(readVal.matches("register .*"))
          {
            String[] splitted = readVal.split("\\s+");
            String speakerName = splitted[1];
            int sessione = Integer.valueOf(splitted[2]);
            int giorno = Integer.valueOf(splitted[3]);
            System.out.println("Richiesta registrazione dello speaker " +
                    speakerName + " alla sessione S" + sessione +
                    " per il giorno " + giorno);
            System.out.println(stub.registerSpeaker(giorno, sessione, speakerName));
          }
        }
        reader.close();
      }catch(IOException e)
      {
        e.printStackTrace();
      }
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