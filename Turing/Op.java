package Turing;

public enum Op
{
  SuccessfullyRegistered,     // Registrazione avvenuta con successo
  SuccessfullyLoggedIn,       // Login avvenuto con successo
  SuccessfullyLoggedOut,      // Logout avvenuto con successo
  SuccessfullyCreated,        // Creazione documento avvenuta con successo
  SuccessfullyShared,         // Condivisione documento avvenuta con successo
  WrongPassword,              // Password non corretta
  UserDoesNotExists,          // L'utente non esiste
  NicknameAlreadyExists,      // Esiste già l'utente
  DocumentAlreadyExists,      // Esiste già il documento
  NotDocumentCreator,         // L'utente non è il creatore del documento
  Login,                      // Operazione di Login TCP
  Logout,                     // Operazione di Logout TCP
  Create,                     // Operazione di creazione di un documento
  Share,                      // Operazione di condivisione di un documento
  Error,                      // Errore non ben specificato
  UsageError,                 // Passaggio parametri sbagliato
  ClosedConnection,           // Il server non È più raggiungibile
  UnknownSession,             // Sessione inesistente
  AlreadyLoggedIn,            // L'utente è già loggato
  CannotLogout,               // L'utente è nello stato Started o Editing
  SuccessfullySent,           // Richiesta inviata con successo
  MustBeInLoggedState,        // Bisogna essere nello stato Logged per eseguire
                              // il comando
  DocumentDoesNotExists,      // Il documento non esiste
  AlreadyCollaborates,        // L'utente già collabora alla modifica del
                              // documento
  CreatorCannotBeCollaborator,// Il creatore del documento non può essere
                              // collaboratore


  //---------------//,

}
