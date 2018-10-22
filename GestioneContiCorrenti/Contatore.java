package GestioneContiCorrenti;

class Contatore
{
    private int kBonifico;
    private int kAccredito;
    private int kBollettino;
    private int kF24;
    private int kPagoBancomat;

    Contatore()
    {
        kBonifico = 0;
        kAccredito = 0;
        kBollettino = 0;
        kF24 = 0;
        kPagoBancomat = 0;
    }

    synchronized void aggiornaContatore(Thread thread,
                                        int nBonifico,
                                        int nAccredito,
                                        int nBollettino,
                                        int nF24,
                                        int nPagoBancomat)
    {
        kBonifico += nBonifico;
        kAccredito += nAccredito;
        kBollettino += nBollettino;
        kF24 += nF24;
        kPagoBancomat += nPagoBancomat;
        System.out.printf("%s Contatore aggiornato : \n\t\t" +
                          "kBonifico = %d kAccredito = %d kBollettino = %d" +
                          " kF24 = %d kPagoBancomat = %d\n",
                          thread, kBonifico, kAccredito, kBollettino,
                          kF24, kPagoBancomat);
    }

    synchronized void printContatore(Thread thread)
    {
        System.out.printf("\nThread[%s] Stampa stato contatore :\n\t\t" +
            "kBonifico = %d kAccredito = %d " +
            "kBollettino = %d kF24 = %d kPagoBancomat = %d \n\t\t" +
            "(NUM. TOTALE CAUSALI = %d)\n",
            thread.getName(), kBonifico, kAccredito,
            kBollettino, kF24, kPagoBancomat,
            kBonifico + kAccredito + kBollettino + kF24 + kPagoBancomat);
    }

}
