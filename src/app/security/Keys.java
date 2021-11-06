package app.security;

import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.SecretKey;

public class Keys {
    private final static String ALGORITMO_ASIM = "RSA";

    public static byte[] encrypt(String textoACifrar, SecretKey llave) throws Exception {
        byte[] cifrado = Symmetric.encrypt(llave, textoACifrar);
        return cifrado;
    }

    public static byte[] encrypt(String textoACifrar, PublicKey llave) throws Exception {
        byte[] cifrado = Asymmetric.encrypt(llave, ALGORITMO_ASIM, textoACifrar);
        return cifrado;
    }

    public static byte[] decrypt(byte[] textoCifrado, SecretKey llave) throws Exception {
        byte[] descifrado = Symmetric.decrypt(llave, textoCifrado);
        return descifrado;
    }

    public static byte[] decrypt(byte[] textoCifrado, PrivateKey llave) throws Exception {
        byte[] descifrado = Asymmetric.decrypt(llave, ALGORITMO_ASIM, textoCifrado);
        return descifrado;
    }

    // byte2str genera un encapsulamiento con hexadecimales
    public static byte[] str2byte(String ss) {
        byte[] ret = new byte[ss.length() / 2];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = (byte) Integer.parseInt(ss.substring(i * 2, (i + 1) * 2), 16);
        }
        return ret;
    }

    // byte2str transforma un encapsulamiento con hexadecimales
    public static String byte2str(byte[] b) {

        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String g = Integer.toHexString(((char) b[i]) & 0x00ff);
            ret += (g.length() == 1 ? "0" : "") + g;
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
