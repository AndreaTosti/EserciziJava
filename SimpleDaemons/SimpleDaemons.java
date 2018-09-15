package SimpleDaemons;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

public class SimpleDaemons implements Runnable
{
    public void run()
    {
        try
        {
            //noinspection InfiniteLoopStatement
            while(true)
            {
                TimeUnit.MILLISECONDS.sleep(100);
                System.out.println(Thread.currentThread() + " " + this);
            }
        }
        catch(InterruptedException e)
        {
            System.out.println("sleep() interrupted");
        }
    }

    public static void main(String[] args) throws Exception
    {
        for(int i = 0; i < 10; i++)
        {
            Thread daemon = new Thread(new SimpleDaemons());
            daemon.setDaemon(true);
            daemon.start();
        }
        System.out.println("All daemons started");
        TimeUnit.MILLISECONDS.sleep(500);
    }
}
