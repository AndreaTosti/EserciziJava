package Weblog.Singlethread_version;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainClass
{
    public static void main(String[] args)
    {
        /* Nome del file creato con gli indirizzi ip risolti */
        String nomeNuovoFile = "web.log.singlethread.518111.txt";

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
        /* Lista delle righe lette dal file in input*/
        List<String> allLines = null;

        try
        {
            allLines = Files.readAllLines(Paths.get(args[0]).toAbsolutePath());
        }catch(IOException | NullPointerException e)
        {
            e.printStackTrace();
        }

        /* Misuro il tempo per il confronto con la versione multithread */
        long startTime = System.currentTimeMillis();

        /* Se il file esiste già, allora rimuovilo */
        try
        {
            if(Files.exists(pathNuovoWeblog))
                Files.delete(pathNuovoWeblog);
        } catch (NoSuchFileException e)
        {
            System.err.format("%s: No such" + " file or directory%n", pathNuovoWeblog);
        } catch (DirectoryNotEmptyException e)
        {
            System.err.format("%s Not empty%n", pathNuovoWeblog);
        } catch (IOException e)
        {
            System.err.println(e);
        }

        final String ipv4Pattern = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
        Pattern pattern = Pattern.compile(ipv4Pattern);
        for(String s : allLines)
        {
            Matcher matcher = pattern.matcher(s);
            final String[] secondPart = s.split(ipv4Pattern);
            InetAddress address = null;
            try
            {
                if (matcher.find())
                {
                    /* Risolvo l'indirizzo IP */
                    /* matcher.group(0) è l'IP, secondPart[1] e il resto della riga */
                    address = InetAddress.getByName(matcher.group(0));
                    /* Scrivo sul file pathNuovoWeblog */
                    Files.write(pathNuovoWeblog,
                        (address.getHostName() + secondPart[1] +
                        System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    /* Stampo anche a schermo */
                    System.out.println(address.getHostName() + secondPart[1]);
                }
            }catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;

        System.out.println("--------------------------------------------" +
                "-------------------------------------------------------" +
                "-------------------------------------------------------");
        System.out.printf("The converted file will be available in the path : %s\n",
                pathNuovoWeblog.toAbsolutePath());
        System.out.printf("Tempo impiegato dal main thread : %d ms\n",
                elapsedTime);
    }
}
