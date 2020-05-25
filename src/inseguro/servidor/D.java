package inseguro.servidor;

import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.Socket;
import java.security.KeyPair;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Random;
import javax.management.*;

public class D extends Thread {

	public static final String OK = "OK";
	public static final String ALGORITMOS = "ALGORITMOS";
	public static final String CERTSRV = "CERTSRV";
	public static final String CERCLNT = "CERCLNT";
	public static final String SEPARADOR = ":";
	public static final String HOLA = "HOLA";
	public static final String INICIO = "INICIO";
	public static final String ERROR = "ERROR";
	public static final String REC = "recibio-";
	public static final String ENVIO = "envio-";

	// Atributos
	private Socket sc = null;
	private String dlg;
	private byte[] mybyte;
	private static X509Certificate certSer;
	private static KeyPair keyPairServidor;
	private static File file;
	public static final int numCadenas = 13;

	
	public static void init(X509Certificate pCertSer, KeyPair pKeyPairServidor, File pFile) {
		certSer = pCertSer;
		keyPairServidor = pKeyPairServidor;
		file = pFile;
	}
	
	public D (Socket csP, int idP) {
		sc = csP;
		dlg = new String("delegado " + idP + ": ");
		try {
		mybyte = new byte[520]; 
		mybyte = certSer.getEncoded();
		} catch (Exception e) {
			System.out.println("Error creando el thread" + dlg);
			e.printStackTrace();
		}
	}
	
	private boolean validoAlgHMAC(String nombre) {
		return ((nombre.equals(S.HMACMD5) ||
			 nombre.equals(S.HMACSHA1) ||
			 nombre.equals(S.HMACSHA256) ||
			 nombre.equals(S.HMACSHA384) ||
			 nombre.equals(S.HMACSHA512)
			 ));
	}

