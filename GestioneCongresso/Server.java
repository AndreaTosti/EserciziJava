import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class Server
{
  public Server()
  {

  }

  public static void main(String args[])
  {
    int DEFAULT_PORT = 51811; //Porta
    int DEFAULT_NUMGIORNATE = 3;
    int DEFAULT_NUMSESSIONIPERGIORNO = 12;
    int DEFAULT_NUMMAXSPEAKERPERSESSIONE = 5;

    try
    {
      // Instantiating the implementation class
      ImplCongresso obj = new ImplCongresso(
              DEFAULT_NUMGIORNATE,
              DEFAULT_NUMSESSIONIPERGIORNO,
              DEFAULT_NUMMAXSPEAKERPERSESSIONE);

      // Exporting the object of implementation class
      // (here we are exporting the remote object to the stub)
      Congresso stub = (Congresso) UnicastRemoteObject.exportObject(obj, 0);

      // Binding the remote object (stub) in the registry
      Registry registry = LocateRegistry.createRegistry(DEFAULT_PORT);
      //Registry registry = LocateRegistry.getRegistry();

      registry.bind("Congresso", stub);
      System.err.println("Server ready");
    }
    catch(Exception e)
    {
      System.err.println("Server exception: " + e.toString());
      e.printStackTrace();
    }
  }
} 