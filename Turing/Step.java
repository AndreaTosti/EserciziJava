package Turing;

public enum Step
{
  WaitingForMessageSize,     // Attesa ricezione dimensione del messaggio
  WaitingForMessage,         // Attesa ricezione messaggio
  SendingOutcome,            // Invio dell'esito
  SendingNumberOfSections,   // Invio del numero delle sezioni
  SendingSections,           // Invio di una o più sezioni
}