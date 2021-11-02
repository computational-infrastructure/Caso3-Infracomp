package app.repeater;

import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import app.utils.Termination;

public class Repeater 
{
    private static int port = 9030;
    private static SecretKey llaveSimetrica;
    private static PrivateKey llavePrivada;
    private static PublicKey llavePublicaCliente;

    public static void main(String[] args)
    {
        try
        {
            Runtime current = Runtime.getRuntime();
            ServerSocket serversock = new ServerSocket(port);
            current.addShutdownHook(new Termination(serversock));
            while (true) 
            {   
                Socket socket = serversock.accept();
                new Thread(new RepetidorDelegado(socket)).start();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static class RepetidorDelegado implements Runnable
    {
        Socket socket;
        public RepetidorDelegado(Socket s) {s = socket;}
        public void run()
        {
            
        }
    }
}
