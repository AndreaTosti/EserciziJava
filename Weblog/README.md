# Weblog

Il log file di un web server contiene un insieme di linee, dove ogni linea ha il seguente formato:

150.108.64.57 - - [15/Feb/2001:09:40:58 -0500] "GET / HTTP 1.0" 200 2511

in cui:

    •150.108.64.57 indica l'host remoto, in genere secondo la dotted quad form
    •[data]
    •"HTTP request"
    •status
    •bytes sent
    •eventuale tipo del client "Mozilla/4.0......."

•scrivere una applicazione Weblog che prende in input il nome del log file (che sarà fornito) e stampa su console (e su un file) ogni linea del log file, in cui ogni indirizzo IP è sostituito con il nome dell'host
•sviluppare due versioni del programma, la prima single-threaded, la seconda invece utilizza un thread pool, in cui il task assegnato ad ogni thread riguarda la traduzione di una singola linea del file. Confrontare i tempi delle due versioni.

