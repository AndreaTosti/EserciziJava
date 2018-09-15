package UfficioPostale;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

public class AddettoSportello implements Runnable
{
    /*
     * @param[in] numSportello
     */
    private final int numSportello;
    AddettoSportello(int numSportello)
    {
        this.numSportello = numSportello;
    }

    @Override
    public void run()
    {
        System.out.printf("L'addetto allo sportello %d e' al lavoro\n",
                            numSportello);
        SecureRandom randomNumbers = new SecureRandom();
        int randomValue = randomNumbers.nextInt(2000);
        try
        {
            TimeUnit.MILLISECONDS.sleep(randomValue);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }

        System.out.printf("L'addetto allo sportello %d ha finito il lavoro" +
                          " durato %.3f secondi \n", numSportello,
                            randomValue/1000.f);
    }

}
