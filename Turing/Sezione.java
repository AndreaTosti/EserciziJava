package Turing;

class Sezione
{
  private Utente editingUser;
  private String nome;

  Sezione(String nome)
  {
    this.editingUser = null;
    this.nome = nome;
  }

  Utente getUserEditing()
  {
    return editingUser;
  }

  void edit(Utente utente)
  {
    editingUser = utente;
  }

  void endEdit()
  {
    editingUser = null;
  }

  String getNome()
  {
    return nome;
  }

}
