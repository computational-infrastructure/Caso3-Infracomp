package app.security;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.spec.KeySpec;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class Symmetric {
    private final static String PADDING = "AES/ECB/PKCS5Padding";

    public static byte[] encrypt(SecretKey llave, String texto) {
        byte[] textoCifrado;

        try {
            Cipher cifrador = Cipher.getInstance(PADDING);
            byte[] textoClaro = texto.getBytes();

            cifrador.init(Cipher.ENCRYPT_MODE, llave);
            textoCifrado = cifrador.doFinal(textoClaro);

            return textoCifrado;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] decrypt(SecretKey llave, byte[] texto) {
        byte[] textoClaro;
        try {
            Cipher cifrador = Cipher.getInstance(PADDING);
            cifrador.init(Cipher.DECRYPT_MODE, llave);
            textoClaro = cifrador.doFinal(texto);
            return textoClaro;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            writeKey(128, "./src/app/security/keys/symmetric/server/SymmetricKey", "AES");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeKey(int keySize, String output, String algorithm) throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance(algorithm);
        kg.init(keySize);
        System.out.println();
        System.out.println("KeyGenerator Object Info: ");
        System.out.println("Algorithm = " + kg.getAlgorithm());
        System.out.println("Provider = " + kg.getProvider());
        System.out.println("Key Size = " + keySize);
        System.out.println("toString = " + kg.toString());

        SecretKey ky = kg.generateKey();
        String fl = output + ".key";
        FileOutputStream fos = new FileOutputStream(fl);
        byte[] kb = ky.getEncoded();
        fos.write(kb);
        fos.close();
        System.out.println();
        System.out.println("SecretKey Object Info: ");
        System.out.println("Algorithm = " + ky.getAlgorithm());
        System.out.println("Saved File = " + fl);
        System.out.println("Size = " + kb.length);
        System.out.println("Format = " + ky.getFormat());
        System.out.println("toString = " + ky.toString());
    }

    public static SecretKey readKey(String input, String algorithm) throws Exception {
        String fl = input + ".key";
        FileInputStream fis = new FileInputStream(fl);
        int kl = fis.available();
        byte[] kb = new byte[kl];
        fis.read(kb);
        fis.close();
        SecretKey ky = null;
        ky = new SecretKeySpec(kb, algorithm);
        return ky;
    }
}
