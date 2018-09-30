package GestioneLaboratorio;

/*
 * Nome: Andrea
 * Cognome: Tosti
 * Matricola: 518111
 */

/* Il tutor coordina gli accessi al laboratorio. */

class Tutor
{
    private Computer[] computers;

    /* Numero di computer presenti in laboratorio */
    private int numComputerTotali;

    /* Numero di computer occupati in un certo istante */
    private int numComputerOccupati;

    /* Numero di professori in attesa di poter utilizzare i computer */
    private int numProfessoriInAttesa;

    /* Numero di tesisti in attesa di poter utilizzare un certo computer
       per ogni computer tengo traccia del numero dei tesisti in attesa
       per un certo computer */
    private int[] numTesistiInAttesa;

    Tutor(Computer[] computers)
    {
        this.computers = computers;
        this.numComputerTotali = computers.length;
        this.numProfessoriInAttesa = 0;
        this.numTesistiInAttesa = new int[numComputerTotali];
    }

    /* Permette di visualizzare sullo standard output lo stato di occupazione
       di tutti i computer presenti in laboratorio.
     */
    private void visualizzaLaboratorio(Utente utente, int pc)
    {
        if(utente instanceof Professore)
        {
            System.out.printf("%s sta usando tutti i PC\n", utente);
        }
        else
        {
            System.out.printf("%s sta usando il PC#%d\n", utente, pc);
        }
        System.out.print("{");
        for(int i = 0; i < numComputerTotali; i++)
        {
            if(computers[i].isBeingUsed())
            {
                System.out.print("[X]");
            }
            else
            {
                System.out.print("[ ]");
            }
        }
        System.out.print("}\n");
        System.out.print("-------------------------------------------------" +
                         "-------------\n");
    }

    /*
     *  ritorna -1 se non è disponibile una postazione libera per uno studente
     *  altrimenti ritorna intero >= 0 che è l'indice della prima postazione
     *  libera per lo studente
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

    /* ritorna l'id del computer che lo studente può utilizzare */
    synchronized int usaComputerDaStudente(Studente studente)
            throws InterruptedException
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
        visualizzaLaboratorio(studente, indicePostazioneLibera);
        return indicePostazioneLibera;
    }

    synchronized void lasciaComputerDaStudente(int id)
    {
        computers[id].leaveComputer();
        numComputerOccupati--;
        /* Faccio sapere a tutti gli utenti che si è liberato un computer */
        notifyAll();
    }

    synchronized void usaComputerDaTesista(int idComputer, Tesista tesista)
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
        visualizzaLaboratorio(tesista, idComputer);
    }

    synchronized void lasciaComputerDaTesista(int idComputer)
    {
        numTesistiInAttesa[idComputer]--;
        numComputerOccupati--;
        computers[idComputer].leaveComputer();
        /* Faccio sapere a tutti gli utenti che si è liberato un computer */
        notifyAll();
    }

    synchronized void usaComputerDaProfessore(Professore professore)
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
        visualizzaLaboratorio(professore, -1);
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
