package GestioneContiCorrenti;

import java.util.concurrent.LinkedBlockingQueue;

class Worker implements Runnable
{
    LinkedBlockingQueue<ContoCorrente> codaContiCorrenti;

    Worker(LinkedBlockingQueue<ContoCorrente> codaContiCorrenti)
    {
        this.codaContiCorrenti = codaContiCorrenti;
    }
    @Override
    public void run()
    {
        int k = 0;
        String filename = "conticorrenti.ser";
        ContoCorrente contoCorrente;
        while((contoCorrente = codaContiCorrenti.poll()) != null)
        {
            k += contoCorrente.getNumeroDiMovimenti("Bonifico");
            k += contoCorrente.getNumeroDiMovimenti("Accredito");
        }
        System.out.println(k);
    }
}
