package GestioneContiCorrenti;

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
        int a = 0;
        int b = 0;
        ContoCorrente contoCorrente;
        while((contoCorrente = codaContiCorrenti.poll()) != null)
        {
            b += contoCorrente.getNumeroDiMovimenti("Bonifico");
            a += contoCorrente.getNumeroDiMovimenti("Accredito");
        }
        contatore.aggiornaContatore(a, b);
    }
}
