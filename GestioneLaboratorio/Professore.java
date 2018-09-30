package GestioneLaboratorio;

public class Professore extends Utente
{
    public Professore(Tutor tutor, int id)
    {
        super(tutor, id);
    }

    public void usaComputer() throws InterruptedException
    {
        tutor.usaComputerDaProfessore(this);
    }

    public void lasciaComputer()
    {
        tutor.lasciaComputerDaProfessore();
    }

    @Override
    public String toString()
    {
        return String.format("Professore%d",
                getId());
    }
}
