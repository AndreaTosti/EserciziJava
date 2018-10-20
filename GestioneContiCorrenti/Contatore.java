package GestioneContiCorrenti;

class Contatore
{
    private int kBonifico;
    private int kAccredito;

    Contatore()
    {
        kBonifico = 0;
        kAccredito = 0;
    }

    synchronized void aggiornaContatore(int nBonifico, int nAccredito)
    {
        kBonifico += nBonifico;
        kAccredito += nAccredito;
    }

    void printContatore()
    {
        System.out.printf("kBonifico = %d kAccredito = %d\n",
                            kBonifico, kAccredito);
    }

}
