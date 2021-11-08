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
    public static int port = 27700;
    private static SecretKey llaveSimetricaServidor;
    private static PrivateKey llavePrivada;
    private static PublicKey llavePublicaServidor;
    private static String tipo;
    private static long avg1;
    private static long avg2;
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Repeater type");
            System.err.println("Valid types: [SIMETRICO|ASIMETRICO]");
            System.exit(1);
        } else if (args[0].toUpperCase().equals("SIMETRICO")) {
            tipo = "SIMETRICO";
            try {
                llaveSimetricaServidor = Keys.readSecretKey("./src/app/security/keys/symmetric/server/ServerKey");
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
            System.out.println("Repeater listening on port " + port + "...");
            long i = 1;
            while (true) {
                Socket socket = serversock.accept();
                new Thread(new RepetidorDelegado(socket, i)).start();
                i+=1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Inicio del Repetidor
    public static class RepetidorDelegado implements Runnable {
        Socket socket;
        private long repeaterID;
        private SecretKey llaveSimetricaCliente;
        private PublicKey llavePublicaCliente;
        private long inst1;
        private long inst2;
        private long inst3;
        private long inst4;
        public RepetidorDelegado(Socket s, long repeaterID) 
        {
            socket = s;
            this.repeaterID = repeaterID;
        }
        public void run() {
            try {
                InputStream inputToRepeater = socket.getInputStream();
                Scanner scanner = new Scanner(inputToRepeater, "UTF-8");
                String encryptedIDString = Keys.byte2str(getClientRequestID(scanner));
                String message = requestMessageToServer(encryptedIDString);
                sendMessageToClient(message);
                scanner.close();
                socket.close();
                System.out.println("Request No: " + repeaterID + " - Tiempo de recepción de solicitud, hasta antes de envío a servidor: "+ (inst2-inst1) + " ns");
                System.out.println("Request No: " + repeaterID + " - Tiempo de recepción de mensaje, hasta antes de envío a cliente: "+ (inst4-inst3) + " ns");
                avg1+=inst2-inst1;
                avg2+=inst4-inst3;
                System.out.println("Request No: " + repeaterID + " - Promedio de ejecuciones [1]: "+ avg1/repeaterID + "ns");
                System.out.println("Request No: " + repeaterID + " - Promedio de ejecuciones [2]: "+ avg2/repeaterID + "ns");
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        private byte[] getClientRequestID(Scanner scanner) throws Exception {
            String identificador = scanner.nextLine();
            System.out.println("El cliente " + identificador + " está solicitando una conexión");
            int identificadorCliente = Integer.parseInt(identificador);
            byte[] encryptedID;
            OutputStream outputToClient = socket.getOutputStream();
            PrintWriter repeaterPrintOut = new PrintWriter(new OutputStreamWriter(outputToClient, "UTF-8"), true);
            repeaterPrintOut.println("OK");
            if (tipo.equals("SIMETRICO")) {
                llaveSimetricaCliente = Keys.readSecretKey(
                        "./src/app/security/keys/symmetric/clients/Client" + identificadorCliente + "Key");
                String idMensajeString = scanner.nextLine();
                inst1 = System.nanoTime();
                System.out.println("El ID del mensaje requerido por el cliente ha llegado");
                byte[] idMensajeRaw = Keys.str2byte(idMensajeString);
                byte[] decryptedID = Keys.decrypt(idMensajeRaw, llaveSimetricaCliente);
                String idString = new String(decryptedID, StandardCharsets.UTF_8);
                encryptedID = Keys.encrypt(idString, llaveSimetricaServidor);
            } else {
                llavePublicaCliente = Keys.readPublicKey(
                        "./src/app/security/keys/asymmetric/clients/Client" + identificadorCliente + "Key.pub");
                String idMensajeString = scanner.nextLine();
                inst1 = System.nanoTime();
                byte[] idMensajeRaw = Keys.str2byte(idMensajeString);
                byte[] decryptedID = Keys.decrypt(idMensajeRaw, llavePrivada);
                String idString = new String(decryptedID, StandardCharsets.UTF_8);
                encryptedID = Keys.encrypt(idString, llavePublicaServidor);
            }
            return encryptedID;
        }

        private String requestMessageToServer(String encryptedIDString) throws Exception {
            Socket conexionServer = new Socket("127.0.0.1", Server.port);
            InputStream inputToRepeaterFromServer = conexionServer.getInputStream();
            OutputStream outputToServer = conexionServer.getOutputStream();
            Scanner serverScanner = new Scanner(inputToRepeaterFromServer, "UTF-8");
            PrintWriter repeaterPrintToServer = new PrintWriter(new OutputStreamWriter(outputToServer, "UTF-8"), true);
            String mensajeRecibido = "No se logró obtener el mensaje";
            inst2 = System.nanoTime();
            repeaterPrintToServer.println(encryptedIDString);
            String mensajeEncapsulado = serverScanner.nextLine();
            inst3 = System.nanoTime();
            System.out.println("Se ha recibido el mensaje del servidor con el ID encriptado: " + encryptedIDString);
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
            serverScanner.close();
            conexionServer.close();
            inst4 = System.nanoTime();
            return mensajeRecibido;
        }

        private void sendMessageToClient(String mensajeRecibido) throws Exception {
            OutputStream outputToClient = socket.getOutputStream();
            PrintWriter repeaterPrintOut = new PrintWriter(new OutputStreamWriter(outputToClient, "UTF-8"), true);
            repeaterPrintOut.println(mensajeRecibido);
            System.out.println("Mensaje encriptado enviado al cliente: " + mensajeRecibido + "\n");
        }
    }
}
