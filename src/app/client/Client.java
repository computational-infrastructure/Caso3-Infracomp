package app.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Scanner;

import javax.crypto.SecretKey;

import app.repeater.Repeater;
import app.security.Keys;

public class Client {
    public static int port = 2730;
    private static String tipo;
    private static SecretKey llaveSimetrica;
    private static PrivateKey llavePrivada;
    private static PublicKey llavePublicaRepetidor;

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java Client type clientID messageID");
            System.err.println("Valid types: [SIMETRICO|ASIMETRICO]");
            System.exit(1);
        } else if (args[0].toUpperCase().equals("SIMETRICO")) {
            tipo = "SIMETRICO";
            try {
                llaveSimetrica = Keys
                        .readSecretKey("./src/app/security/keys/symmetric/clients/Client" + args[1] + "Key");
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else if (args[0].toUpperCase().equals("ASIMETRICO")) {
            tipo = "ASIMETRICO";
            try {
                llavePrivada = Keys
                        .readPrivateKey("./src/app/security/keys/asymmetric/clients/Client" + args[1] + "Key");
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
            new Thread(new ClienteDelegado(args[1], args[2])).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class ClienteDelegado implements Runnable {
        String clientID;
        String messageID;
        String messageString;

        public ClienteDelegado(String clientID, String messageID) {
            this.clientID = clientID;
            this.messageID = messageID;
        }

        public void run() {
            try {
                Socket conexionRepeater = new Socket("127.0.0.1", Repeater.port);
                requestMessageToRepeater(conexionRepeater);
                getMessage(conexionRepeater);
                conexionRepeater.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void requestMessageToRepeater(Socket conexionRepeater) throws Exception {
            OutputStream outputToRepeater = conexionRepeater.getOutputStream();
            PrintWriter clientPrintToRepeater = new PrintWriter(new OutputStreamWriter(outputToRepeater, "UTF-8"),
                    true);
            System.out.println("El Cliente " + clientID + " está solicitando el mensaje: " + messageID);
            clientPrintToRepeater.println(clientID);
            if (tipo.equals("SIMETRICO")) {
                byte[] encryptedMessageID = Keys.encrypt(messageID, llaveSimetrica);
                String encapsulatedMessageID = Keys.byte2str(encryptedMessageID);
                clientPrintToRepeater.println(encapsulatedMessageID);
            } else {
                byte[] encryptedMessageID = Keys.encrypt(messageID, llavePublicaRepetidor);
                String encapsulatedMessageID = Keys.byte2str(encryptedMessageID);
                clientPrintToRepeater.println(encapsulatedMessageID);
            }
        }

        private void getMessage(Socket conexionRepeater) throws Exception {
            InputStream in = conexionRepeater.getInputStream();
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
            System.out.println("El Cliente " + clientID + " recibió el mensaje: " + messageString);
            scanner.close();
        }
    }
}
