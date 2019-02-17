package Turing;


import java.util.HashMap;
import java.util.Map;

class Documento
{
  private String nome;
  private Sezione[] sezioni;
  private int numSezioni;
  private Utente creatore;
  private Map<String, Utente> collaborators;

  Documento(String nome, int numSezioni, Utente creatore, Sezione[] sezioni)
  {
    this.nome = nome;
    this.numSezioni = numSezioni;
    this.creatore = creatore;
    this.sezioni = sezioni;
    this.collaborators = new HashMap<>();
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

  Sezione[] getSezioni()
  {
    return sezioni;
  }

}