	/*
	 * Generacion del archivo log.
	 * Nota:
	 * - Debe conservar el metodo .
	 * - Es el Ãºnico metodo permitido para escribir en el log.
	 */
	private void escribirMensaje(String pCadena) {

		try {
			FileWriter fw = new FileWriter(file,true);
			fw.write(pCadena + "\n");
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		String[] cadenas;
		cadenas = new String[numCadenas];

		String feedback;
		String linea;
	    System.out.println(dlg + "Empezando atencion.");
	        try {

				PrintWriter ac = new PrintWriter(sc.getOutputStream() , true);
				BufferedReader dc = new BufferedReader(new InputStreamReader(sc.getInputStream()));

				/***** Fase 1:  *****/
				linea = dc.readLine();
				if (!linea.equals(HOLA)) {
					ac.println(ERROR);
				    sc.close();
					throw new Exception(dlg + ERROR + REC + linea +"-terminando.");
				} else {
					ac.println(OK);
					cadenas[0] = dlg + REC + linea + "-continuando.";
					System.out.println(cadenas[0]);
				}

				/***** Fase 2:  *****/
				linea = dc.readLine();
				if (!(linea.contains(SEPARADOR) && linea.split(SEPARADOR)[0].equals(ALGORITMOS))) {
					ac.println(ERROR);
					sc.close();
					throw new Exception(dlg + ERROR + REC + linea +"-terminando.");
				}

				String[] algoritmos = linea.split(SEPARADOR);
				if (!algoritmos[1].equals(S.DES) && !algoritmos[1].equals(S.AES) &&
					!algoritmos[1].equals(S.BLOWFISH) && !algoritmos[1].equals(S.RC4)){
					ac.println(ERROR);
					sc.close();
					throw new Exception(dlg + ERROR + "Alg.Simetrico" + REC + algoritmos + "-terminando.");
				}
				if (!algoritmos[2].equals(S.RSA) ) {
					ac.println(ERROR);
					sc.close();
					throw new Exception(dlg + ERROR + "Alg.Asimetrico." + REC + algoritmos + "-terminando.");
				}
				if (!validoAlgHMAC(algoritmos[3])) {
					ac.println(ERROR);
					sc.close();
					throw new Exception(dlg + ERROR + "AlgHash." + REC + algoritmos + "-terminando.");
				}
				cadenas[1] = dlg + REC + linea + "-continuando.";
				System.out.println(cadenas[1]);
				ac.println(OK);
				cadenas[2] = dlg + ENVIO + OK + "-continuando.";
				System.out.println(cadenas[2]);


				/***** Fase 3: Recibe certificado del cliente *****/
				String strCertificadoCliente = dc.readLine();
				byte[] certificadoClienteBytes = new byte[520];
				certificadoClienteBytes = toByteArray(strCertificadoCliente);
				CertificateFactory creador = CertificateFactory.getInstance("X.509");
				InputStream in = new ByteArrayInputStream(certificadoClienteBytes);
				X509Certificate certificadoCliente = (X509Certificate)creador.generateCertificate(in);
				cadenas[3] = dlg + REC + "certificado del cliente. continuando.";
				System.out.println(cadenas[3]);
				ac.println(OK);
				cadenas[4] = dlg + ENVIO + OK + "-continuando.";
				System.out.println(cadenas[4]);

				/***** Fase 4: Envia certificado del servidor *****/
				String strSerCert = toHexString(mybyte);
				ac.println(strSerCert);
				cadenas[5] = dlg + ENVIO + " certificado del servidor. continuando.";
				System.out.println(cadenas[5]);
				linea = dc.readLine();
				if (!linea.equals(OK)) {
				    sc.close();
					throw new Exception(dlg + ERROR + REC + linea +"-terminando.");
				} else {
					cadenas[6] = dlg + REC + linea + "-continuando.";
					System.out.println(cadenas[6]);
				}

				long start = System.nanoTime();
				/***** Fase 5: Envia llave simetrica *****/
				SecretKey simetrica = S.kgg(algoritmos[1]);
				byte [ ] ciphertext1 = S.ae(simetrica.getEncoded(),
						                 certificadoCliente.getPublicKey(), algoritmos[2]);
				ac.println(toHexString(ciphertext1));
				cadenas[7] = dlg +  ENVIO + "llave K_SC al cliente. continuado.";
				System.out.println(cadenas[7]);

				/***** Fase 5: Envia reto *****/
				Random rand = new Random();
				int intReto = rand.nextInt(999);
				String strReto = intReto+"";
				while (strReto.length()%4!=0) strReto += "0";

				String reto = strReto;
				ac.println(reto);
				cadenas[8] = dlg + ENVIO + reto + "-reto al cliente. continuando ";
				System.out.println(cadenas[8]);

				/***** Fase 6: Recibe reto del cliente *****/
				linea = dc.readLine();
				if (linea.equals(reto)) {
					cadenas[9] = dlg + REC + linea + "-reto correcto. continuado.";
					System.out.println(cadenas[9]);
					ac.println("OK");
				} else {
					ac.println("ERROR");
				    sc.close();
					throw new Exception(dlg + REC + linea + "-ERROR en reto. terminando");
				}

				/***** Fase 7: Recibe identificador de usuario *****/
				linea = dc.readLine();
				cadenas[10] = dlg + REC + linea + "-continuando";
				System.out.println(cadenas[10]);
				
				/***** Fase 8: Envia hora de registro *****/
				Calendar rightNow = Calendar.getInstance();
				int hora = rightNow.get(Calendar.HOUR_OF_DAY);
				int minuto = rightNow.get(Calendar.MINUTE);
				String strvalor;
				if (hora<10)
					strvalor = "0" + ((hora) * 100 + minuto);
				else
					strvalor = ((hora) * 100 + minuto) + "";
				while (strvalor.length()%4!=0) strvalor = "0" + strvalor;

				ac.println(strvalor);
				cadenas[11] = dlg + ENVIO + strvalor + " continuado.";
				System.out.println(cadenas[11]);

				double elapsedTimeInSec = (System.nanoTime() - start);

				System.err.println("Elapsed Time: "+ elapsedTimeInSec);
				System.err.println("Memory "+ getSystemCpuLoad());

				//printFile(dlg, getSystemCpuLoad(), elapsedTimeInSec);

				linea = dc.readLine();	
				if (linea.equals(OK)) {
					cadenas[12] = dlg + REC + linea + "-Terminando exitosamente.";
					System.out.println(cadenas[12]);
				} else {
					cadenas[12] = dlg + REC + linea + "-Terminando con error";
			        System.out.println(cadenas[12]);
				}
		        sc.close();
		        
			    for (int i=0;i<numCadenas;i++) {
				    escribirMensaje(cadenas[i]);
			    }
			   
	        } catch (Exception e) {
	          e.printStackTrace();
	        }
	}

	public double getSystemCpuLoad() throws Exception {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
		System.out.println(name);
		AttributeList list = mbs.getAttributes(name, new String[]{ "SystemCpuLoad" });
		if (list.isEmpty()) return Double.NaN;
		Attribute att = (Attribute)list.get(0);
		Double value = (Double)att.getValue();
		// usually takes a couple of seconds before we get real values
		if (value == -1.0) return Double.NaN;
		// returns a percentage value with 1 decimal point precision
		return ((int)(value * 1000) / 10.0);
	}

	public void printFile(String pDelegado, double pMem, double pTemp)
	{
		File fl = new File("./data/Libro10.csv");
		try
		{
			FileWriter fp = new FileWriter(fl, true);
			fp.write(pDelegado + ";" + pMem + ";" + pTemp + "\n");
			fp.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static String toHexString(byte[] array) {
	    return DatatypeConverter.printBase64Binary(array);
	}

	public static byte[] toByteArray(String s) {
	    return DatatypeConverter.parseBase64Binary(s);
	}
	
}
