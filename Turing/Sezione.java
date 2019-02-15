package Turing;

class Sezione
{
  private Utente editingUser;

  Sezione()
  {
    this.editingUser = null;
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

}
