public class MainClass
{
    public static void main(String[] args)
    {
        final int numComputerTotali = 20;
        Computer[] computers = new Computer[numComputerTotali];
        for(int i = 0; i < numComputerTotali; i++)
        {
            computers[i] = new Computer();
        }
        System.out.print("Main: end of the program\n");

    }
}
