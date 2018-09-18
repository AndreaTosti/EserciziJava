package UfficioPostale;

import java.util.concurrent.LinkedBlockingQueue;

public class AddettoSportello implements Runnable
{
    private final int numSportello;
    private final LinkedBlockingQueue<Task> codaSportelli;

    AddettoSportello(int numSportello, LinkedBlockingQueue<Task> codaSportelli)
    {
        this.numSportello = numSportello;
        this.codaSportelli = codaSportelli;
    }

    @Override
    public void run()
    {
        System.out.printf("L'addetto allo sportello %d e' al lavoro\n",
                            numSportello);
        while(true)
        {
            Task task = codaSportelli.poll();

            if(task == null)
                break;

            task.run();

            System.out.printf("L'addetto allo sportello %d ha servito il " +
                              "task id %d\n", numSportello, task.getId());
        }

        System.out.printf("L'addetto allo sportello %d ha finito il lavoro!\n",
                            numSportello);
    }

}
