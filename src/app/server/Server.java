package app.server;

import java.io.File;
import java.io.IOException;
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

public class Server {
    public static int port = 1234;
    private static String[] mensajes = new String[10];
    private static String tipo;
    private static SecretKey llaveSimetrica;
    private static PrivateKey llavePrivada;
    private static PublicKey llavePublicaRepetidor;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Server type");
            System.err.println("Valid types: [SIMETRICO|ASIMETRICO]");
            System.exit(1);
        } else if (args[0].toUpperCase().equals("SIMETRICO")) {
            tipo = "SIMETRICO";
            try {
                llaveSimetrica = Keys.readSecretKey("./src/app/security/keys/symmetric/server/ServerKey");
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else if (args[0].toUpperCase().equals("ASIMETRICO")) {
            tipo = "ASIMETRICO";
            try {
                llavePrivada = Keys.readPrivateKey("./src/app/security/keys/asymmetric/server/ServerKey.key");
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

        // Carga de Mensajes
        try {
            File f = new File("./src/app/server/mensajes.txt");
            Scanner lector = new Scanner(f);
            for (int i = 0; i < 10; i++) {
                mensajes[i] = lector.nextLine();
            }
            lector.close();
            System.out.println("Datos del servidor cargados exitosamente");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Inicio del servidor
        try {
            Runtime current = Runtime.getRuntime();
            ServerSocket serversock = new ServerSocket(port);
            current.addShutdownHook(new Termination(serversock));
            System.out.println("El Servidor está corriendo...");
            while (true) {
                Socket socket = serversock.accept();
                new Thread(new ServidorDelegado(socket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class ServidorDelegado implements Runnable {
        Socket socket;

        public ServidorDelegado(Socket s) {
            socket = s;
        }

        public void run() {
            try {
                InputStream inputToServer = socket.getInputStream();
                OutputStream outputFromServer = socket.getOutputStream();
                Scanner scanner = new Scanner(inputToServer, "UTF-8");
                PrintWriter serverPrintOut = new PrintWriter(new OutputStreamWriter(outputFromServer, "UTF-8"), true);
                serverPrintOut.println("OK");
                String identificador = scanner.nextLine();
                byte[] idBytes = Keys.str2byte(identificador);
                if (tipo.equals("SIMETRICO")) {
                    byte[] decryptedID = Keys.decrypt(idBytes, llaveSimetrica);
                    String idString = new String(decryptedID, StandardCharsets.UTF_8);
                    int idMensaje = Integer.parseInt(idString);
                    String mensaje = mensajes[idMensaje];
                    byte[] encryptedMessage = Keys.encrypt(mensaje, llaveSimetrica);
                    String mensajeEncapsulado = Keys.byte2str(encryptedMessage);
                    serverPrintOut.println(mensajeEncapsulado);
                } else {
                    byte[] decryptedID = Keys.decrypt(idBytes, llavePrivada);
                    String idString = new String(decryptedID, StandardCharsets.UTF_8);
                    int idMensaje = Integer.parseInt(idString);
                    String mensaje = mensajes[idMensaje];
                    byte[] encryptedMessage = Keys.encrypt(mensaje, llavePublicaRepetidor);
                    String mensajeEncapsulado = Keys.byte2str(encryptedMessage);
                    serverPrintOut.println(mensajeEncapsulado);
                }
                outputFromServer.flush();
                scanner.close();
                socket.close();
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
