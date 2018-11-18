//package GestioneContiCorrentiJSON;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

class ContoCorrente implements Serializable
{
    /* il nome del correntista */
    private String nomeCorrentista;

    /* lista dei movimenti per il conto corrente */
    @JsonProperty(value = "listaMovimenti") LinkedList<Movimento> listaMovimenti;

    ContoCorrente(@JsonProperty("nomeCorrentista") String nomeCorrentista)
    {
        this.nomeCorrentista = nomeCorrentista;
        this.listaMovimenti = new LinkedList<>();
    }

    public String getNomeCorrentista()
    {
        return nomeCorrentista;
    }

    public void setNomeCorrentista(String nomeCorrentista)
    {
        this.nomeCorrentista = nomeCorrentista;
    }

    public void addMovimento(Movimento.Causale causale)
    {
        Date currentDate = Calendar.getInstance().getTime();
        Movimento m = new Movimento(currentDate, causale);
        listaMovimenti.add(m);
    }

    public LinkedList<Movimento> getListaMovimenti()
    {
        return listaMovimenti;
    }

    public void setListaMovimenti(LinkedList<Movimento> listaMovimenti)
    {
        this.listaMovimenti = listaMovimenti;
    }
}
