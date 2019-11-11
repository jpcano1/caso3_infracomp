package src.cliente_inseguro;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
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
     * Servidor - Puerto
     */
    public static final int PUERTO = 5555;
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

    //-------------------
    // Atributos
    //-------------------

    /**
     * Llave secreta para cifrado simetrico
     */
    private SecretKey symmetricKey;

    /**
     * Arreglo dinamico con los algoritmos
     */
    private ArrayList<String> algos;

    //---------------------
    // Constructores
    //---------------------
    /**
     * Constructor
     */
    public Cliente()
    {
        KeyGenerator keyGen;
        algos = new ArrayList<String>();
        try
        {
            keyGen = KeyGenerator.getInstance(AES);
            symmetricKey = keyGen.generateKey();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    //---------------------
    // Metodos
    //---------------------

    /**
     * Imprime el menu con la lista de
     * algoritmos de cifrado simetrico
     */
    void menuSimetrico() {
        System.out.println("1. AES ");
        System.out.println("2. Blowfish (No disponible)");
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
     * @param stdIn
     */
    private void chooseAlgos(BufferedReader stdIn)
    {
        System.out.println("Escoja uno de estos algoritmos simetricos: ");
        try
        {
            menuSimetrico();
            int selection = Integer.parseInt(stdIn.readLine());
            switch (selection)
            {
                case 1:
                    algos.add(AES);
                    break;

                case 2:
                    System.out.println("No disponible, es usará AES por defecto");
                    algos.add(AES);
                    break;
            }
            System.out.println("Escoja uno de estos algoritmos de hashing: ");
            menuHmac();
            selection = Integer.parseInt(stdIn.readLine());

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
     * Metodo que encripta asimentricamente
     * @param msg El mensaje que se va a cifrar
     * @param publicKey la llave publica
     * @return El mensaje cifrado en bytes
     */
    private byte[] encryptA(byte[] msg, PublicKey publicKey)
    {
        byte[] bytes = null;
        try
        {
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            bytes = cipher.doFinal(msg);
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        return bytes;
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
            cipher.init(Cipher.ENCRYPT_MODE, symmetricKey);
            bytes = cipher.doFinal(msg);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return bytes;
    }

    /**
     * Metodo que procesa todo el procedimiento
     * de comunicacion con el servidor
     * @param stdIn la entrada estandar del usuario
     * @param pIn la entrada de los mensajes del servidor
     * @param pOut la salida de los mensajes del servidor
     * @throws IOException Se lanza este error cuando hay
     * un error de entrada/salida.
     */
    public void procesar(BufferedReader stdIn, BufferedReader pIn, PrintWriter pOut) throws IOException
    {
        // Se escogen los algoritmos
        chooseAlgos(stdIn);
        // se envia el primer mensaje
        String fromUser = HOLA;
        pOut.println(fromUser);
        System.out.println("Se envio un " + HOLA);

        // Se recibe el primer mensaje del servidor
        System.out.println("Servidor dice: " + pIn.readLine());

        // Se envian los algoritmos
        fromUser = ALGORITMOS + ":" + algos.get(0) + ":" + RSA + ":" + algos.get(1);

        pOut.println(fromUser);
        System.out.println("En envian los algoritmos.");

        // Se debe recibir OK
        System.out.println("Servidor dice: " + pIn.readLine());

        // Recibe el certificado
        String cert = pIn.readLine();
        System.out.println("Se recibio certificado");

        try
        {
            // Convierte el certificado a una entrada por arreglo de bytes
            InputStream is = new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(cert));
            // Crea una fabrica de certificados
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) cf.generateCertificate(is);

            // Obtiene la llave publica del servidor
            PublicKey publicKey = certificate.getPublicKey();

            // Se envia la llave simetrica
            byte[] ks = symmetricKey.getEncoded();
            fromUser = DatatypeConverter.printBase64Binary(ks);
            pOut.println(fromUser);

            // Envía reto
            System.out.println("Ingrese un mensaje sin espacios:");

            fromUser = stdIn.readLine();
            String reto = fromUser;
            byte[] userMsg = toBytesArray(fromUser);
            pOut.println(DatatypeConverter.printBase64Binary(userMsg));
            System.out.println("Reto enviado");

            String fromServer = pIn.readLine();
            System.out.println("Reto recibido");
            if(fromServer.equals(reto))
            {
                pOut.println(OK);
                System.out.println("Reto correcto");
            }
            else
            {
                pOut.println(ERROR);
                System.out.println("Reto incorrecto");
                return;
            }

            // Se ingresan los datos de cedula y clave
            System.out.println("Ingrese su cedula: ");
            // Cedula
            fromUser = stdIn.readLine();
            pOut.println(fromUser);
            System.out.println("Cedula enviada");

            // Clave
            System.out.println("Ingrese su clave: ");
            fromUser = stdIn.readLine();
            pOut.println(fromUser);
            System.out.println("Clave enviada");

            // Se recibe el valor
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}
