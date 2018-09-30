package GestioneLaboratorio;

public class Tesista extends Utente
{
    /* indice computer in cui è installato il software utile al tesista */
    private int computerId;

    public Tesista(Tutor tutor, int id, int computerId)
    {
        super(tutor, id);
        this.computerId = computerId;
    }

    public void usaComputer() throws  InterruptedException
    {
        tutor.usaComputerDaTesista(computerId, this);
    }

    public void lasciaComputer()
    {
        tutor.lasciaComputerDaTesista(computerId);
    }

    @Override
    public String toString()
    {
        return String.format("Tesista%d",
                getId());
    }

}
