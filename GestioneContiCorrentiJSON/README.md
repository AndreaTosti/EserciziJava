La banca dello scorso esercizio (Gestione conti correnti, Lezione Java I/O) deve aggiornare il proprio software per soddisfare i nuovi requisiti imposti dalla Banca Centrale Europa: al fine di migliorare l'interoperabilità e la trasparenza dei sistemi informativi bancari è necessario che il file di descrizione dei conti correnti sia in formato JSON. L'aggiornamento del software prevede inoltre di migliorare le performance attraverso l'utilizzo di librerie NIO.

In sintesi, si richiede di implementare la stessa applicazione dell'esercizio del 12 Ottobre, ma utilizzando il formato JSON e le librerie JAVA NIO al posto di JAVA IO. Si domanda inoltre di valutare le performance del software.

DESCRIZIONE DETTAGLIATA.

creare un file contenente oggetti che rappresentano i conti correnti in formato JSON.

ogni conto corrente contiene il nome del correntista ed una lista di movimenti.

per ogni movimento vengono registrati la data e la causale del movimento. L'insieme delle causali possibili è fissato, ed ogni casuale è rappresentata da una stringa (es. Bonifico, Accredito, F24, etc...).

Rileggere il file e trovare, per ogni possibile causale, quanti movimenti hanno quella causale.


A questo scopo progettare un'applicazione che attiva un insieme di thread. Uno di essi legge dal file gli oggetti “conto corrente” e li passa, uno per volta, ai thread presenti in un thread pool.

ogni thread calcola il numero di occorrenze di ogni possibile causale all'interno di quel conto corrente ed aggiorna un contatore globale.

alla fine il programma stampa

      (a) per ogni possibile causale il numero totale di occorrenze

      (b) la dimensione del file di descrizione dei conti correnti

      (c) il tempo totale impiegato.

per valutare le performance, eseguire l'applicazione su due files di descrizione dei conti correnti: il primo di dimensione 1M e il secondo di 10M.

FACOLTATIVO: modificare l'applicazione del 12 Ottobre, in modo da farle stampare i punti (b) e (c) sopra. Utilizzare questa versione modificata per confrontare le performance tra l'applicazione che utilizza le standard IO e quella che utilizza NIO.