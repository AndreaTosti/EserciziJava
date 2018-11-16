package GestioneContiCorrentiJSON;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.*;
import java.security.SecureRandom;
import java.nio.*;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
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
        LinkedBlockingQueue<ContoCorrente> codaContiCorrenti = new LinkedBlockingQueue<>();

        if(args.length != 2)
        {
            System.err.print("You must put the following 2 arguments:" + " numWorkers and numContiCorrenti");
            return;
        }

        int numWorkers = Integer.parseInt(args[0]);
        int numContiCorrenti = Integer.parseInt(args[1]);

        System.out.printf("Thread[%s] Num. Thread Workers: %d - Num. Conti Correnti: " + "%d\n", Thread.currentThread().getName(), numWorkers, numContiCorrenti);

        /* Pool di threads */
        ExecutorService execWorkers = Executors.newFixedThreadPool(numWorkers);

        SecureRandom randomNumbers = new SecureRandom();

        int numTotaleCausali = 0;

        /* Per indicizzare l'enum in modo efficiente, memorizzo un array
           all'esterno del loop */
        final Movimento.Causale[] arrayCausali = Movimento.Causale.values();

        //ObjectWriter objectMapper = new ObjectMapper().writer().withDefaultPrettyPrinter();

        ObjectMapper mapper = new ObjectMapper();
        /* Genero dei conti correnti con i relativi movimenti in modo casuale */
        try
        {
            for(int i = 0; i < numContiCorrenti; i++)
            {
                ContoCorrente contoCorrente = new ContoCorrente("Correntista" + i);
                /* Aggiungo un numero random di movimenti */
                int numRandomMovimenti = randomNumbers.nextInt(40);
                for(int j = 0; j < numRandomMovimenti; j++)
                {
                    // Scelgo una causale random
                    numTotaleCausali++;
                    int causaleRandom = randomNumbers.nextInt(arrayCausali.length);
                    contoCorrente.addMovimento(arrayCausali[causaleRandom]);
                }
                //mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
                mapper.writeValue(new File(filename), contoCorrente);
            }
            // Scrivo i conti correnti sul file filename
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        System.out.printf("Thread[%s] Serializzati tutti i conti correnti " + "(NUM. TOTALE CAUSALI = %d)\n", Thread.currentThread().getName(), numTotaleCausali);


        long startTime = System.currentTimeMillis();

        try
        {
            //LinkedList<ContoCorrente> lista2 = new LinkedList<>();
            //lista2 = mapper.readValue(new File(filename), LinkedList<Cont>.class);

            ContoCorrente program1 = mapper.readValue(new File(filename), ContoCorrente.class);
            try
            {
                //System.out.println(program1.getListaMovimenti().getFirst().getCausale());
                    codaContiCorrenti.put(program1);
            }catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }catch(IOException e)
        {
            e.printStackTrace();
        }

                    /*System.out.printf("Thread[%s] Deserializzati tutti i conti " +
                        "correnti e inseriti in coda\n\n",
                            Thread.currentThread().getName());
                    break;*/
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
