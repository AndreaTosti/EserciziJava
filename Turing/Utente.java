package Turing;

class Utente
{
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
