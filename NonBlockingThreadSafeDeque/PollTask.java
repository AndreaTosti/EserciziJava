package NonBlockingThreadSafeDeque;

import java.security.SecureRandom;
import java.util.concurrent.ConcurrentLinkedDeque;

public class PollTask implements Runnable
{
    private final ConcurrentLinkedDeque<String> list;

    PollTask(ConcurrentLinkedDeque<String> list)
    {
        this.list = list;
    }

    @Override
    public void run()
    {
        int k= 0;
        SecureRandom randomNumbers = new SecureRandom();
        int randomValue = randomNumbers.nextInt(5000);
        for(int i = 0; i < randomValue; i++)
        {
            k += (list.pollFirst() == null ? 0 : 1) ;
            k += (list.pollLast() == null ? 0 : 1);
        }
        System.out.printf("Polled %d items \n", k);
    }
}
