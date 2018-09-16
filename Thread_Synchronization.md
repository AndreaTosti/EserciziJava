### Gerarchia delle classi: RUNNABLE
* Thread estende Object e implementa l'interfaccia Runnable
* runnable è una variabile locale della classe Thread, memorizza l'oggetto
Runnable passato come parametro
    ```java
    public void run()   //metodo run della classe Thread
    {
        if(runnable != null)
            runnable.run();
    }
    ```
* l'invocazione di *start()* provoca l'esecuzione di *run()*, che a sula volta
provoca l'esecuzione di *run()* di runnable
* Java è a ereditarietà singola, quindi usare l'approccio in cui una classe
estende Thread fa sí che tale classe non può estendere altre classi.
### Classe java.lang.Thread
* Contiene metodi per:
    * costruire un thread
    * metodi set e get per settare o ottenere le caratteristiche di un thread
        * assegnare un nome al thread e la priorità
    * attivare, sospendere, interrompere i threads
    * attendere la terminazione di un thread
* NON contiene metodi per:
    * la sincronizzazione tra i threads. Tali metodi sono definiti in
    *java.lang.object*, **perchè la sincronizzazione opera su oggetti**
* salva informazioni utili ad identificare i thread
    * **ID** identificatore del thread
    * **nome** nome del thread
    * **priorità** valore da 1 a 10 (1 priorità più bassa)
    * **nome gruppo** gruppo a cui appartiene il thread
    * **stato** uno tra i possibili stati *new*, *runnable*, *blocked*,
    *waiting*, *time waiting* o *terminated*
* metodi setter e getter per ottenere valori di una proprietà, ad esempio per
associare un nome a un thread e di reperirlo
    ```java //esempio per reperire la priorità
    Thread.currentThread().getPriority();
    ```
### Metodo join()
* attende la terminazione del thread sul quale è richiamato
* il thread che esegue join() **rimane bloccato** in attesa della terminazione
del thread su cui è invocata la join()
* È possibile specificare un timeout di attesa
* può lanciare un'eccezione *InterruptedException* se il thread riceve
un'interruzione mentre è sospeso sulla join
### Condivisione di risorse
* Scenario tipico di un programma concorrente: un insieme di threads
condividono una risorsa
    * più threads accedono concorrentemente allo stesso file, alla
    stessa parte di un database o di una struttura di memoria
* **race conditions** - l'accesso non controllato a risorse condivise
può provocare errori e inconsistenze
* **sezione critica** - blocco di codice in cui si effettua l'accesso
ad una risorsa condivisa e che deve essere eseguito da un thread per
volta
* meccanismi di sincronizzazione per implementare le sezioni critiche
    * interfaccia **Lock** e le sue diverse implementazioni
    * concetto di **monitor**
Esempio di oggetto condiviso come risorsa condivisa
```java
Account account = new Account();
Company company = new Company(account);
Bancomat bank = new Bancomat(account);
```
Notare come i costruttori di Company e Bancomat prendano in input
l'oggetto account. Se viene manipolato, nello stesso momento, sia dal
thread che gestisce company che dal thread che gestisce bank,
l'oggetto account(tramite metodi che modificano i suoi campi),
può manifestarsi una Race Condition.

Per risolvere bisogna sincronizzare i thread, esistono alcuni
meccanismi:
* meccanismi a basso livello
    * lock()
    * variabili di condizione associate a lock()
* meccanismi ad alto livello
    * synchronized()
    * wait(), notify(), notifyAll()
    * monitors

### Lock

Una lock è un oggetto che può trovarsi in due stati diversi: lock e
unlocked.
* lo stato viene impostato con i metodi lock() e unlock()
* un solo thread alla volta può ottenere la lock()
* gli altri thread che tentano di ottenere la lock si bloccano

Quando un thread tenta di acquisire una lock
* rimane bloccato fintanto che la lock è detenuta da un altro thread
* rilascio della lock: uno dei thread in attesa la acquisisce

Interfaccia: *java.util.concurrent.locks.Lock*

Implementazione: *java.util.concurrent.locks.ReentrantLock*

Esempio:
```java
private final Lock accountLock = new ReentrantLock();
accountLock.lock();
//esegui operazioni ...
accountLock.unlock();
```
Esempio di **deadlock** (i thread A e B rimangono bloccati all'infinito)
```
Thread(A) acquisisce Lock(X)
Thread(B) acquisisce Lock(Y)
A tenta di acquisire Lock(Y)
B tenta di acquisire Lock(X)
```
### Read-Write Locks
Interfaccia: *ReadWriteLock*

Implementazione: *ReentrantReadWriteLock*
* mantiene una coppia di lock associate, una per le operazioni di sola
lettura e una per le scritture
    * la *read lock* può essere acquisita da più thread lettori, purchè
    non vi siano scrittori
    * la *write lock* è esclusiva

### Cooperazione threads - variabili di condizione
* l'interazione esplicita tra threads avviene tramite l'utilizzo di
oggetti condivisi
* esempio: *Produttore-Consumatore* dove il produttore P produce un
nuovo valore e lo comunica ad un thread consumatore C
* il valore prodotto viene incapsulato in un oggetto condiviso da P e
da C, ad esempio una *coda*
* la mutua esclusione sull'oggetto condiviso è garantita dall'uso di
metodi synchronized, **ma non è sufficiente garantire sincronizzazioni
esplicite**
* è necessario introdurre costrutti per sospendere un thread T quando
una condizione C non è verificata e per riattivare T quando diventa
vera
    * il produttore si sospende se il buffer è pieno, si riattiva
    quando c'è una posizione libera
* ad una lock possono essere associate un insieme di *variabili di
condizione*
* lo scopo di tali variabili è di permettere ai thread di controllare
se una condizione sullo stato della risorsa è verificata o meno e
    * se la condizione è falsa, di sospendersi rilasciando la lock() ed
    inserire il thread in una coda di attesa di quella condizione
    * risvegliare un thread in attesa quando la condizione risulta
    verificata
* per ogni oggetto diverse code:
    * una per i threads in attesa di acquisire la lock()
    * una associata ad ogni variabile di condizione
* sospensione su variabili di condizione associate ad un oggetto solo
dopo aver acquisito la lock() su quell'oggetto, altrimenti viene
sollevata una *IllegalMonitorException*
* l'interfaccia *Condition* fornisce i meccanismi per sospendere un
thread e per risvegliarlo