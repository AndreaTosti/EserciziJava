package GestioneContiCorrenti;

import java.io.*;
import java.util.concurrent.*;
import java.security.SecureRandom;

public class MainClass
{
    public static void main(String[] args)
    {
        /* File contenente gli oggetti ContoCorrente serializzati */
        String filename = "conticorrenti.ser";

        /* Contatore globale del numero di occorrenze di ogni causale
           all'interno di ogni conto corrente */
        Contatore contatore = new Contatore();

        /* Coda concorrente in cui il thread main inserisce gli oggetti di tipo
           ContoCorrente e da cui i thread del pool estraggono tali oggetti */
        LinkedBlockingQueue<ContoCorrente> codaContiCorrenti =
                new LinkedBlockingQueue<>();

        if(args.length != 2)
        {
            System.err.print("You must put the following 2 arguments:" +
                    " numWorkers and numContiCorrenti");
            return;
        }

        int numWorkers = Integer.parseInt(args[0]);
        int numContiCorrenti = Integer.parseInt(args[1]);

        System.out.printf("Thread[%s] Num. Thread Workers: %d - Num. Conti Correnti: "
        + "%d\n", Thread.currentThread().getName(), numWorkers, numContiCorrenti);

        /* Pool di threads */
        ExecutorService execWorkers =
                Executors.newFixedThreadPool(numWorkers);

        SecureRandom randomNumbers = new SecureRandom();

        int numTotaleCausali = 0;

        /* Per indicizzare l'enum in modo efficiente, memorizzo un array
           all'esterno del loop */
        final Movimento.Causale[] arrayCausali = Movimento.Causale.values();

        /* Genero dei conti correnti con i relativi movimenti in modo casuale */
        try(FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fos))
        {
            for(int i = 0; i < numContiCorrenti; i++)
            {
                ContoCorrente contoCorrente = new ContoCorrente("Correntista" + i);

                /* Aggiungo un numero random di movimenti */
                int numRandomMovimenti = randomNumbers.nextInt(700);
                for(int j = 0; j < numRandomMovimenti; j++)
                {
                    // Scelgo una causale random
                    numTotaleCausali++;
                    int causaleRandom =
                            randomNumbers.nextInt(arrayCausali.length);
                    contoCorrente.addMovimento(arrayCausali[causaleRandom]);
                }
                /* Scrivo su file l'oggetto serializzato */
                out.writeObject(contoCorrente);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        System.out.printf("Thread[%s] Serializzati tutti i conti correnti " +
                "(NUM. TOTALE CAUSALI = %d)\n",
                Thread.currentThread().getName(), numTotaleCausali);


        long startTime = System.currentTimeMillis();

        ContoCorrente contoCorrente = null;

        /* Try with resources */
        try(FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fis))
        {
            while(true)
            {
                try
                {
                    /* Passo gli oggetti ai thread del Pool */
                    codaContiCorrenti.put((ContoCorrente) in.readObject());
                }catch(EOFException eof)
                {
                    System.out.printf("Thread[%s] Deserializzati tutti i conti " +
                        "correnti e inseriti in coda\n",
                            Thread.currentThread().getName());
                    break;
                }
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
        contatore.printContatore(Thread.currentThread());

    }
}
