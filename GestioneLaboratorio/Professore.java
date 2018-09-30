package GestioneLaboratorio;

public class Professore extends Utente
{
    public Professore(Tutor tutor, int id)
    {
        super(tutor, id);
    }

    public void usaComputer() throws InterruptedException
    {
        tutor.usaComputerDaProfessore();
    }

    public void lasciaComputer()
    {
        tutor.lasciaComputerDaProfessore();
    }
}
