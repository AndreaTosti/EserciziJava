# Daemon threads

## Codice di esempio

Si hanno due threads:
1. un thread utente che scrive eventi su una coda
2. un thread daemon che fa il clean della coda, rimuovendo gli eventi che son
stati generati dopo più di 10 secondi

Codice ripreso da [Java-9-Concurrency-Cookbook-Second-Edition](
http://github.com/PacktPublishing/Java-9-Concurrency-Cookbook-Second-Edition)
