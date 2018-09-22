/*
 * Nome: Andrea
 * Cognome: Tosti
 * Matricola: 518111
 */

//TOGLIERE PACKAGE

package UfficioPostale;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

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
                System.out.printf("[ServerSalaAttesa] Cliente con id = " +
                        "%d e' entrato in sala d'attesa\n", task.getId());
                exec.execute(task);
            }
            catch(InterruptedException e)
            {
                System.out.println("[ServerSalaAttesa] Comunicazione " +
                                   "chiusura dal thread main");
                Task task;
                while((task = codaSalaAttesa.poll()) != null)
                {
                    System.out.printf("[ServerSalaAttesa] Cliente con " +
                        "id = %d e' entrato in sala d'attesa\n", task.getId());
                    exec.execute(task);
                }
                exec.shutdown();
                return;
            }
        }
    }
}
