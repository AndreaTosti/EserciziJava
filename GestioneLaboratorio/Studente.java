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
        computerId = tutor.usaComputerDaStudente();
    }

    public void lasciaComputer()
    {
        tutor.lasciaComputerDaStudente(computerId);
    }
}
