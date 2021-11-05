package app.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import app.security.Keys;
import app.utils.Termination;

public class Client {
    private static int port = 2730;
    private static String tipo;
    private static SecretKey llaveSimetrica;
    private static PrivateKey llavePrivada;
    private static PublicKey llavePublicaRepetidor;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java Client clientId type");
            System.err.println("Valid types: [SIMETRICO|ASIMETRICO]");
            System.exit(1);
        } else if (args[0].toUpperCase().equals("SIMETRICO")) {
            tipo = "SIMETRICO";
            try {
                llaveSimetrica = Keys
                        .readSecretKey("./src/app/security/keys/symmetric/client/Client" + args[1] + "Key.key");
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else if (args[0].toUpperCase().equals("ASIMETRICO")) {
            tipo = "ASIMETRICO";
            try {
                llavePrivada = Keys
                        .readPrivateKey("./src/app/security/keys/asymmetric/client/Client" + args[1] + "Key.key");
                llavePublicaRepetidor = Keys
                        .readPublicKey("./src/app/security/keys/asymmetric/repeater/RepeaterKey.pub");
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            System.err.println("Invalid arguments: " + args);
            System.err.println("Valid args: SIMETRICO | ASIMETRICO");
            System.exit(1);
        }
        try {
            Runtime current = Runtime.getRuntime();
            ServerSocket serversock = new ServerSocket(port);
            current.addShutdownHook(new Termination(serversock));
            System.out.println("El Cliente " + args[1] + " está listo para solicitar un mensaje...");
            while (true) {
                Socket socket = serversock.accept();
                new Thread(new ClienteDelegado(socket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class ClienteDelegado implements Runnable {
        Socket socket;

        public ClienteDelegado(Socket s) {
            socket = s;
        }

        public void run() {
            try {

            } catch (Exception e) {
                e.printStackTrace();
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}