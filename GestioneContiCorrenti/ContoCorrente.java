package GestioneContiCorrenti;

import java.io.Serializable;
import java.util.GregorianCalendar;
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

    private static int randBetween(int start, int end) {
        return start + (int)Math.round(Math.random() * (end - start));
    }

    void addMovimento(Movimento.Causale causale)
    {
        GregorianCalendar gc = new GregorianCalendar();
        int year = randBetween(2016, 2018);
        gc.set(gc.YEAR, year);
        int dayOfYear = randBetween(1, gc.getActualMaximum(gc.DAY_OF_YEAR));
        gc.set(gc.DAY_OF_YEAR, dayOfYear);

        System.out.println(gc.get(gc.YEAR) + "-" + (gc.get(gc.MONTH) + 1) + "-" + gc.get(gc.DAY_OF_MONTH));

        Movimento m = new Movimento(gc, causale);
        listaMovimenti.add(m);
    }

    LinkedList<Movimento> getListaMovimenti()
    {
        return listaMovimenti;
    }
}
