### Creazione e attivazione di thread
1. Definire un task, ovvero definire una classe X che implementi l'interfaccia
Runnable, creare un'istanza di X, infine creare un thread passandogli
l'istanza del task creato, oppure
2. estendere la classe java.lang.Thread

* Una classe che implementa l'interfaccia deve fornire l'implementazione del
metodo run()
* Un oggetto istanza di X è un Task, cioè un frammento di codice che può essere
eseguito in un thread
* Creare un task non implica che venga creato un Thread
* Lo stesso task può essere eseguito da più threads (quindi si ha
un solo codice)
* Un oggetto Thread viene istanziato con un riferimento al task da eseguire
* Dopo l'attivazione dei threads e prima della loro terminazione, il controllo
ripassa al programma principale

### Start e Run

#### Un thread NON viene attivato, cioè non si crea un flusso di esecuzione, se:
1. si crea un oggetto istanza della classe Thread
2. si invoca il metodo run() della classe che implementa l'interfaccia
Runnable
3. solo il metodo start() comporta la creazione di un nuovo thread

#### Cosa accade se sostituisco l'invocazione del metodo run al posto di start?
1. non viene attivato alcun thread
2. ogni metodo run viene eseguito all'interno del flusso del thread
attivato per l'esecuzione del programma principale
3. si ottiene un flusso di esecuzione sequenziale

### Riassunto

#### Per definire task ed attivare thread che li eseguano
1. Definire una classe R che implementi l'interfaccia Runnable, quindi
implementare il metodo run.
2. allocare un'istanza T di R
3. per costruire il thread, utilizzare il costruttore Thread passando il task
come parametro
4. attivare il thread con una start()

Il metodo **start()**

* segnala allo schedulatore (tramite la JVM) che il thread può essere attivato
e di conseguenza viene inizializzato l'ambiente del thread

* restituisce immediatamente il controllo al chiamante, senza attendere che il
thread attivato inizi la sua esecuzione

### Thread demoni

* I thread demoni sono thread che hanno il compito di fornire un servizio  in
background  fino  a  che  il  programma  è  in  esecuzione.
* Quando  tutti  i  thread  non-demoni  sono  completati,  il  programma  termina
(anche se ci sono thread demoni in esecuzione).
* Se  ci  sono  thread  non-demoni  ancora  in  esecuzione,  il  programma  non
termina.
* esempi di thread non-demone sono il main() e il Java Garbage Collector

