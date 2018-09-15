package NonBlockingThreadSafeDeque;

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
        for(int i = 0; i < 5000; i++)
        {
            list.pollFirst();
            list.pollLast();
        }
    }
}
