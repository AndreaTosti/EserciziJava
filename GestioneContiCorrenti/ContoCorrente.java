package GestioneContiCorrenti;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

class ContoCorrente implements Serializable
{
    private static final long serialVersionUID = 1;

    private String nomeCorrentista;     /* il nome del correntista */
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

    void addMovimento(String causale)
    {
        Date currentDate = Calendar.getInstance().getTime();
        Movimento m = new Movimento(currentDate, causale);
        listaMovimenti.add(m);
    }

    int getNumeroDiMovimenti(String causale)
    {
        int k = 0;
        for(Movimento m : listaMovimenti)
        {
            if(m.getCausale().compareTo(causale) == 0)
            {
                k++;
            }
        }
        return k;
    }
}
