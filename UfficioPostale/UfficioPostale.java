package UfficioPostale;

import java.util.concurrent.*;

public class UfficioPostale
{
    private static final int numSportelli = 4;
    public static void main(String[] args)
    {
        ExecutorService exec = Executors.newFixedThreadPool(numSportelli);
        for(int i = 0; i < numSportelli; i++)
        {
            exec.execute(new AddettoSportello(i));
        }
        exec.shutdown();
    }
}
