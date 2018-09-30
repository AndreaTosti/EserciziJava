package GestioneLaboratorio;

/*
 * Nome: Andrea
 * Cognome: Tosti
 * Matricola: 518111
 */

public class Professore extends Utente
{
    Professore(Tutor tutor, int id)
    {
        super(tutor, id);
    }

    public void usaComputer() throws InterruptedException
    {
        /* Passo come parametro anche un riferimento all'oggetto di
           Professore */
        tutor.usaComputerDaProfessore(this);
    }

    public void lasciaComputer()
    {
        tutor.lasciaComputerDaProfessore();
    }

    @Override
    public String toString()
    {
        return String.format("Professore%d", getId());
    }
}
