package GestioneLaboratorio;

/*
 * Nome: Andrea
 * Cognome: Tosti
 * Matricola: 518111
 */

class Computer
{
    private boolean occupied;

    Computer()
    {
        this.occupied = false;
    }

    boolean isBeingUsed()
    {
        return occupied;
    }

    void useComputer()
    {
        occupied = true;
    }

    void leaveComputer()
    {
        occupied = false;
    }
}
