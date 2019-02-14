package Turing;

import java.util.concurrent.ConcurrentMap;

public class RegUtenteImplementation implements RegUtenteInterface
{
  private ConcurrentMap<String, Utente> users;

  RegUtenteImplementation(ConcurrentMap<String, Utente> users)
  {
    this.users = users;
  }

  public Op registerUser(String username, String password)
  {
    System.out.println("registerUser called with parameters " + username + " " + password);
    if(users.putIfAbsent(username,new Utente(username, password)) != null)
    {
      //L'utente esiste già
      return Op.Error;
    }
    else
    {
      //Creato un nuovo utente
      return Op.SuccessfullyRegistered;
    }
  }
}
