import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class MainClass
{
    public static void main(String[] args)
    {
        /* Numero dei threads del threadpool */
        final int numThreads = 32;

        /* Nome del file creato con gli indirizzi ip risolti */
        String nomeNuovoFile = "web.log.multithread.518111.txt";

        Path pathNuovoWeblog = Paths.get(nomeNuovoFile);

        if(args.length != 1)
        {
            System.err.print("You must put the following 1 argument:" + " filename");
            return;
        }
        if(!Files.exists(Paths.get(args[0]).toAbsolutePath()))
        {
            System.out.printf("The file %s does not exist in the path : %s\n",
                    args[0], Paths.get(args[0]).toAbsolutePath());
            System.out.println("Exiting ...");
            return;
        }
        else
        {
            System.out.printf("The file %s has been found in the path : %s\n",
                    args[0], Paths.get(args[0]).toAbsolutePath());
            System.out.printf("The converted file will be available in the path : %s\n",
                    pathNuovoWeblog.toAbsolutePath());
            System.out.println("--------------------------------------------" +
                    "-------------------------------------------------------" +
                    "-------------------------------------------------------");
        }

        /* Lista di righe da passare ai thread workers */
        LinkedBlockingQueue<String> lineeIn = new LinkedBlockingQueue<>();

        /* Lista di righe convertite dai threads workers */
        LinkedBlockingQueue<String> lineeOut = new LinkedBlockingQueue<>();

        if(numThreads > 0)
        {
            ExecutorService execReaders =
                    Executors.newFixedThreadPool(numThreads);
            for(int i = 0; i < numThreads; i++)
            {
                execReaders.execute(new Task(lineeIn, lineeOut));
            }
            execReaders.shutdown();
        }
        else
        {
            System.out.println("0 threads disponibili nel threadpool");
            return;
        }

        /* Lista delle righe lette dal file in input*/
        List<String> allLines = null;

        try
        {
            allLines = Files.readAllLines(Paths.get(args[0]).toAbsolutePath());
        }catch(IOException | NullPointerException e)
        {
            e.printStackTrace();
        }

        /* Misuro il tempo per il confronto con la versione singlethread */
        long startTime = System.currentTimeMillis();

        for(String s : allLines){
            try
            {
                /* Passo le righe ai thread workers */
                lineeIn.put(s);
            }catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        for(int i = 0; i < numThreads; i++)
        {
            try
            {
                /* Placeholder per terminare ciascun thread del threadpool */
                lineeIn.put(System.lineSeparator()); /* di solito \n */
            }catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        /* Se il file esiste già, allora rimuovilo */
        try
        {
            if(Files.exists(pathNuovoWeblog))
                Files.delete(pathNuovoWeblog);
        } catch (NoSuchFileException e)
        {
            System.err.format("%s: No such" + " file or directory%n"    , pathNuovoWeblog);
        } catch (DirectoryNotEmptyException e)
        {
            System.err.format("%s Not empty%n", pathNuovoWeblog);
        } catch (IOException e)
        {
            System.err.println(e);
        }

        try
        {
            /* Itera un numero di volte pari al numero di linee del file
               originale in input */
            for(int i = 0; i < allLines.size(); i++)
            {
                /* Estraggo le linee convertite dai thread del pool */
                String linea = lineeOut.take();

                /* Scrivo sul file pathNuovoWeblog */
                Files.write(pathNuovoWeblog,
                        (linea + System.lineSeparator()).getBytes("UTF-8"),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);

                /* Stampo anche a schermo */
                System.out.println(linea);
            }
        }
        catch(InterruptedException | IOException e)
        {
            e.printStackTrace();
        }
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;

        System.out.println("--------------------------------------------" +
                "-------------------------------------------------------" +
                "-------------------------------------------------------");
        System.out.printf("The converted file will be available in the path : %s\n",
                pathNuovoWeblog.toAbsolutePath());
        System.out.printf("Tempo impiegato dal pool di thread : %d ms\n",
                elapsedTime);
    }
}
