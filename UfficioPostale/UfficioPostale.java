package UfficioPostale;

import java.util.LinkedList;
import java.util.concurrent.*;

public class UfficioPostale
{
    private static final int numSportelli = 4;
    private static final int capacitaCodaSportelli = 10;
    private static final int numTasks = 25;

    public static void main(String[] args)
    {
        LinkedList<Task> codaSalaAttesa = new LinkedList<>();
        LinkedBlockingQueue<Task> codaSportelli =
                new LinkedBlockingQueue<Task>(capacitaCodaSportelli);

        for(int i = 0; i < numTasks; i++)
        {
            Task task = new Task(i);
            codaSalaAttesa.add(task);
        }

        ExecutorService exec = Executors.newFixedThreadPool(numSportelli);
        for(int i = 0; i < numSportelli; i++)
        {
            exec.execute(new AddettoSportello(i, codaSportelli));
        }
        exec.shutdown();

        for(int i = 0; i < numTasks; i++)
        {
            Task task = codaSalaAttesa.poll();
            try
            {
                assert task != null;
                codaSportelli.put(task);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
