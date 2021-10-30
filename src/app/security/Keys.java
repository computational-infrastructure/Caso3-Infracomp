package app.security;

import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.SecretKey;

public class Keys 
{
    private final static String ALGORITMO_ASIM = "RSA";

    private final static String ALGORITMO_SIM = "AES";

    public static byte[] cifrar(String tipo, String textoACifrar, String path) throws Exception
    {
        if (tipo.equals("SIMETRICO"))
        {
            SecretKey llave = Simetrico.readKey("./keys/simetricas/"+path, ALGORITMO_SIM);
            byte [] cifrado = Simetrico.cifrar(llave, textoACifrar);
            return cifrado;
        }
        else if (tipo.equals("ASIMETRICO"))
        {
            PublicKey llave = Asimetrico.readPublicKey("./keys/asimetricas/"+path);
            byte [] cifrado = Asimetrico.cifrar(llave, ALGORITMO_ASIM, textoACifrar);
            return cifrado;
        }
        else
        {
            throw new Exception("El tipo de seguridad introducido no es válido.");
        }
    }

}
