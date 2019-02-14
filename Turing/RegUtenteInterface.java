package Turing;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegUtenteInterface extends Remote
{
  //Registra un nuovo utente
  public void registerUser(String username, String password) throws RemoteException;

}
