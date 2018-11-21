package GestioneContiCorrentiJSON;

import java.util.concurrent.LinkedBlockingQueue;

class Worker implements Runnable
{
    private LinkedBlockingQueue<ContoCorrente> codaContiCorrenti;
    private Contatore contatore;

    Worker(LinkedBlockingQueue<ContoCorrente> codaContiCorrenti,
           Contatore contatore)
    {
        this.codaContiCorrenti = codaContiCorrenti;
        this.contatore = contatore;
    }

    @Override
    public void run()
    {
        int kBonifico = 0;
        int kAccredito = 0;
        int kBollettino = 0;
        int kF24 = 0;
        int kPagobancomat = 0;

        ContoCorrente contoCorrente;

        while((contoCorrente = codaContiCorrenti.poll()) != null)
        {
            for(Movimento m : contoCorrente.getListaMovimenti())
            {
                switch(m.getCausale())
                {
                    case BONIFICO:
                        kBonifico++;
                        break;
                    case ACCREDITO:
                        kAccredito++;
                        break;
                    case BOLLETTINO:
                        kBollettino++;
                        break;
                    case F24:
                        kF24++;
                        break;
                    case PAGOBANCOMAT:
                        kPagobancomat++;
                        break;
                    default:
                        break;
                }
            }
        }
        contatore.aggiornaContatore(
                Thread.currentThread(),
                kBonifico,
                kAccredito,
                kBollettino,
                kF24,
                kPagobancomat);
    }
}
