/*
 * Nome: Andrea
 * Cognome: Tosti
 * Matricola: 518111
 */

//TOGLIERE PACKAGE

package UfficioPostale;

import sun.nio.ch.ThreadPool;

import java.util.concurrent.*;

class ServerSalaSportelli implements Runnable
{
    private ThreadPoolExecutor exec;
    private LinkedBlockingQueue<Task> codaSportelli;

    ServerSalaSportelli(int numSportelli,
                        LinkedBlockingQueue<Task> codaSportelli)
    {
        this.codaSportelli = codaSportelli;
        exec = new ThreadPoolExecutor(numSportelli, numSportelli, 0,
                                  TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                Task task = codaSportelli.take();
                System.out.printf("[ServerSalaSportelli] entrato cliente con " +
                        "id = %d\n", task.getId());
                exec.execute(task);
            }
            catch(InterruptedException e)
            {
                System.out.println("ThreadSalaSportelli deve smettere di " +
                                   "lavorare");
                Task task;
                while((task = codaSportelli.poll()) != null)
                {
                    System.out.printf("[ServerSalaSportelli] entrato cliente " +
                            "con id = %d\n", task.getId());
                    exec.execute(task);
                }
                exec.shutdown();
                return;
            }
        }
    }
}
