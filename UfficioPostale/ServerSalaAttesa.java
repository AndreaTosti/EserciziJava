/*
 * Nome: Andrea
 * Cognome: Tosti
 * Matricola: 518111
 */

//TOGLIERE PACKAGE

package UfficioPostale;

import java.util.concurrent.LinkedBlockingQueue;

public class ServerSalaAttesa implements Runnable
{
    private LinkedBlockingQueue<Task> codaSalaAttesa;
    private LinkedBlockingQueue<Task> codaSportelli;

    ServerSalaAttesa(LinkedBlockingQueue<Task> codaSalaAttesa,
                     LinkedBlockingQueue<Task> codaSportelli)
    {
        this.codaSalaAttesa = codaSalaAttesa;
        this.codaSportelli = codaSportelli;
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
                codaSportelli.put(task);
            }
            catch(InterruptedException e)
            {
                System.out.println("ThreadSalaAttesa deve smettere di lavorare");
                Task task;
                while((task = codaSalaAttesa.poll()) != null)
                {
                    System.out.printf("[ServerSalaAttesa] nuovo cliente con " +
                                      "id = %d\n", task.getId());
                    try
                    {
                        codaSportelli.put(task);
                    }
                    catch(InterruptedException ex)
                    {
                        ex.printStackTrace();
                    }
                }
                return;
            }
        }
    }
}
