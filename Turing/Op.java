package Turing;

public enum Op
{
  SuccessfullyRegistered,     // Registrazione avvenuta con successo
  SuccessfullyLoggedIn,       // Login avvenuto con successo
  SuccessfullyLoggedOut,      // Logout avvenuto con successo
  SuccessfullyCreated,        // Creazione documento avvenuta con successo
  SuccessfullyShared,         // Condivisione documento avvenuta con successo
  SuccessfullyShown,          // Visualizzazione avvenuta con successo
  SuccessfullyRemovedSession, // Sessione rimossa con successo
  SuccessfullyReceivedSections,// Tutte le sezioni richieste sono state ricevute
                               // con successo
  SuccessfullyReceivedList,   // Lista ricevuta con successo
  SuccessfullyListed,         // Lista visualizzata con successo
  SuccessfullyStartedEditing, // Inizio editing sezione avvenuto con successo
  SuccessfullyEndedEditing,   // Fine editing sezione avvenuto con successo
  SuccessfullySentSection,    // Invio sezione avvenuta con successo
  SuccessfullySentMessage,    // Invio messaggio avvenuto con successo
  SuccessfullyReceivedMessage,// Ricezione messaggio avvenuta con successo
  WrongPassword,              // Password non corretta
  UserDoesNotExists,          // L'utente non esiste
  NicknameAlreadyExists,      // Esiste già l'utente
  DocumentAlreadyExists,      // Esiste già il documento
  DirectoryAlreadyExists,     // Esiste già la cartella
  NotDocumentCreator,         // L'utente non è il creatore del documento
  NotDocumentCreatorNorCollaborator, // L'utente non è né il creatore del
                                     // documento, né un collaboratore
  SectionUnderModification,   // La sezione è attualmente sotto modifica
  Login,                      // Operazione di Login TCP
  Logout,                     // Operazione di Logout TCP
  Create,                     // Operazione di creazione di un documento
  Share,                      // Operazione di condivisione di un documento
  Show,                       // Operazione di visualizzazione di una sezione
                              // o dell'intero documento
  List,                       // Operazione di visualizzazione lista documenti
                              // con relativi creatori e collaboratori
  Edit,                       // Operazione di richiesta modifica di una sezione
  EndEdit,                    // Operazione di richiesta fine modifica di una
                              // sezione
  Send,                       // Operazione di invio di un messaggio
  Error,                      // Errore non ben specificato
  UsageError,                 // Passaggio parametri sbagliato
  ClosedConnection,           // Il server non È più raggiungibile
  UnknownSession,             // Sessione inesistente
  AlreadyLoggedIn,            // L'utente è già loggato
  CannotLogout,               // L'utente è nello stato Started o Editing
  SuccessfullySent,           // Richiesta inviata con successo
  MustBeInLoggedState,        // Bisogna essere nello stato Logged per eseguire
                              // il comando
  MustBeInEditingState,       // Bisogna essere nello stato Editing per eseguire
                              // il comando
  DocumentDoesNotExists,      // Il documento non esiste
  SectionDoesNotExists,       // La sezione non esiste
  AlreadyCollaborates,        // L'utente già collabora alla modifica del
                              // documento
  CreatorCannotBeCollaborator,// Il creatore del documento non può essere
                              // collaboratore



  //---------------//,

}
