package UfficioPostale;

/*
 * @brief Una persona è un Task
 */

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

public class Task implements Runnable
{
    private final int id;

    public Task(int id)
    {
        this.id = id;
    }

    @Override
    public void run()
    {
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
    }

    int getId()
    {
        return id;
    }
}
