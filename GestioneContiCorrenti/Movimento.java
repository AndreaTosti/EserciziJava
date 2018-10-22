package GestioneContiCorrenti;

import java.io.Serializable;
import java.util.Date;

class Movimento implements Serializable
{
    public enum Causale
    {
        BONIFICO,
        ACCREDITO,
        BOLLETTINO,
        F24,
        PAGOBANCOMAT
    }

    private Date data;
    private Causale causale;

    Movimento(Date data, Causale causale)
    {
        this.data = data;
        this.causale = causale;
    }

    Causale getCausale()
    {
        return causale;
    }

    Date getData()
    {
        return data;
    }

}
