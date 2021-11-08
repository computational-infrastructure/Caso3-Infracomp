package app.utils;

import java.io.IOException;
import java.util.Random;

public class MultipleClients 
{
    public static void main(String[] args)
    {
        try 
        {
            for (int i = 1; i<=16;i++)
            {
                Runtime.getRuntime().exec("java -cp ./bin app.client.Client ASIMETRICO "+ i + " 0"+ new Random().nextInt(10));
            }
            
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
