package GestioneContiCorrenti;

import java.io.Serializable;
import java.util.LinkedList;

public class ContoCorrente implements Serializable
{
    private static final long serialVersionUID = 1;

    String nomeCorrentista;     /* il nome del correntista */
    LinkedList<Movimento> listaMovimenti;

}
