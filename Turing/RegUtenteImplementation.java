package Turing;

import java.rmi.RemoteException;

public class RegUtenteImplementation implements  RegUtenteInterface
{
  public void registerUser(String username, String password)
  {
    System.out.println("registerUser called with parameters " + username + " " + password);
  }
}
