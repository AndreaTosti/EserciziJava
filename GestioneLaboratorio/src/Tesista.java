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
        tutor.usaComputerDaTesista(computerId);
    }

    public void lasciaComputer()
    {
        tutor.lasciaComputerDaTesista(computerId);
    }
}
