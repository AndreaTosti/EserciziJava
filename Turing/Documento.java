package Turing;


import java.util.HashMap;
import java.util.Map;

public class Documento
{
  private String nome;
  private Sezione[] sezioni;
  private int numSezioni;
  private Utente creatore;
  private Map<String, Utente> collaborators;

  Documento(String nome, int numSezioni, Utente creatore)
  {
    this.nome = nome;
    this.numSezioni = numSezioni;
    this.creatore = creatore;
    this.sezioni = new Sezione[numSezioni];
    this.collaborators = new HashMap<>();

    for(int i = 0; i < numSezioni; i++)
    {
      this.sezioni[i] = new Sezione();
    }

  }

  Utente getCreatore()
  {
    return creatore;
  }

  boolean isCollaboratore(Utente utente)
  {
    return collaborators.containsKey(utente.getNickname());
  }

  void addCollaboratore(Utente collaboratore)
  {
    collaborators.put(collaboratore.getNickname(), collaboratore);
  }

}