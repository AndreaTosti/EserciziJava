package GestioneContiCorrenti;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

class ContoCorrente implements Serializable
{
    /* il nome del correntista */
    private String nomeCorrentista;

    /* lista dei movimenti per il conto corrente */
    private LinkedList<Movimento> listaMovimenti;

    ContoCorrente(String nomeCorrentista)
    {
        this.nomeCorrentista = nomeCorrentista;
        this.listaMovimenti = new LinkedList<>();
    }

    String getNomeCorrentista()
    {
        return nomeCorrentista;
    }

    void addMovimento(Movimento.Causale causale)
    {
        Date currentDate = Calendar.getInstance().getTime();
        Movimento m = new Movimento(currentDate, causale);
        listaMovimenti.add(m);
    }

    LinkedList<Movimento> getListaMovimenti()
    {
        return listaMovimenti;
    }
}
