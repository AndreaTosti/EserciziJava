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
           all'interno di ogni conto corrente;
           Viene aggiornato in modo concorrente dai thread del pool,
           la concorrenza è gestita mediante metodi synchronized*/
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
        System.out.printf("Thread[%s] Il file %s verrà memorizzato nella " +
            "directory %s\n", Thread.currentThread().getName(), filename,
                System.getProperty("user.dir"));
        System.out.printf("Thread[%s] Attendere ...\n",
                Thread.currentThread().getName());

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
        System.out.printf("Thread[%s] Attendere ...\n",
                Thread.currentThread().getName());

        /* Memorizzo startTime per vedere il tempo impiegato per la
           deserializzazione e all'inserimento in coda(poco rilevante rispetto
           deserializzazione) da parte del main*/
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
                        "correnti e inseriti in coda (%d ms)\n\n",
                            Thread.currentThread().getName(),
                            System.currentTimeMillis() - startTime);
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

        contatore.printContatore(Thread.currentThread());

    }
}
