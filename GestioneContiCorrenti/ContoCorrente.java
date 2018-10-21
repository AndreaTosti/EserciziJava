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

    private int kBonifico;
    private int kAccredito;
    private int kBollettino;
    private int kF24;
    private int kPagoBancomat;

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
        if(causale.compareTo("Bonifico") == 0)
        {
            kBonifico++;
        }
        else if(causale.compareTo("Accredito") == 0)
        {
            kAccredito++;
        }
        else if(causale.compareTo("Bollettino") == 0)
        {
            kBollettino++;
        }
        else if(causale.compareTo("F24") == 0)
        {
            kF24++;
        }
        else if(causale.compareTo("PagoBancomat") == 0)
        {
            kPagoBancomat++;
        }
    }

    int getNumeroDiMovimenti(String causale)
    {
        if(causale.compareTo("Bonifico") == 0)
        {
            return kBonifico;
        }
        else if(causale.compareTo("Accredito") == 0)
        {
            return kAccredito;
        }
        else if(causale.compareTo("Bollettino") == 0)
        {
            return kBollettino;
        }
        else if(causale.compareTo("F24") == 0)
        {
            return kF24;
        }
        else if(causale.compareTo("PagoBancomat") == 0)
        {
            return kPagoBancomat;
        }
        else
        {
            return 0;
        }
    }
}
