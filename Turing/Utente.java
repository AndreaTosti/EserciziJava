package Turing;

class Utente
{
  @Override
  public boolean equals(Object obj)
  {
    if(obj == null)
      return false;
    if(!(obj instanceof Utente))
      return false;
    return ((Utente)obj).getNickname().compareTo(nickname) == 0;
  }

  private final String nickname;
  private final String password;

  Utente(String nickname, String password)
  {
    this.nickname = nickname;
    this.password = password;
  }

  String getNickname()
  {
    return nickname;
  }

  String getPassword()
  {
    return password;
  }
}
