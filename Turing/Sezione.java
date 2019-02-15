package Turing;

public class Sezione
{
  private boolean editing;

  Sezione()
  {
    this.editing = false;
  }

  boolean isBeingEdited()
  {
    return editing;
  }

  void edit()
  {
    editing = true;
  }

  void endEdit()
  {
    editing = false;
  }

}
