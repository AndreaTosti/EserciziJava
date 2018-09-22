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
    private static final int numSportelli = 4;
    private static final int capacitaCodaSportelli = 10;
    private static final int numTasks = 100;

    public static void main(String[] args) throws Exception
    {
        LinkedBlockingQueue<Task> codaSalaAttesa =
                new LinkedBlockingQueue<>();

        RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
        ExecutorService exec = new ThreadPoolExecutor(
                numSportelli,
                numSportelli,
                0,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(capacitaCodaSportelli),
                rejectedExecutionHandler);

        ServerSalaAttesa serverSalaAttesa =
                new ServerSalaAttesa(codaSalaAttesa, exec);

        Thread threadServerSalaAttesa = new Thread(serverSalaAttesa);
        threadServerSalaAttesa.start();

        for(int i = 1; i <= numTasks; i++)
        {
            Task task = new Task(i);
            try
            {
                codaSalaAttesa.put(task);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        threadServerSalaAttesa.interrupt();
        threadServerSalaAttesa.join();
    }
}
