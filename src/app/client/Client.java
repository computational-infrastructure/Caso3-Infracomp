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
    private static String tipo;
    private static SecretKey llaveSimetrica;
    private static PrivateKey llavePrivada;
    private static PublicKey llavePublicaRepetidor;

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java Client type clientID messageID");
            System.err.println("Valid types: [SIMETRICO|ASIMETRICO]");
            System.exit(1);
        } else if (Integer.parseInt(args[2]) > 9 || Integer.parseInt(args[2]) < 0) {
            System.err.println("Invalid arguments:");
            System.err.println("messageID must be between 00 and 09");
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
                        .readPrivateKey("./src/app/security/keys/asymmetric/clients/Client" + args[1] + "Key.key");
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
                InputStream in = conexionRepeater.getInputStream();
                Scanner scanner = new Scanner(in, "UTF-8");

                requestMessageToRepeater(conexionRepeater, scanner);
                getMessage(conexionRepeater, scanner);

                conexionRepeater.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void requestMessageToRepeater(Socket conexionRepeater, Scanner scanner) throws Exception {
            OutputStream outputToRepeater = conexionRepeater.getOutputStream();
            PrintWriter clientPrintToRepeater = new PrintWriter(new OutputStreamWriter(outputToRepeater, "UTF-8"),
                    true);
            System.out.println("El Cliente " + clientID + " est?? solicitando el mensaje: " + messageID);
            clientPrintToRepeater.println(clientID);
            String statusConexion = scanner.nextLine();
            if (statusConexion.equals("OK")) {
                System.out.println("El Cliente se ha conectado al Repetidor exitosamente");
            } else {
                System.out.println("El Cliente no se ha logrado conectar al Repetidor");
                System.exit(1);
            }
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

        private void getMessage(Socket conexionRepeater, Scanner scanner) throws Exception {
            String message = scanner.nextLine();
            byte[] messageBytes = Keys.str2byte(message);
            if (tipo.equals("SIMETRICO")) {
                byte[] decryptedMessage = Keys.decrypt(messageBytes, llaveSimetrica);
                this.messageString = new String(decryptedMessage, StandardCharsets.UTF_8);
            } else {
                byte[] decryptedMessage = Keys.decrypt(messageBytes, llavePrivada);
                this.messageString = new String(decryptedMessage, StandardCharsets.UTF_8);
            }
            System.out.println("El Cliente " + clientID + " recibio el mensaje: " + messageString);
            scanner.close();
        }
    }
}
