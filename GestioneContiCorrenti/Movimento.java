package GestioneContiCorrenti;

import java.io.Serializable;
import java.util.GregorianCalendar;

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

    private GregorianCalendar data;
    private Causale causale;

    Movimento(GregorianCalendar data, Causale causale)
    {
        this.data = data;
        this.causale = causale;
    }

    Causale getCausale()
    {
        return causale;
    }

    GregorianCalendar getData()
    {
        return data;
    }

}
