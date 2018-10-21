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
        kBollettino += nAccredito;
        kF24 += nF24;
        kPagoBancomat += nPagoBancomat;
        System.out.printf("Contatore aggiornato dal thread %s : \n\t\t" +
                          "kBonifico = %d kAccredito = %d kBollettino = %d" +
                          " kF24 = %d kPagoBancomat = %d\n",
                          thread, kBonifico, kAccredito, kBollettino,
                          kF24, kPagoBancomat);

    }

    synchronized void printContatore()
    {
        System.out.printf("\nkBonifico = %d kAccredito = %d kBollettino = %d" +
                          " kF24 = %d kPagoBancomat = %d",
                            kBonifico, kAccredito, kBollettino,
                            kF24, kPagoBancomat);
    }

}
