package app.client;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import app.security.Keys;

public class Client {
    private static int port = 1234;
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
                llaveSimetrica = Keys.readSecretKey("./src/app/security/keys/symmetric/client/Client" + args[1] + "Key.key");
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else if (args[0].toUpperCase().equals("ASIMETRICO")) {
            tipo = "ASIMETRICO";
            try {
                llavePrivada = Keys.readPrivateKey("./src/app/security/keys/asymmetric/client/Client" + args[1] + "Key.key");
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
    }
}