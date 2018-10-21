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
        int bonifico = 0;
        int accredito = 0;
        int bollettino = 0;
        int f24 = 0;
        int pagobancomat = 0;
        ContoCorrente contoCorrente;
        while((contoCorrente = codaContiCorrenti.poll()) != null)
        {
            bonifico += contoCorrente.getNumeroDiMovimenti("Bonifico");
            accredito += contoCorrente.getNumeroDiMovimenti("Accredito");
            bollettino += contoCorrente.getNumeroDiMovimenti("Bollettino");
            f24 += contoCorrente.getNumeroDiMovimenti("F24");
            pagobancomat += contoCorrente.getNumeroDiMovimenti("PagoBancomat");
        }
        contatore.aggiornaContatore(Thread.currentThread(), bonifico, accredito,
                                        bollettino, f24, pagobancomat);
    }
}
