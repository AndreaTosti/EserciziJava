# Daemon threads

* I thread demoni sono thread che hanno il compito di fornire un servizio  in
background  fino  a  che  il  programma  è  in  esecuzione.
* Quando  tutti  i  thread  non-demoni  sono  completati,  il  programma  termina
(anche se ci sono thread demoni in esecuzione).
* Se  ci  sono  thread  non-demoni  ancora  in  esecuzione,  il  programma  non
termina.
* esempi di thread non-demone sono il main() e il Java Garbage Collector

## Codice di esempio

Si hanno due threads:
1. un thread utente che scrive eventi su una coda
2. un thread daemon che fa il clean della coda, rimuovendo gli eventi che son
stati generati dopo più di 10 secondi

Codice ripreso da [Java-9-Concurrency-Cookbook-Second-Edition](
http://github.com/PacktPublishing/Java-9-Concurrency-Cookbook-Second-Edition)
