/*
 * Nome: Andrea
 * Cognome: Tosti
 * Matricola: 518111
 */

import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainClass
{
    public static void main(String[] args)
    {
        /* numero di computer presenti in laboratorio */
        final int numComputerTotali = 20;

        Computer[] computers = new Computer[numComputerTotali];
        for(int i = 0; i < numComputerTotali; i++)
        {
            computers[i] = new Computer();
        }

        Tutor tutor = new Tutor(computers);

        if(args.length != 3)
        {
            System.err.print("You must put the following 3 arguments:" +
                    " numStudenti, numTesisti and numProfessori");
            return;
        }

        Integer numStudenti = Integer.parseInt(args[0]);
        Integer numTesisti = Integer.parseInt(args[1]);
        Integer numProfessori = Integer.parseInt(args[2]);

        System.out.printf("Num. Studenti: %d - Num. Tesisti: %d - " +
                          "Num. Professori: %d\n", numStudenti, numTesisti,
                           numProfessori);
        System.out.print("-------------------------------------------------" +
                "-------------\n");
        if(numStudenti > 0)
        {
            ExecutorService execStudenti =
                    Executors.newFixedThreadPool(numStudenti);
            for(int i = 0; i < numStudenti; i++)
            {
                execStudenti.execute(new Studente(tutor, i));
            }
            execStudenti.shutdown();
        }

        if(numTesisti > 0)
        {
            ExecutorService execTesisti =
                    Executors.newFixedThreadPool(numTesisti);
            for(int i = 0; i < numTesisti; i++)
            {
                SecureRandom randomNumbers = new SecureRandom();
                /* A ogni tesista assegno un computer random */
                int randomComputerId = randomNumbers.nextInt(numComputerTotali);
                execTesisti.execute(new Tesista(tutor, i, randomComputerId));
            }
            execTesisti.shutdown();
        }

        if(numProfessori > 0)
        {
            ExecutorService execProfessori =
                    Executors.newFixedThreadPool(numProfessori);
            for(int i = 0; i < numProfessori; i++)
            {
                execProfessori.execute(new Professore(tutor, i));
            }
            execProfessori.shutdown();
        }
    }
}
