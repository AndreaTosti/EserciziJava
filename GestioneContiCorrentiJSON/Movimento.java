package GestioneContiCorrentiJSON;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;

class Movimento
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

    Movimento(@JsonProperty("data") Date data, @JsonProperty("causale") Causale causale)
    {
        this.data = data;
        this.causale = causale;
    }

    public Causale getCausale()
    {
        return causale;
    }

    public void setCausale(Causale causale)
    {
        this.causale = causale;
    }

    public Date getData()
    {
        return data;
    }

    public void setData()
    {
        this.data = data;
    }

}
