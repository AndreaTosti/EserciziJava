//package GestioneContiCorrentiJSON;

import java.io.*;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.security.SecureRandom;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;

public class MainClass
{
    public static void main(String[] args)
    {
        String filename = "conticorrenti.json";

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

        System.out.printf("Thread[%s] Num. Thread Workers: %d - Num. Conti Correnti: " +
                "%d\n", Thread.currentThread().getName(), numWorkers, numContiCorrenti);

        /* Pool di threads */
        ExecutorService execWorkers = Executors.newFixedThreadPool(numWorkers);

        SecureRandom randomNumbers = new SecureRandom();

        int numTotaleCausali = 0;

        /* Per indicizzare l'enum in modo efficiente, memorizzo un array
           all'esterno del loop */
        final Movimento.Causale[] arrayCausali = Movimento.Causale.values();

        ObjectMapper mapper = new ObjectMapper();

        /* Per avere un file .json piu' leggibile */
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        /* Preparo una lista in cui inserire tutti i conti correnti generati */
        List<ContoCorrente> conti= new LinkedList<ContoCorrente>();

        /* Genero dei conti correnti con i relativi movimenti in modo casuale */
        try
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
                    int causaleRandom = randomNumbers.nextInt(arrayCausali.length);
                    contoCorrente.addMovimento(arrayCausali[causaleRandom]);
                }
                conti.add(contoCorrente);
            }
            /* Scrivo la lista dei conti correnti sul file filename */

            //mapper.writeValue(new File(filename), conti);

            byte[] contiBytes = mapper.writeValueAsBytes(conti);
            File fileJson = new File(filename);
            try(FileOutputStream fos  = new FileOutputStream(fileJson, false);
                FileChannel outchannel = fos.getChannel();)
            {
                ByteBuffer bytebuffer = ByteBuffer.wrap(contiBytes);
                outchannel.write(bytebuffer);
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        System.out.printf("Thread[%s] Serializzati tutti i conti correnti " +
                "(NUM. TOTALE CAUSALI = %d)\n",
                Thread.currentThread().getName(), numTotaleCausali);

        long startTime = System.currentTimeMillis();

        TypeReference<List<ContoCorrente>> mapType =
                    new TypeReference<List<ContoCorrente>>() {};

        List<ContoCorrente> lista = null;

        //lista = mapper.readValue(new File(filename), mapType);

        try(FileInputStream fis = new FileInputStream(filename);
            FileChannel inChannel = fis.getChannel())
        {
            ByteBuffer buf = ByteBuffer.allocate(fis.available());
            inChannel.read(buf);
            lista = mapper.readValue(buf.array(), mapType);
            for(ContoCorrente cc: lista)
            {
                codaContiCorrenti.put(cc);
            }
            System.out.printf("Thread[%s] Deserializzati tutti i conti " + "correnti e inseriti in coda\n\n", Thread.currentThread().getName());
        }catch(IOException | InterruptedException e)
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
        contatore.printContatore(Thread.currentThread(), elapsedTime);
    }
}
