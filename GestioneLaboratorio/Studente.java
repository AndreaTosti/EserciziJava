package GestioneLaboratorio;

public class Studente extends Utente
{
    private int computerId;

    public Studente(Tutor tutor, int id)
    {
        super(tutor, id);
    }

    public void usaComputer() throws InterruptedException
    {
        computerId = tutor.usaComputerDaStudente(this);
    }

    public void lasciaComputer()
    {
        tutor.lasciaComputerDaStudente(computerId);
    }

    @Override
    public String toString()
    {
        return String.format("Studente%d",
                getId());
    }
}
