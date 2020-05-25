package seguro.cliente;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

public class Cliente
{

    //---------------------
    // Constantes
    //---------------------

    /**
     * Arreglo dinamico con los algoritmos
     */
    private ArrayList<String> algos;

    /**
     * Llave secreta para cifrado simetrico
     */
    private SecretKey secretKey;

    /**
     *
     */
    private KeyPair keyPair;

    /**
     *
     */
    private X509Certificate certificate;

    /**
     * Servidor - Puerto
     */
    public static final String SERVIDOR = "localhost";

    /**
     * Cadenas de control
     */
    public static final String HOLA = "HOLA";
    public static final String ALGORITMOS = "ALGORITMOS";
    public final static String OK = "OK";
    public final static String ERROR = "ERROR";

    /**
     * Algoritmos en string
     */
    public static final String AES = "AES";
    public static final String BLOWFISH = "Blowfish";
    public static final String RSA = "RSA";
    public static final String HMACSHA1 = "HMACSHA1";
    public static final String HMACSHA256 = "HMACSHA256";
    public static final String HMACSHA384 = "HMACSHA384";
    public static final String HMACSHA512 = "HMACSHA512";

    public Cliente()
    {
        algos = new ArrayList<String>();
        try
        {
            keyPair = KeysProcessing.keyPairGenerator();
            certificate = KeysProcessing.generateCertificate(keyPair);
        }
        catch(Exception e)
        {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Imprime el menu con la lista de
     * algoritmos de cifrado simetrico
     */
    void menuSimetrico() {
        System.out.println("1. AES ");
        System.out.println("2. Blowfish");
    }


    /**
     * Imprime el menu con la lista de
     * algoritmos de cifrado con hash
     */
    void menuHmac() {
        System.out.println("1. HmacSHA1");
        System.out.println("2. HmacSHA256");
        System.out.println("3. HmacSHA384");
        System.out.println("4. HmacSHA512");
    }

    /**
     * Imprime el menu para escoger algoritmos
     * @param stdIn the reader
     */
    private void chooseAlgos(BufferedReader stdIn)
    {
        System.out.println("Escoja uno de estos algoritmos simetricos: ");
        try
        {
            int selection = 1;
            switch (selection)
            {
                case 1:
                    algos.add(AES);
                    break;

                case 2:
                    algos.add(BLOWFISH);
                    break;
            }
            selection = 1;

            switch (selection)
            {
                case 1:
                    algos.add(HMACSHA1);
                    break;

                case 2:
                    algos.add(HMACSHA256);
                    break;

                case 3:
                    algos.add(HMACSHA384);
                    break;

                case 4:
                    algos.add(HMACSHA512);
                    break;
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Metodo que encripta simetricamente
     * @param msg El mensaje que se va encriptar
     * @return el mensaje encriptado en bytes
     */
    private byte[] encryptS(byte[] msg)
    {
        byte[] bytes = null;
        try
        {
            Cipher cipher = Cipher.getInstance(algos.get(0));
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            bytes = cipher.doFinal(msg);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return bytes;
    }

    /**
     * Metodo que desencripta simetricamente
     * @param msg el mensaje que se va a descrifar
     * @return el mensaje descrifrado en bytes
     */
    private byte[] decryptS(byte[] msg)
    {
        byte[] bytes = null;
        try
        {
            Cipher cipher = Cipher.getInstance(algos.get(0));
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            bytes = cipher.doFinal(msg);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return bytes;
    }

    /**
     * Metodo que descrifra asimentricamente
     * @param msg El mensaje que se va a descifrar
     * @return el mensaje descencriptado en bytes
     */
    private byte[] decryptA(byte[] msg)
    {
        byte[] bytes = null;
        try
        {
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            bytes = cipher.doFinal(msg);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return bytes;
    }

    /**
     * Metodo que encripta asimentricamente
     * @param msg El mensaje que se va a cifrar
     * @param ks la llave publica
     * @return El mensaje cifrado en bytes
     */
    private byte[] encryptA(byte[] msg, PublicKey ks)
    {
        byte[] bytes = null;
        try
        {
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.ENCRYPT_MODE, ks);
            bytes = cipher.doFinal(msg);
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        return bytes;
    }

    /**
     * Metodo que transforma una cadena de caracteres
     * en un arreglo de bytes validando que la longitud sea
     * multiplo de 4
     * @param msg la cadena que se va a transformar
     * @return la cadena transformada
     */
    private byte[] toBytesArray(String msg)
    {
        String final_ = msg;
        while(final_.length() % 4 != 0) final_ += 0;
        byte[] bytes = DatatypeConverter.parseBase64Binary(final_);
        return bytes;
    }


    /**
     * Metodo que transforma una cadena de caracteres
     * en un arreglo de bytes validando que la longitud sea
     * multiplo de 4
     * @param msg la cadena que se va a transformar
     * @return la cadena transformada
     */
    private byte[] toByteArray(String msg)
    {
        return DatatypeConverter.parseBase64Binary(msg);
    }

    /**
     * Metodo que transforma un arreglo de bytes
     * en un String
     * @param bytes la cadena que se va a transformar
     * @return la cadena transformada
     */
    private String toHexString(byte[] bytes)
    {
        return DatatypeConverter.printBase64Binary(bytes);
    }


    /**
     * Metodo que procesa todo el procedimiento
     * de comunicacion con el servidor
     * @param stdIn la entrada estandar del usuario
     * @param pIn la entrada de los mensajes del servidor
     * @param pOut la salida de los mensajes del servidor
     * @throws Exception Se lanza este error cuando hay
     * un error de entrada/salida.
     */
    public void procesar(BufferedReader stdIn, BufferedReader pIn, PrintWriter pOut) throws Exception
    {
        try
        {
            // Se escogen los algoritmos
            chooseAlgos(stdIn);

            String fromUser = HOLA;
            pOut.println(fromUser);
            System.out.println("Se envio un: " + HOLA);

            System.out.println("Servidor dice: " + pIn.readLine());

            // Mensaje con los algoritmos
            fromUser = ALGORITMOS + ":" + algos.get(0) + ":" + RSA + ":" + algos.get(1);

            pOut.println(fromUser);
            System.out.println("En envian los algoritmos.");

            // Se debe recibir OK
            System.out.println("Servidor dice: " + pIn.readLine());

            // Se envia el certificado una vez ya se creo en el constructor
            byte[] bytes = certificate.getEncoded();
            fromUser = toHexString(bytes);

            pOut.println(fromUser);
            System.out.println("Se envio el certificado.");

            // Se debe recibir OK
            System.out.println("Servidor dice: " + pIn.readLine());

            // Se recibe el certificado del servidor.
            String fromServer = pIn.readLine();
            System.out.println("Se recibio el certificado.");
            PublicKey ks = null;
            try
            {
                // Convierte el certificado a una entrada por arreglo de bytes
                InputStream is = new ByteArrayInputStream(toByteArray(fromServer));

                // Crea una fabrica de certificados
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate serverCert = (X509Certificate) cf.generateCertificate(is);

                // Obtiene la llave publica del servidor
                ks = serverCert.getPublicKey();

                System.out.println("Se obtuvo la llave publica del servidor");
                pOut.println(OK);
            }
            catch(Exception e)
            {
                pOut.println(ERROR);
                System.out.println("Error: " + e.getMessage());
            }

            // Se recibe llave secreta
            fromServer = pIn.readLine();
            System.out.println("Se recibe llave secreta");
            bytes = toByteArray(fromServer);
            bytes = decryptA(bytes);

            // Se genera la llave secreta.
            secretKey = new SecretKeySpec(bytes, 0, bytes.length, algos.get(0));

            // Se recibe el reto encriptado.
            fromServer = pIn.readLine();
            bytes = toByteArray(fromServer);
            bytes = decryptS(bytes);
            System.out.println("Se recibio reto: " + toHexString(bytes));

            // Se envia reto cifrado de nuevo
            bytes = encryptA(bytes, ks);
            pOut.println(toHexString(bytes));
            System.out.println("Se envia reto cifrado");

            // Se debe recibir OK
            System.out.println("Servidor dice: " + pIn.readLine());

            // Se ingresan los datos
            fromUser = "juan";
            byte[] msg = toBytesArray(fromUser);
            bytes = encryptS(msg);
            // Se envian encriptados al servidor
            pOut.println(toHexString(bytes));

            // Se recibe la hora de actualizacion
            fromServer = pIn.readLine();
            bytes = toBytesArray(fromServer);
            bytes = decryptS(bytes);
            toHexString(bytes);

            // Se notifica que se recibio
            pOut.println(OK);
            System.out.println("Se recibió la fecha y se termino el protocolo");
        }
        catch(Exception e)
        {
            pOut.println(ERROR);
            System.out.println("Hubo un error: " + e.getMessage());
        }
    }
}
