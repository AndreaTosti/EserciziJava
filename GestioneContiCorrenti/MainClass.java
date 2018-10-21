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

        final int numWorkers = 4;
        ExecutorService execWorkers =
                Executors.newFixedThreadPool(numWorkers);

        /* Try with resources */
        try(FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fos))
        {
            for(int i = 0; i < 200; i++)
            {
                ContoCorrente contoCorrente = new ContoCorrente("");
                for(int j = 0; j < 600; j++)
                {
                    if(j % 5 == 0)
                    {
                        contoCorrente.addMovimento("Bonifico");
                    }
                    else if(j % 5 == 1)
                    {
                        contoCorrente.addMovimento("Accredito");
                    }
                    else if(j % 5 == 2)
                    {
                        contoCorrente.addMovimento("Bollettino");
                    }
                    else if(j % 5 == 3)
                    {
                        contoCorrente.addMovimento("F24");
                    }
                    else if(j % 5 == 4)
                    {
                        contoCorrente.addMovimento("PagoBancomat");
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

        int b = 0;
        int a = 0;
        /* Try with resources */
        try(FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fis))
        {
            try
            {
                for(int i = 0; i < 200; i++)
                {
                    contoCorrente = (ContoCorrente)in.readObject();
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
            execWorkers.awaitTermination(1, TimeUnit.SECONDS);
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
