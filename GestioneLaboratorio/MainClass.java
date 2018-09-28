//Togli package
package GestioneLaboratorio;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainClass
{
    public static void main(String[] args)
    {
        ThreadPoolExecutor executor =
                new ThreadPoolExecutor
                        (
                            4,
                            4,
                            1,
                            TimeUnit.SECONDS,
                            new PriorityBlockingQueue<Runnable>()
                        );
        for(int i = 0; i < 10; i++)
        {
            Utente utente = new Utente("Utente" + i, i);
            executor.execute(utente);
        }
        try
        {
            TimeUnit.SECONDS.sleep(1);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
        for(int i = 10; i < 20; i++)
        {
            Utente utente = new Utente("Utente" + i, i);
            executor.execute(utente);
        }
        executor.shutdown();
        try
        {
            executor.awaitTermination(1, TimeUnit.DAYS);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
        System.out.print("Main: end of the program\n");

    }
}
