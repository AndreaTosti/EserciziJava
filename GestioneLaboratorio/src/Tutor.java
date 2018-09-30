/* Il tutor coordina gli accessi al laboratorio.
 * Il tutor deve avere dei metodi synchronized per
 * poter fornire i computer agli utenti
 */
class Tutor
{
    private Computer[] computers;

    private int numComputerTotali;

    private int numComputerOccupati;

    private int numProfessoriInAttesa;

    private int[] numTesistiInAttesa;

    Tutor(Computer[] computers)
    {
        this.computers = computers;
        this.numComputerTotali = computers.length;
        this.numProfessoriInAttesa = 0;
        this.numTesistiInAttesa = new int[numComputerTotali];
    }

    /*
     * @returns -1 se non è disponibile una postazione libera per uno studente
     *          intero >= 0 che è l'indice della postazione libera per
     *          lo studente
     */
    private int ottieniIndicePostazioneLiberaPerStudente()
    {
        if(numComputerOccupati == numComputerTotali)
            return -1;

        if(numProfessoriInAttesa > 0)
            return -1;

        for(int i = 0; i < numComputerTotali; i++)
        {
            if(!computers[i].isBeingUsed() && numTesistiInAttesa[i] == 0)
            {
                return i;
            }
        }
        return -1;
    }
    /* Il lock viene effettuato sull'istanza di tutor
     * @returns id del computer che lo studente può utilizzare
     */
    synchronized int usaComputerDaStudente() throws InterruptedException
    {
        /* I professori e i tesisti hanno priorità sugli studenti */
        int indicePostazioneLibera;
        while((indicePostazioneLibera =
                ottieniIndicePostazioneLiberaPerStudente()) == -1)
        {
            wait();
        }
        computers[indicePostazioneLibera].useComputer();
        numComputerOccupati++;
        return indicePostazioneLibera;
    }

    synchronized void lasciaComputerDaStudente(int id)
    {
        computers[i].leaveComputer();
        numComputerOccupati--;
        /* Faccio sapere a tutti gli utenti che si è liberato un computer */
        notifyAll();
    }

    synchronized void usaComputerDaTesista(int idComputer)
            throws InterruptedException
    {
        /* I professori hanno priorità sui tesisti e sugli studenti
         * I tesisti hanno priorità sugli studenti
         */
        numTesistiInAttesa[idComputer]++;
        while(numProfessoriInAttesa > 0 ||
                computers[idComputer].isBeingUsed())
        {
            wait();
        }
        /* Sono stato risvegliato, sicuramente il pc è libero */
        numComputerOccupati++;
        computers[idComputer].useComputer();
    }

    synchronized void lasciaComputerDaTesista(int idComputer)
    {
        numTesistiInAttesa[idComputer]--;
        numComputerOccupati--;
        computers[idComputer].leaveComputer();
        /* Faccio sapere a tutti gli utenti che si è liberato un computer */
        notifyAll();
    }

    synchronized void usaComputerDaProfessore()
            throws InterruptedException
    {
        /* I professori hanno priorità sui tesisti e sugli studenti */
        numProfessoriInAttesa++;
        while(numComputerOccupati > 0)
        {
            wait();
        }
        for(int i = 0; i < numComputerTotali; i++)
        {
            computers[i].useComputer();
        }
        numComputerOccupati = numComputerTotali;
    }

    synchronized void lasciaComputerDaProfessore()
    {
        numProfessoriInAttesa--;
        for(int i = 0; i < numComputerTotali; i++)
        {
            computers[i].leaveComputer();
        }
        numComputerOccupati = 0;
        /* Faccio sapere a tutti gli utenti tutti i computer sono liberi */
        notifyAll();
    }
}
