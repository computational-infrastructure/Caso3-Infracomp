package app.security;

import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.SecretKey;

public class Keys {
    private final static String ALGORITMO_ASIM = "RSA";

    public static byte[] cifrar(String tipo, String textoACifrar, SecretKey llave) throws Exception {
        if (tipo.equals("SIMETRICO")) {
            byte[] cifrado = Symmetric.cifrar(llave, textoACifrar);
            return cifrado;
        } else if (tipo.equals("ASIMETRICO")) {
            throw new Exception("Método incorrecto, esta versión cifra de forma simétrica");
        } else {
            throw new Exception("El tipo de cifrado introducido no es válido.");
        }
    }

    public static byte[] cifrar(String tipo, String textoACifrar, PublicKey llave) throws Exception {
        if (tipo.equals("SIMETRICO")) {
            throw new Exception("Método incorrecto, esta versión descifra de forma asimétrica");
        } else if (tipo.equals("ASIMETRICO")) {
            byte[] cifrado = Asymmetric.cifrar(llave, ALGORITMO_ASIM, textoACifrar);
            return cifrado;
        } else {
            throw new Exception("El tipo de cifrado introducido no es válido.");
        }
    }

    public static byte[] descifrar(String tipo, byte[] textoCifrado, SecretKey llave) throws Exception {
        if (tipo.equals("SIMETRICO")) {

            byte[] descifrado = Symmetric.descifrar(llave, textoCifrado);
            return descifrado;
        } else if (tipo.equals("ASIMETRICO")) {
            throw new Exception("Método incorrecto, esta versión descifra de forma simétrica");
        } else {
            throw new Exception("El tipo de cifrado introducido no es válido.");
        }
    }

    public static byte[] descifrar(String tipo, byte[] textoCifrado, PrivateKey llave) throws Exception {
        if (tipo.equals("SIMETRICO")) {
            throw new Exception("Método incorrecto, esta versión descifra de forma asimétrica");
        } else if (tipo.equals("ASIMETRICO")) {
            byte[] descifrado = Asymmetric.descifrar(llave, ALGORITMO_ASIM, textoCifrado);
            return descifrado;
        } else {
            throw new Exception("El tipo de cifrado introducido no es válido.");
        }
    }

    public static String byte2str(byte[] b) {
        // Encapsulamiento con hexadecimales
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String g = Integer.toHexString(((char) b[i]) & 0x00ff);
            ret += (g.length() == 1 ? "0" : "") + g;
        }
        return ret;
    }

    public static byte[] str2byte(String ss) {
        // Encapsulamiento con hexadecimales
        byte[] ret = new byte[ss.length() / 2];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = (byte) Integer.parseInt(ss.substring(i * 2, (i + 1) * 2), 16);
        }
        return ret;
    }

    public static SecretKey readSecretKey(String input) throws Exception {
        return Symmetric.readKey(input, "AES");
    }

    public static PublicKey readPublicKey(String filename) throws Exception {
        return Asymmetric.readPublicKey(filename);
    }

    public static PrivateKey readPrivateKey(String filename) throws Exception {
        return Asymmetric.readPrivateKey(filename);
    }

}
