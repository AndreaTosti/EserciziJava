/*
 * Nome: Andrea
 * Cognome: Tosti
 * Matricola: 518111
 */

//TOGLIERE PACKAGE

package UfficioPostale;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class ServerSalaAttesa implements Runnable
{
    private LinkedBlockingQueue<Task> codaSalaAttesa;
    private ExecutorService exec;

    ServerSalaAttesa(LinkedBlockingQueue<Task> codaSalaAttesa,
                     ExecutorService exec)
    {
        this.codaSalaAttesa = codaSalaAttesa;
        this.exec = exec;
    }

    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                Task task = codaSalaAttesa.take();
                System.out.printf("[ServerSalaAttesa] Nuovo cliente con id = " +
                        "%d\n", task.getId());
                exec.execute(task);
            }
            catch(InterruptedException e)
            {
                System.out.println("ThreadSalaAttesa deve smettere di lavorare");
                Task task;
                while((task = codaSalaAttesa.poll()) != null)
                {
                    System.out.printf("[ServerSalaAttesa] nuovo cliente con " +
                                      "id = %d\n", task.getId());
                    exec.execute(task);
                }
                return;
            }
        }
    }
}
