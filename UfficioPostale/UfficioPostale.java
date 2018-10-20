/*
 * Nome: Andrea
 * Cognome: Tosti
 * Matricola: 518111
 */

//TOGLIERE PACKAGE

package UfficioPostale;

import java.util.concurrent.*;
import java.util.concurrent.TimeUnit;

public class UfficioPostale
{
    /* numero dei thread del ThreadPool*/
    private static final int numSportelli = 4;

    /* numero massimo di task che possono stare nella seconda sala, che equivale
       ad avere al massimo k persone nella seconda sala */
    private static final int capacitaCodaSportelli = 1;

    /* Numero di task da far entrare nella prima sala */
    private static final int numTasks = 50;

    /* Ogni task viene servito dall'addetto allo sportello per un numero di
       secondi random che va da 0 a secondiMassimiTask */
    private static final int secondiMassimiTask = 3;


    public static void main(String[] args) throws Exception
    {
        if(numSportelli <= 0 || capacitaCodaSportelli <= 0 ||
                numTasks < 0 || secondiMassimiTask <= 0)
        {
            System.out.println("Inserire i parametri correttamente");
            return;
        }

        LinkedBlockingQueue<Task> codaSalaAttesa =
                new LinkedBlockingQueue<>();

        /*
            la seguente istruzione non va bene, perche' altrimenti il task
            verrebbe eseguito dal thread chiamante e non da un thread del pool

            RejectedExecutionHandler rejectedExecutionHandler =
            new ThreadPoolExecutor.CallerRunsPolicy();
        */

        ThreadPoolExecutor exec = new ThreadPoolExecutor(
                numSportelli,
                numSportelli,
                0,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(capacitaCodaSportelli));

        /* https://stackoverflow.com/a/10353250/9963438
           la seguente istruzione serve a bloccare il thread
           threadServerSalaAttesa nel caso in cui questo provi a mettere in coda
           un task quando la coda del ThreadPool e' piena
         */
        exec.setRejectedExecutionHandler
        (
            new RejectedExecutionHandler()
            {
                public void rejectedExecution(Runnable r,
                                              ThreadPoolExecutor executor)
                {
                    /* se la coda è piena, il metodo put si blocca */
                    try
                    {
                        executor.getQueue().put(r);
                    }
                    catch(InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        );
                                         
        ServerSalaAttesa serverSalaAttesa =
                new ServerSalaAttesa(codaSalaAttesa, exec);

        Thread threadServerSalaAttesa = new Thread(serverSalaAttesa);
        threadServerSalaAttesa.start();

        for(int i = 1; i <= numTasks; i++)
        {
            Task task = new Task(i, secondiMassimiTask);
            try
            {
                codaSalaAttesa.put(task);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        System.out.println("Il thread main ha finito di inserire i task");
        /* Ho finito di inserire i task, chiedo al thread
           threadServerSalaAttesa di finire il lavoro rimanente */
        threadServerSalaAttesa.interrupt();
        threadServerSalaAttesa.join();
    }
}
