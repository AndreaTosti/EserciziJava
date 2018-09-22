/*
 * Nome: Andrea
 * Cognome: Tosti
 * Matricola: 518111
 */

//TOGLIERE PACKAGE

package UfficioPostale;

/*
 * @brief Una persona è un Task
 */

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

public class Task implements Runnable
{
    private final int id;

    Task(int id)
    {
        this.id = id;
    }

    @Override
    public void run()
    {
        System.out.printf("[Task %d] è davanti all'operatore %s\n",
                          this.id, Thread.currentThread().getName());

        SecureRandom randomNumbers = new SecureRandom();
        int randomValue = randomNumbers.nextInt(1000);
        try
        {
            TimeUnit.MILLISECONDS.sleep(randomValue);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
        System.out.printf("[Task %d] è stato servito dall'addetto %s\n",
                this.id, Thread.currentThread().getName());
    }

    int getId()
    {
        return id;
    }
}
