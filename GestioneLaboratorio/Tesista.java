package GestioneLaboratorio;

/*
 * Nome: Andrea
 * Cognome: Tosti
 * Matricola: 518111
 */

public class Tesista extends Utente
{
    /* computer in cui è installato il software utile al tesista */
    private int computerId;

    Tesista(Tutor tutor, int id, int computerId)
    {
        super(tutor, id);
        this.computerId = computerId;
    }

    public void usaComputer() throws  InterruptedException
    {
        /* Passo come parametro anche un riferimento all'oggetto di Tesista */
        tutor.usaComputerDaTesista(computerId, this);
    }

    public void lasciaComputer()
    {
        tutor.lasciaComputerDaTesista(computerId);
    }

    @Override
    public String toString()
    {
        return String.format("Tesista%d", getId());
    }

}
