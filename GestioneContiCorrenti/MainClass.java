package GestioneContiCorrenti;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MainClass
{
    public static void main(String[] args)
    {
        /* File contenente gli oggetti ContoCorrente serializzati */
        String filename = "conticorrenti.ser";

        Contatore contatore = new Contatore();

        LinkedBlockingQueue<ContoCorrente> codaContiCorrenti =
                new LinkedBlockingQueue<>();

        if(args.length != 2)
        {
            System.err.print("You must put the following 2 arguments:" +
                    " numWorkers and numContiCorrenti");
            return;
        }

        Integer numWorkers = Integer.parseInt(args[0]);
        Integer numContiCorrenti = Integer.parseInt(args[1]);

        ExecutorService execWorkers =
                Executors.newFixedThreadPool(numWorkers);

        /* Try with resources */
        try(FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fos))
        {
            for(int i = 0; i < numContiCorrenti; i++)
            {
                ContoCorrente contoCorrente = new ContoCorrente("Correntista" + i);
                for(int j = 0; j < 600; j++)
                {
                    if(j % 5 == 0)
                    {
                        contoCorrente.addMovimento(Movimento.Causale.BONIFICO);
                    }
                    else if(j % 5 == 1)
                    {
                        contoCorrente.addMovimento(Movimento.Causale.ACCREDITO);
                    }
                    else if(j % 5 == 2)
                    {
                        contoCorrente.addMovimento(Movimento.Causale.BOLLETTINO);
                    }
                    else if(j % 5 == 3)
                    {
                        contoCorrente.addMovimento(Movimento.Causale.F24);
                    }
                    else if(j % 5 == 4)
                    {
                        contoCorrente.addMovimento(Movimento.Causale.PAGOBANCOMAT);
                    }
                }
                out.writeObject(contoCorrente);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        System.out.println("MAIN: Inseriti tutti i conti correnti e i relativi" +
                            " movimenti");

        long startTime = System.currentTimeMillis();

        ContoCorrente contoCorrente = null;

        /* Try with resources */
        try(FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fis))
        {
            try
            {
                while((contoCorrente = (ContoCorrente)in.readObject()) != null)
                {
                    /* Passo gli oggetti ai thread del Pool */
                    codaContiCorrenti.put(contoCorrente);
                }
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        for(int i = 0; i < numWorkers; i++)
        {
            execWorkers.execute(new Worker(codaContiCorrenti, contatore));
        }

        execWorkers.shutdown();

        try
        {
            execWorkers.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println(elapsedTime + "ms");
        contatore.printContatore();

    }
}
