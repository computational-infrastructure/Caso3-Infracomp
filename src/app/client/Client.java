package app.client;

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

public class Client {
    public static int port = 2730;
    private static String tipo;
    private static SecretKey llaveSimetrica;
    private static PrivateKey llavePrivada;
    private static PublicKey llavePublicaRepetidor;

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java Client clientId messageId type");
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
                new Thread(new ClienteDelegado(socket, args[1], args[2])).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class ClienteDelegado implements Runnable {
        Socket socket;
        String clientId;
        String messageId;
        String messageString;

        public ClienteDelegado(Socket s, String clientId, String messageId) {
            this.socket = s;
            this.clientId = clientId;
            this.messageId = messageId;
        }

        public void run() {
            try {
                requestMessage();
                getMessage();
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

        private void requestMessage() throws Exception {
            OutputStream out = socket.getOutputStream();
            PrintWriter clientPrintOut = new PrintWriter(new OutputStreamWriter(out, "UTF-8"), true);
            System.out.println("El Cliente " + clientId + " está solicitando el mensaje: " + messageId);
            if (tipo.equals("SIMETRICO")) {
                byte[] encryptedMessageId = Keys.encrypt(messageId, llaveSimetrica);
                String encapsulatedMessageId = Keys.byte2str(encryptedMessageId);
                clientPrintOut.println(encapsulatedMessageId);
            } else {
                byte[] encryptedMessageId = Keys.encrypt(messageId, llavePublicaRepetidor);
                String encapsulatedMessageId = Keys.byte2str(encryptedMessageId);
                clientPrintOut.println(encapsulatedMessageId);
            }
            out.flush();
        }

        private void getMessage() throws Exception {
            InputStream in = socket.getInputStream();
            Scanner scanner = new Scanner(in, "UTF-8");
            String message = scanner.nextLine();
            byte[] messageBytes = Keys.str2byte(message);
            if (tipo.equals("SIMETRICO")) {
                byte[] decryptedMessage = Keys.decrypt(messageBytes, llaveSimetrica);
                this.messageString = new String(decryptedMessage, StandardCharsets.UTF_8);
            } else {
                byte[] decryptedMessage = Keys.decrypt(messageBytes, llavePrivada);
                this.messageString = new String(decryptedMessage, StandardCharsets.UTF_8);
            }
            System.out.println("El Cliente " + clientId + " recibió el mensaje: " + messageString);
            scanner.close();
        }
    }
}
