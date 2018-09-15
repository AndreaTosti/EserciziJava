### Synchronized

Quando un task vuole eseguire una parte di codice soggetta alla parola chiave
**synchronized**, va a controllare se il blocco è attivo, lo acquisisce, esegue
il codice e lo rimette a disposizione.
Per gestire l'accesso a una risorsa comune come un file, una porta I/O, un
dispositivo esterno, bisogna prima inserire tale risorsa in un oggetto.
A questo punto qualunque metodo utilizzi la risorsa può essere reso
**synchronized**. Se un task sta chiamando uno dei metodi **synchronized**, a
tutti gli altri sarà impedito l'accesso a qualsiasi metodo **synchronized** di
quell'oggetto, fino a quando il primo task non ritorni dalla sua chiamata.
Bisogna rendere **private** gli elementi di dati di una classe e accedere a
quella porzione di memoria soltanto per mezzo di metodi. Si possono impedire
eventuali conflitti dichiarando quei metodi **synchronized**.
Tutti gli oggetti contengono automaticamente un solo lock, chiamato anche
**monitor lock**. Quando chiamate un metodo **synchronized** l'oggetto viene
bloccato e nessun altro metodo **synchronized** dello stesso oggetto può
essere chiamato finché il primo non termini e rilasci il lock. Per quanto
riguarda ad esempio *synchronized void f() { /* ... */ }*, se a **f()** viene
richiesto un oggetto da un task, un altro task non potrà richiedere lo stesso
oggetto chiamando **f()** o **g()** dove *synchronized void g() { /* ... */ }*,
fino a quando **f()** non abbia completato l'esecuzione e rilasciato il lock.
Questo dimostra che esiste un solo lock che viene ripartito tra tutti i
metodi **synchronized** di un determinato oggetto, e tale lock può essere
utilizzato per impedire la scrittura della memoria dell'oggetto da parte di
più task contemporaneamente.
