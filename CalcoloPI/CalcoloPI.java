package CalcoloPI;

public class CalcoloPI implements Runnable
{
    private Double accuracy;
    private long maxWaitingTime;
    private double piStimato = 0.0;
    private int n = 0;
    private Thread th;

    private CalcoloPI(Double accuracy, Thread th)
    {
        this.accuracy = accuracy;
        this.th = th;
    }

    @Override
    public void run()
    {
        //Differenza tra il valore stimato di PI.GRECO (piStimato) e il valore
        //di Math.PI
        while(Math.abs(piStimato * 4 - Math.PI) >= accuracy)
        {
            piStimato = piStimato + (Math.pow(-1, n)) / (2 * n + 1);
            n++;
            System.out.printf("%.12f%n", piStimato * 4);
            //Se il Thread e' stato interrotto nel frattempo
            if(Thread.currentThread().isInterrupted())
            {
                System.out.println("Thread worker interrupted because of" +
                                    " maximumWaitingTime");
                break;
            }
        }
        th.interrupt();
    }

    public static void main(String[] args)
    {
        if(args.length != 2)
        {
            System.err.print("You must put the following 2 arguments:" +
                              " accuracy and maximum waiting time (seconds)");
            return;
        }
        Double accuracy = Double.parseDouble(args[0]);
        long maxWaitingTime = Long.parseLong(args[1]);
        Thread mainThread = Thread.currentThread();

        CalcoloPI task = new CalcoloPI(accuracy, mainThread);
        //Pool con un solo thread
        Thread thPI = new Thread(task);
        thPI.start();
        try
        {
            thPI.join(maxWaitingTime * 1000);
            //Se il thread thPI non ha ancora finito, interrompilo manualmente
            if(thPI.isAlive())
                thPI.interrupt();
        }
        catch(InterruptedException exception)
        {
            System.out.printf("%n Thread worker finished task before" +
                               " maxWaitingTime");
        }
    }
}