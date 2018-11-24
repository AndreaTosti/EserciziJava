import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;

public class Task implements Runnable
{
    private LinkedBlockingQueue<String> lineeIn;
    private LinkedBlockingQueue<String> lineeOut;

    Task(LinkedBlockingQueue<String> lineeIn,
         LinkedBlockingQueue<String> lineeOut)
    {
        this.lineeIn = lineeIn;
        this.lineeOut = lineeOut;
    }

    @Override
    public void run()
    {
        String linea = null;
        final String ipv4Pattern = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
        Pattern pattern = Pattern.compile(ipv4Pattern);
        try
        {
            /* Il thread termina quando gli viene inviato, dal main thread,
                \n nella coda */
            while((linea = lineeIn.take()).compareTo(System.lineSeparator()) != 0)
            {
                Matcher matcher = pattern.matcher(linea);
                final String[] secondPart = linea.split(ipv4Pattern);
                InetAddress address = null;
                try
                {
                    if (matcher.find())
                    {
                        /* Risolvo l'indirizzo IP */
                        /* matcher.group(0) è l'IP, secondPart[1] e il resto della riga */
                        address = InetAddress.getByName(matcher.group(0));
                        lineeOut.put(address.getHostName() + secondPart[1]);
                    }
                }catch(UnknownHostException e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
