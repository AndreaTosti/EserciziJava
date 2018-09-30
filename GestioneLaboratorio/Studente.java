package GestioneLaboratorio;

/*
 * Nome: Andrea
 * Cognome: Tosti
 * Matricola: 518111
 */

public class Studente extends Utente
{
    private int computerId;

    Studente(Tutor tutor, int id)
    {
        super(tutor, id);
    }

    public void usaComputer() throws InterruptedException
    {
        /* Tengo traccia del computer che mi è stato assegnato dal tutor per
           utilizzarlo successivamente nel metodo lasciaComputer(), inoltre
           passo come parametro un riferimento all'oggetto di Studente */
        computerId = tutor.usaComputerDaStudente(this);
    }

    public void lasciaComputer()
    {
        tutor.lasciaComputerDaStudente(computerId);
    }

    @Override
    public String toString()
    {
        return String.format("Studente%d", getId());
    }
}
