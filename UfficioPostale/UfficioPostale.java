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

        LinkedBlockingQueue<Task> codaSportelli =
                new LinkedBlockingQueue<>(capacitaCodaSportelli);

        ServerSalaAttesa serverSalaAttesa =
                new ServerSalaAttesa(codaSalaAttesa, codaSportelli);
        Thread threadServerSalaAttesa = new Thread(serverSalaAttesa);

        ServerSalaSportelli serverSalaSportelli =
                new ServerSalaSportelli(numSportelli, codaSportelli);
        Thread threadServerSalaSportelli = new Thread(serverSalaSportelli);

        threadServerSalaAttesa.start();
        threadServerSalaSportelli.start();

        for(int i = 1; i <= numTasks; i++)
        {
            Task task = new Task(i);
            try
            {
                //Thread.sleep(50);
                codaSalaAttesa.put(task);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        threadServerSalaAttesa.interrupt();
        threadServerSalaAttesa.join();
        threadServerSalaSportelli.interrupt();
        threadServerSalaSportelli.join();
    }
}
