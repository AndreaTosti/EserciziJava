package Turing;

public class Sessione
{
  private Utente utente;
  private Stato stato;

  enum Stato
  {
    Started,
    Logged,
    Editing
  }

  Sessione()
  {
    this.stato = Stato.Started;
  }

  Utente getUtente()
  {
    return utente;
  }

  void setUtente(Utente utente)
  {
    this.utente = utente;
  }

  void setStato(Stato stato)
  {
    this.stato = stato;
  }

}
