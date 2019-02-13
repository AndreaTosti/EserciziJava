package GestioneCongresso;

import java.rmi.Remote;
import java.rmi.RemoteException;

// Creating Remote interface for our application
public interface Congresso extends Remote
{

  //Per ottenere il programma del congresso
  public String getProgrammaCongresso() throws RemoteException;

  //Per ottenere il numero di sessioni per giorno
  public int getNumSessioniPerGiorno() throws RemoteException;

  //Per ottenere il numero di giornate del programma
  public int getNumGiornate() throws  RemoteException;

  //Per registrare uno speaker a una sessione in un determinato giorno
  public String registerSpeaker(int giorno, int sessione, String nomeSpeaker) throws RemoteException;
}
