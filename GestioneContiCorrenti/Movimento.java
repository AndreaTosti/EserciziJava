package GestioneContiCorrenti;

import java.io.Serializable;
import java.util.Date;

class Movimento implements Serializable
{
    private Date data;
    private String causale;

    Movimento(Date data, String causale)
    {
        this.data = data;
        this.causale = causale;
    }

    String getCausale()
    {
        return causale;
    }

    Date getData()
    {
        return data;
    }
}
