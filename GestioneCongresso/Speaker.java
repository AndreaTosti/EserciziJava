package GestioneCongresso;

public class Speaker
{
  private String nome;

  Speaker()
  {
    this.nome = null;
  }

  String getNome()
  {
    if(nome == null)
      return "";
    else
      return nome;
  }

  void setNome(String nome)
  {
    this.nome = nome;
  }

}
