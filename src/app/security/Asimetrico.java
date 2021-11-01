package app.security;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.*;

public class Asimetrico 
{

    public static byte[] cifrar(Key llave, String algoritmo, String texto)
    {
        byte[] textoCifrado;
        try
        {
            Cipher cifrador = Cipher.getInstance(algoritmo);
            byte[] textoClaro = texto.getBytes();

            cifrador.init(Cipher.ENCRYPT_MODE, llave);
            textoCifrado = cifrador.doFinal(textoClaro);

            return textoCifrado;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] descifrar(Key llave, String algoritmo, byte[] texto)
    {
        byte[] textoClaro;

        try
        {
            Cipher cifrador = Cipher.getInstance(algoritmo);
            cifrador.init(Cipher.DECRYPT_MODE, llave);
            textoClaro = cifrador.doFinal(texto);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        return textoClaro;
    }

    public static void imprimir(byte[] contenido)
    {
        int i = 0;
        for(; i<contenido.length; i++)
        {
            System.out.println(contenido[i] + " ");
        }
    }

    public static void main(String[] args)
    {
        writeKey("./src/app/security/keys/asimetricas/server/ServerKey");          
    }

    public static void writeKey(String output)
    {
        try
        {
            // Make object of key pair generator using RSA algorithm
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            System.out.println("KeyPairGenerator  Object :- "+kpg);
            OutputStream out;
            //set size of key
            kpg.initialize(1024);
            
            //generate pair of public and private keys
            KeyPair kp = kpg.generateKeyPair();
            
            //make public and private keys
            Key pub = kp.getPublic();
            Key pvt = kp.getPrivate();
            
            System.out.println("Generated Public key :- "+pub);
            System.out.println("Generated Private key :- "+pvt);
            
            //saving keys in binary format
            
            String outFile = output;
            out = new FileOutputStream(outFile + ".key");
            out.write(pvt.getEncoded());
            out.close();
             
            out = new FileOutputStream(outFile + ".pub");
            out.write(pub.getEncoded());
            out.close();
            
            System.err.println("Private key format in which it is created: " + pvt.getFormat());
            // prints "Private key format"
             
            System.err.println("Public key format in which it is created: " + pub.getFormat());
            // prints "Public key format"
            
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }      
    }

    public static PrivateKey readPrivateKey(String filename) throws Exception
    {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public static PublicKey readPublicKey(String filename) throws Exception
    {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
}
