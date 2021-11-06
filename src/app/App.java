package app;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class App 
{
    public static void main(String[] args)
    {
        Scanner s = new Scanner(System.in);
        ArrayList<String> clientes = new ArrayList<String>();
        ArrayList<String> comandos = new ArrayList<String>();
        System.out.println("¿Qué tipo de cifrado quiere utilizar?");
        System.out.println(" - SIMETRICO");
        System.out.println(" - ASIMETRICO");
        String tipo = s.nextLine();
        System.out.println("¿Cuántos Clientes van a recibir mensajes?");
        int numClientes = Integer.parseInt(s.nextLine());
        try
        {
            String servidor = "java -cp ./bin app.server.Server "+tipo;
            String repetidor = "java -cp ./bin app.repeater.Repeater "+tipo;
            comandos.add(servidor);
            comandos.add(repetidor);
            for (int i = 0; i < numClientes; i++)
            {
                String cliente = "java -cp ./bin app.client.Client " + tipo;
                System.out.println("Digite el id del cliente:");
                cliente += s.nextLine() + " ";
                System.out.println("Digite el id del mensaje que quiere recuperar el cliente:");
                cliente += s.nextLine();
                clientes.add(cliente);
            }
            s.close();
            new App().execute(comandos);
            new App().execute(clientes);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void execute(ArrayList<String> comandos)
    {
        for (int i = 0 ; i < comandos.size() ; i++) 
        {
            Proceso ec = new Proceso(i, comandos.get(i));
            new Thread (ec).start();
        }   
    }

    public class Proceso implements Runnable
    {
        private int prIndex;
        private String executable;
        
        public Proceso(int k, String cmd)
        {
            prIndex = k;
            executable = cmd;
        }

        public void run()
        {
            try
            {
                Process child = Runtime.getRuntime().exec(executable);
                BufferedReader br = new BufferedReader(new InputStreamReader(child.getInputStream()));
                for (String s = br.readLine() ; s != null ; s = br.readLine())
                {
                    System.out.println ("[" + prIndex + "] " + s);
                    try
                    {
                        Thread.sleep(20);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                br.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
