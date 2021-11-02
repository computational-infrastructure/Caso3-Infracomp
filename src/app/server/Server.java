package app.server;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Scanner;
import javax.crypto.SecretKey;

import app.security.Keys;
import app.utils.Termination;
public class Server 
{
    private static int port = 1234;
    private static String[] mensajes = new String[10];
    private static String tipo;
    private static SecretKey llaveSimetrica;
    private static PrivateKey llavePrivada;
    private static PublicKey llavePublicaRepetidor;
    public static void main(String[] args)
    {
        if (args.length != 1) 
        {
            System.err.println("Usage: java server type");
            System.err.println("Valid types: [SIMETRICO|ASIMETRICO]");
            System.exit(1);
        }
        else if (args[0].equals("SIMETRICO"))
        {
            tipo = "SIMETRICO";
            try
            {
                llaveSimetrica = Keys.readSecretKey("./src/app/security/keys/simetricas/server/SymmetricKey");
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.exit(1);
            }
        }
        else if (args[0].equals("ASIMETRICO"))
        {
            tipo = "ASIMETRICO";
            try
            {
                llavePrivada = Keys.readPrivateKey("./src/app/security/keys/asimetricas/server/ServerKey.key");
                llavePublicaRepetidor = Keys.readPublicKey("./src/app/security/keys/asimetricas/repeater/RepeaterKey.pub");
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.exit(1);
            }
        }
        else
        {
            System.err.println("Invalid arguments: " + args);
            System.err.println("Valid args: SIMETRICO | ASIMETRICO");
            System.exit(1);
        }

        //Carga de Mensajes
        try 
        {
            File f = new File("./src/app/server/mensajes.txt");
            Scanner lector = new Scanner(f);
            for(int i = 0; i<10; i++)
            {
                mensajes[i] = lector.nextLine();
            }
            lector.close();
            System.out.println("Configuración cargada");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        //Inicio del servidor
        try
        {
            Runtime current = Runtime.getRuntime();
            ServerSocket serversock = new ServerSocket(port);
            current.addShutdownHook(new Termination(serversock));
            while (true) 
            {   
                Socket socket = serversock.accept();
                new Thread(new ServidorDelegado(socket)).start();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static class ServidorDelegado implements Runnable
    {
        Socket socket;
        public ServidorDelegado(Socket s){socket = s;}

        public void run()
        {
            try
            {
                InputStream inputToServer = socket.getInputStream();
                OutputStream outputFromServer = socket.getOutputStream();
                Scanner scanner = new Scanner(inputToServer, "UTF-8");
                PrintWriter serverPrintOut = new PrintWriter(new OutputStreamWriter(outputFromServer, "UTF-8"), true);
                String identificador = scanner.nextLine();
                byte[] idArray = Keys.str2byte(identificador);
                if (tipo.equals("SIMETRICO"))
                {
                    byte [] decryptedID = Keys.descifrar(tipo, idArray, llaveSimetrica);
                    String idString = new String(decryptedID, StandardCharsets.UTF_8);
                    int idMensaje = Integer.parseInt(idString);
                    String mensaje = mensajes[idMensaje];
                    byte[] encryptedMessage = Keys.cifrar(tipo, mensaje, llaveSimetrica);
                    String mensajeEncapsulado = Keys.byte2str(encryptedMessage);
                    serverPrintOut.println(mensajeEncapsulado);
                }
                else
                {
                    byte[] decryptedID = Keys.descifrar(tipo, idArray, llavePrivada);
                    String idString = new String(decryptedID, StandardCharsets.UTF_8);
                    int idMensaje = Integer.parseInt(idString);
                    String mensaje = mensajes[idMensaje];
                    byte[] encryptedMessage = Keys.cifrar(tipo, mensaje, llavePublicaRepetidor);
                    String mensajeEncapsulado = Keys.byte2str(encryptedMessage);
                    serverPrintOut.println(mensajeEncapsulado);
                }
                scanner.close();
                socket.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
