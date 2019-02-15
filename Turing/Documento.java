package Turing;

public class Documento
{
  private String nome;
  private Sezione[] sezioni;
  private int numSezioni;
  private String nomeCreatore;

  Documento(String nome, int numSezioni, String nomeCreatore)
  {
    this.nome = nome;
    this.numSezioni = numSezioni;
    this.nomeCreatore = nomeCreatore;

    this.sezioni = new Sezione[numSezioni];
    for(int i = 0; i < numSezioni; i++)
    {
      this.sezioni[i] = new Sezione();
    }
  }

}