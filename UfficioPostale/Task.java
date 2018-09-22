/*
 * Nome: Andrea
 * Cognome: Tosti
 * Matricola: 518111
 */

//TOGLIERE PACKAGE

package UfficioPostale;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

public class Task implements Runnable
{
    private final int id;
    private final int secondiMassimiTask;

    Task(int id, int secondiMassimiTask)
    {
        this.id = id;
        this.secondiMassimiTask = secondiMassimiTask;
    }

    @Override
    public void run()
    {
        System.out.printf("[Task %d] inizio conversazione con operatore %s\n",
                          this.id, Thread.currentThread().getName());

        SecureRandom randomNumbers = new SecureRandom();
        int randomValue = randomNumbers.nextInt(secondiMassimiTask * 1000);
        try
        {
            TimeUnit.MILLISECONDS.sleep(randomValue);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
        System.out.printf("[Task %d] fine conversazione con operatore %s\n",
                this.id, Thread.currentThread().getName());
    }

    int getId()
    {
        return id;
    }
}
