package GestioneContiCorrenti;

import GestioneLaboratorio.Professore;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class MainClass
{
    public static void main(String[] args)
    {
        /* File contentente gli oggetti ContoCorrente serializzati */
        String filename = "conticorrenti.ser";

        LinkedBlockingQueue<ContoCorrente> codaContiCorrenti =
                new LinkedBlockingQueue<>();

        /* Try with resources */
        try(FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fos))
        {
            for(int i = 0; i < 100; i++)
            {
                ContoCorrente contoCorrente = new ContoCorrente("Andrea");
                for(int j = 0; j < 100; j++)
                {
                    if(j % 2 == 0)
                    {
                        contoCorrente.addMovimento("Bonifico");
                    }
                    else
                    {
                        contoCorrente.addMovimento("Accredito");
                    }
                }
                out.writeObject(contoCorrente);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        ContoCorrente contoCorrente = null;

        int b = 0;
        int a = 0;
        /* Try with resources */
        try(FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fis))
        {
            try
            {
                for(int i = 0; i < 100; i++)
                {
                    contoCorrente = (ContoCorrente)in.readObject();
                    codaContiCorrenti.put(contoCorrente);
                    /* Passo gli oggetti ai thread del Pool */
                    //b += contoCorrente.getNumeroDiMovimenti("Accredito");
                    //a += contoCorrente.getNumeroDiMovimenti("Bonifico");
                }
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
            //System.out.printf("A = %d B = %d\n", a, b);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }


        ExecutorService execWorkers =
                Executors.newFixedThreadPool(4);
        for(int i = 0; i < 4; i++)
        {
            execWorkers.execute(new Worker(codaContiCorrenti));
        }
        execWorkers.shutdown();
    }
}
