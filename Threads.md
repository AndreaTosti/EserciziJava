### Creazione e attivazione di thread
1. Definire un task, ovvero definire una classe X che implementi l'interfaccia
Runnable, creare un'istanza di X, infine creare un thread passandogli
l'istanza del task creato, oppure
2. estendere la classe java.lang.Thread

## *Iniziamo a vedere il metodo 1.*

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

### Terminazione di programmi concorrenti

* Un programma Java termina quando terminano tutti i threads non demoni che
lo compongono
* se il thread iniziale, cioè quello che esegue il metodo main() termina, i
restanti thread ancora attivi continuano la loro esecuzione fino alla loro
terminazione
* se uno dei thread usa System.exit() per terminare l'esecuzione, allora tutti
i threads terminano la loro esecuzione (viene spenta la JVM)

### Interruzione di threads

* un thread può essere interrotto e può intercettare l'interruzione in modi
diversi, a seconda dello stato in cui si trova
    * se è sospeso l'interruzione causa il sollevamento di una
    **InterruptedException**
    * se è in esecuzione, può testare un flag che segnala che è stata inviata
    una interruzione
* possiamo interrompere un thread usando il metodo interrupt() su di esso e
cosí facendo viene impostato un flag a **true** nel descrittore del thread
* è possibile testare il valore di tale flag
   * con il metodo statico Interrupted() e tale metodo setta in automatico il
   flag a **false** oppure
   * usando isInterrupted() su un oggetto di tipo thread lasciando
   **invariato** il valore del flag

**Task** = segmento di codice che può essere eseguito da un esecutore (un
oggetto di tipo Runnable, in Java)

**Thread** = Esecutore di tasks

### Thread Pool
* struttura dati la cui dimensione massima può essere **prefissata**, contiene
riferimenti ad un insieme di threads
* i thread del pool possono essere **riutilizzati** per l'esecuzione di più
tasks
* dopo aver messo un task nel pool, l'esecuzione del task può essere ritardata
se non vi sono risorse disponibili
* Esiste una politica di gestione dei thread del pool, stabilita all'atto della
creazione del pool da parte dell'utente, tale politica stabilisce:
    * **quando** i thread vengono attivati(subito, on demand, ecc)
    * se e quando è opportuno terminare l'esecuzione di un thread, ad esempio
    se non c'è un numero sufficiente di tasks da eseguire
* Quando viene sottomesso un task, il pool di thread:
    * può utilizzare un thread attivato in precedenza, che magari è inattivo
    * creare un nuovo thread
    * memorizzare il task in una coda, in attesa di eseguirlo
    * respingere la richiesta di esecuzione del task
* Il numero di threads attivi nel pool varia dinamicamente
* Esistono supporti per facilitare la creazione di pool di threads, ad esempio
la classe **Executors** opera come una **factory** ed è in grado di generare
oggetti di tipo **ExecutorService**
    * i tasks devono essere incapsulati in oggetti di tipo Runnable e passati
    a questi esecutori mediante il metodo **execute()**

Metodo **newCachedThreadPool**
* crea un pool dove
    * non ci sono limiti sulla dimensione del pool, se tutti i thread sono
    occupati nell'esecuzione di altri task da eseguire, viene creato un nuovo
    thread
    * se disponibile, viene riutilizzato un thread che ha terminato
    l'esecuzione di un task precedente
    * se un thread rimane inutilizzato per 60 secondi, la sua esecuzione
    termina
    * è elastico, ovvero può espandersi all'infinito e si contrae quando la
    domanda di esecuzione di task diminuisce

Metodo **newFixedThreadPool(int N)**
* crea un pool dove
    * vengono creati N thread, riutilizzati per l'esecuzione di più tasks
    * quando viene sottomesso un task T:
        * se tutti i threads sono occupati nell'esecuzione di altri tasks, T
        viene inserito in una coda, gestita poi automaticamente da
        ExecutorService
        * la coda è illimitata
        * se almeno un thread è inattivo, viene utilizzato quel thread

## *Vediamo ora il metodo 2.*

* fare uso di subclassing, overriding..
* creare una classe X che estenda la classe Thread e fare l'override del
metodo **run()**
* instanziare un oggetto di quella classe, tale oggetto è un thread il cui
comportamento è quello definito nel metodo run (su cui è stato fatto
l'override
* invocare il metodo **start()** sull'oggetto istanziato





