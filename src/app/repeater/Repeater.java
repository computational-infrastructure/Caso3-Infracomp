package app.repeater;

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
import app.server.Server;
import app.utils.Termination;

public class Repeater {
    public static int port = 9030;
    private static SecretKey llaveSimetricaServidor;
    private static PrivateKey llavePrivada;
    private static PublicKey llavePublicaServidor;
    private static String tipo;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Repeater type");
            System.err.println("Valid types: [SIMETRICO|ASIMETRICO]");
            System.exit(1);
        } else if (args[0].toUpperCase().equals("SIMETRICO")) {
            tipo = "SIMETRICO";
            try {
                llaveSimetricaServidor = Keys.readSecretKey("./src/app/security/keys/symmetric/server/SymmetricKey");
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else if (args[0].toUpperCase().equals("ASIMETRICO")) {
            tipo = "ASIMETRICO";
            try {
                llavePrivada = Keys.readPrivateKey("./src/app/security/keys/asymmetric/repeater/RepeaterKey.key");
                llavePublicaServidor = Keys.readPublicKey("./src/app/security/keys/asymmetric/server/ServerKey.pub");
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
            System.out.println("El Repetidor está corriendo...");
            while (true) {
                Socket socket = serversock.accept();
                new Thread(new RepetidorDelegado(socket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Inicio del Repetidor
    public static class RepetidorDelegado implements Runnable {
        Socket socket;
        private SecretKey llaveSimetricaCliente;
        private PublicKey llavePublicaCliente;

        public RepetidorDelegado(Socket s) {
            socket = s;
        }

        public void run() {
            try {
                String encryptedIDString = Keys.byte2str(getClientRequestID());
                String message = requestMessageToServer(encryptedIDString);
                sendMessageToClient(message);
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

        private byte[] getClientRequestID() throws Exception {
            InputStream inputToRepeater = socket.getInputStream();
            Scanner scanner = new Scanner(inputToRepeater, "UTF-8");
            String identificador = scanner.nextLine();
            int identificadorCliente = Integer.parseInt(identificador);
            byte[] encryptedID;
            if (tipo.equals("SIMETRICO")) {
                llaveSimetricaCliente = Keys.readSecretKey(
                        "./src/app/security/keys/symmetric/client/Client" + identificadorCliente + "Key.key");
                String idMensajeString = scanner.nextLine();
                byte[] idMensajeRaw = Keys.str2byte(idMensajeString);
                byte[] decryptedID = Keys.decrypt(idMensajeRaw, llaveSimetricaCliente);
                String idString = new String(decryptedID, StandardCharsets.UTF_8);
                encryptedID = Keys.encrypt(idString, llaveSimetricaServidor);
            } else {
                llavePublicaCliente = Keys.readPublicKey(
                        "./src/app/security/keys/asymmetric/client/Client" + identificadorCliente + "Key.pub");
                String idMensajeString = scanner.nextLine();
                byte[] idMensajeRaw = Keys.str2byte(idMensajeString);
                byte[] decryptedID = Keys.decrypt(idMensajeRaw, llavePrivada);
                String idString = new String(decryptedID, StandardCharsets.UTF_8);
                encryptedID = Keys.encrypt(idString, llavePublicaServidor);
            }
            scanner.close();
            return encryptedID;
        }

        private String requestMessageToServer(String encryptedIDString) throws Exception {
            Socket conexionServer = new Socket("127.0.0.1", Server.port);
            InputStream inputToRepeaterFromServer = conexionServer.getInputStream();
            OutputStream outputToServer = conexionServer.getOutputStream();
            Scanner serverScanner = new Scanner(inputToRepeaterFromServer, "UTF-8");
            PrintWriter repeaterPrintToServer = new PrintWriter(new OutputStreamWriter(outputToServer, "UTF-8"), true);
            String mensajeRecibido = "No se logró obtener el mensaje";
            if (serverScanner.nextLine().equals("OK")) {
                repeaterPrintToServer.println(encryptedIDString);
                String mensajeEncapsulado = serverScanner.nextLine();
                byte[] mensajeEncrypted = Keys.str2byte(mensajeEncapsulado);
                if (tipo.equals("SIMETRICO")) {
                    byte[] mensajeDecrypted = Keys.decrypt(mensajeEncrypted, llaveSimetricaServidor);
                    String mensajeDecryptedString = new String(mensajeDecrypted, StandardCharsets.UTF_8);
                    byte[] mensajeReEncrypted = Keys.encrypt(mensajeDecryptedString, llaveSimetricaCliente);
                    mensajeRecibido = Keys.byte2str(mensajeReEncrypted);
                } else {
                    byte[] mensajeDecrypted = Keys.decrypt(mensajeEncrypted, llavePrivada);
                    String mensajeDecryptedString = new String(mensajeDecrypted, StandardCharsets.UTF_8);
                    byte[] mensajeReEncrypted = Keys.encrypt(mensajeDecryptedString, llavePublicaCliente);
                    mensajeRecibido = Keys.byte2str(mensajeReEncrypted);
                }
            }
            serverScanner.close();
            conexionServer.close();
            return mensajeRecibido;
        }

        private void sendMessageToClient(String mensajeRecibido) throws Exception {
            OutputStream outputToClient = socket.getOutputStream();
            PrintWriter repeaterPrintOut = new PrintWriter(new OutputStreamWriter(outputToClient, "UTF-8"), true);
            repeaterPrintOut.println(mensajeRecibido);
        }
    }
}
