package Turing;

public enum Op
{
  SuccessfullyRegistered,     // Registrazione avvenuta con successo
  SuccessfullyLoggedIn,       // Login avvenuto con successo
  SuccessfullyLoggedOut,      // Logout avvenuto con successo
  WrongPassword,              // Password non corretta
  NicknameDoesNotExists,      // L'utente non esiste
  NicknameAlreadyExists,      // Esiste già l'utente
  Login,                      // Operazione di Login TCP
  Logout,                     // Operazione di Logout TCP
  Error,                      // Errore non ben specificato
  UsageError,                 // Passaggio parametri sbagliato
  ClosedConnection,           // Il server non È più raggiungibile
  UnknownSession,             // Sessione inesistente
  AlreadyLoggedIn,            // L'utente è già loggato
  CannotLogout,               // L'utente è nello stato Started o Editing

}
