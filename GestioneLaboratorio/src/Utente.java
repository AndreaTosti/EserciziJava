public abstract class Utente implements Runnable
{
    protected final Tutor tutor;    //riferimento al tutor
    private final int id;         //id utente

    public Utente(Tutor tutor, int id)
    {
        this.tutor = tutor;
        this.id = id;
    }

    public abstract void usaComputer() throws InterruptedException;
    public abstract void lasciaComputer();

    public int getId()
    {
        return id;
    }

    /* ritorna la Stringa che rappresenta l'oggetto Utente */
    @Override
    public String toString()
    {
        return String.format("Utente con ID=%d", getId());
    }

    @Override
    public void run()
    {

    }
}
