package Weblog;

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
        final int numThreads = 4;

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
        }

        /* Lista di righe da passare ai thread workers */
        LinkedBlockingQueue<String> lineeIn = new LinkedBlockingQueue<>();

        /* Lista di righe processate dai threads workers */
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

        List<String> allLines = null;
        try
        {
            allLines = Files.readAllLines(Paths.get(args[0]).toAbsolutePath());
        }catch(IOException e)
        {
            e.printStackTrace();
        }
        if(allLines == null)
        {
            System.out.println("Il file è vuoto, chiusura del programma...");
            return;
        }

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
                lineeIn.put("\n");    // Placeholder per terminare i threads
            }catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        File file = new File("web.log.replaced.txt");
        Path pathNuovoWeblog = Paths.get("web.log.replaced.txt");
        try {
            if(Files.exists(pathNuovoWeblog))
                Files.delete(pathNuovoWeblog);
        } catch (NoSuchFileException x) {
            System.err.format("%s: no such" + " file or directory%n", pathNuovoWeblog);
        } catch (DirectoryNotEmptyException x) {
            System.err.format("%s not empty%n", pathNuovoWeblog);
        } catch (IOException x) {
            // File permission problems are caught here.
            System.err.println(x);
        }

        try
        {
            int j = 0;
            for(int i = 0; i < allLines.size(); i++)
            {
                String linea = lineeOut.take();
                Files.write(Paths.get("web.log.replaced.txt"),
                        (linea + System.lineSeparator()).getBytes("UTF-8"),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                System.out.println(linea);
            }
        }
        catch(InterruptedException | IOException e)
        {
            e.printStackTrace();
        }
    }
}
