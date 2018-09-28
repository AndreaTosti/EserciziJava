package GestioneLaboratorio;

import java.util.concurrent.TimeUnit;

class Utente implements Runnable, Comparable<Utente>
{
    private int priority;
    private String nome;

    Utente(String nome, int priority)
    {
        this.nome = nome;
        this.priority = priority;
    }

    private int getPriority()
    {
        return priority;
    }

    @Override
    public int compareTo(Utente utente)
    {
        return Integer.compare(utente.getPriority(), this.getPriority());
    }

    @Override
    public void run()
    {
        System.out.printf("Utente con nome %s ha priorita' %d\n", nome,
                           priority);
        try
        {
            TimeUnit.SECONDS.sleep(2);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}
