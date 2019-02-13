package GestioneCongresso;

public class ImplCongresso implements Congresso
{
  //Numero di giorni del programma
  private int numGiornate;
  //Numero di sessioni per ogni giornata
  private int numSessioniPerGiorno;
  //Numero di speaker per ogni sessione
  private int numMaxSpeakerPerSessione;

  //Array di Matrici: a ogni giornata è associata una matrice di Speaker,
  //dove tale matrice contiene le sessioni sulle righe e gli interventi
  //sulle colonne
  private Speaker[][] vettoreGiornate[];

  ImplCongresso(int numGiornate, int numSessioniPerGiorno, int numMaxSpeakerPerSessione)
  {
    this.numGiornate = numGiornate;
    this.numSessioniPerGiorno = numSessioniPerGiorno;
    this.numMaxSpeakerPerSessione = numMaxSpeakerPerSessione;
    this.vettoreGiornate = new Speaker[numGiornate + 1][numSessioniPerGiorno + 1][numMaxSpeakerPerSessione + 1];

    for(int k = 1; k <= numGiornate; k++)
    {
      for(int i = 1; i <= numSessioniPerGiorno; i++)
      {
        for(int j = 1; j <= numMaxSpeakerPerSessione; j++)
        {
          vettoreGiornate[k][i][j] = new Speaker();
        }
      }
    }
  }

  public int getNumSessioniPerGiorno()
  {
    return numSessioniPerGiorno;
  }

  public int getNumGiornate()
  {
    return numGiornate;
  }

  public String getProgrammaCongresso()
  {
    //Costruisce, sottoforma di stringa, il programma completo dei vari interventi,
    //che verrà poi stampato a schermo da parte del client
    StringBuilder sb = new StringBuilder();
    sb.append("Programma del congresso\n");
    for(int k = 1; k <= numGiornate; k++)
    {
      sb.append("Giorno " + k + "\n");
      sb.append("Sessione\t\t");
      for(int j = 1; j <= numMaxSpeakerPerSessione; j++)
      {
        sb.append("Intervento " + j + "\t\t");
      }
      sb.append("\n");

      for(int i = 1; i <= numSessioniPerGiorno; i++)
      {
        sb.append("S" + i + "\t\t\t\t");
        for(int j = 1; j <= numMaxSpeakerPerSessione; j++)
        {
          sb.append(String.format("%-20s", vettoreGiornate[k][i][j].getNome()));
        }
        sb.append("\n");
      }
      sb.append("--------\t\t");
      for(int j = 1; j <= numMaxSpeakerPerSessione; j++)
      {
        sb.append(String.format("%-20s", "-----------------"));
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  public String registerSpeaker(int giorno, int sessione, String nomeSpeaker)
  {
    //Permette di registrare uno speaker specificando il giorno, la sessione e
    //il nome dello speaker; se è possibile fare un intervento per la sessione
    //scelta (esiste uno slot libero) allora viene aggiunto tale intervento,
    //altrimenti registituisce un errore al client

    if(giorno > numGiornate || giorno < 1)
      return "Errore: num giorni non rispetta i vincoli";
    if(sessione > numSessioniPerGiorno || sessione < 1)
      return "Errore: num sessione non rispetta i vincoli";
    if(nomeSpeaker.length() > 17)
      return "Errore: nome speaker troppo lungo";

    //Controllo se ci sono slot liberi per effettuare un intervento nella
    //sessione scelta e nel giorno scelto
    int available = 0;
    int j = 1;
    while(j <= numMaxSpeakerPerSessione)
    {
      if(this.vettoreGiornate[giorno][sessione][j].getNome().equals(""))
      {
        available = j;
        break;
      }
      j++;
    }
    if(available == 0)
      return "Errore: sono stati già coperti tutti gli spazi di intervento";

    vettoreGiornate[giorno][sessione][available].setNome(nomeSpeaker);
    return "Success: è stato registrato l'intervento per il giorno " +
            giorno + " alla sessione S" + sessione +
            " dello speaker " +
            vettoreGiornate[giorno][sessione][available].getNome();
  }
}
