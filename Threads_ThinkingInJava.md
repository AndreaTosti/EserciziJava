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

È importante rendere i campi **private** quando si lavora con la
concorrenza, altrimenti la parola chiave **synchronized** non potrà
impedire che un altro task acceda al campo direttamente, provocando cosí un
conflitto.

Un task potrebbe acquisire *più volte* il lock su un oggetto: questo accade
se un metodo chiama un altro metodo sullo stesso oggetto, che a sua volta
chiama un altro metodo sullo stesso oggetto e cosí via. La JVM tiene traccia
del numero di volte che l'oggetto è stato bloccato: non appena l'oggetto
viene sbloccato il conteggio riparte da zero. Quando un task acquisisce per
la prima volta il lock, il conteggio vale uno, e viene incrementato ogni
volta che lo stesso task acquisisce un altro lock sullo stesso oggetto.
L'acquisizione di lock multipli è concessa soltanto al task che lo acquisito
per la prima volta. Ogni volta che il task esce da un metodo **synchronized**
il conteggio diminuisce, fino ad arrivare a zero, liberando cosí il lock a
favore di altri task.

Se la classe ha più metodi che gestiscono dati critici, bisogna sincronizzare
tutti i metodi. In caso contrario i metodi non sincronizzati sarebbero liberi
di ignorare il lock dell'oggetto e potrebbero essere chiamati senza alcun
controllo.

Quindi, ogni metodo che accede a una risorsa comune critica deve essere di
tipo **synchronized**, oppure non funzionerà correttamente.

