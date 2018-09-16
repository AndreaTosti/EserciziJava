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

