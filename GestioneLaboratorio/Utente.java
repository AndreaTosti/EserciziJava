package GestioneLaboratorio;

/*
 * Nome: Andrea
 * Cognome: Tosti
 * Matricola: 518111
 */

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

public abstract class Utente implements Runnable
{
    final Tutor tutor;    /* riferimento al tutor */
    private final int id; /* id utente */

    Utente(Tutor tutor, int id)
    {
        this.tutor = tutor;
        this.id = id;
    }

    /* metodo usato per fare una richiesta al tutor per accedere a uno o più
       computer del laboratorio */
    public abstract void usaComputer() throws InterruptedException;

    public abstract void lasciaComputer();

    int getId()
    {
        return id;
    }

    @Override
    public String toString()
    {
        return String.format("Utente%d", getId());
    }

    @Override
    public void run()
    {
        SecureRandom randomNumbers = new SecureRandom();
        /* Genero un intero tra 0 e 5 */
        int randomValue = randomNumbers.nextInt(5);
        for(int i = 0; i < randomValue; i++)
        {
            try
            {
                /* Intervallo di tempo random che intercorre tra un accesso e
                   il successivo */
                TimeUnit.MILLISECONDS.sleep(randomNumbers.nextInt(2000));

                this.usaComputer();

                /* Intervallo di tempo random di permanenza in laboratorio */
                TimeUnit.MILLISECONDS.sleep(randomNumbers.nextInt(3000));

                this.lasciaComputer();
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
