package generador;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import seguro.cliente.Cliente;
import uniandes.gload.core.Task;

import java.io.*;
import java.net.Socket;
import java.security.Security;

public class ClienteTask extends Task {

    @Override
    public void fail() {
        System.err.println(Task.MENSAJE_FAIL);
    }

    @Override
    public void success() {
        System.out.println(Task.OK_MESSAGE);

    }

    @Override
    public void execute() {
        Socket socket = null;

        PrintWriter escritor = null;

        BufferedReader lector = null;

        try
        {
            socket = new Socket(Cliente.SERVIDOR, 5555);

            escritor = new PrintWriter(socket.getOutputStream(), true);

            lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }

        Security.addProvider(new BouncyCastleProvider());
        Cliente cliente = new Cliente();
        try {
            BufferedReader stdIn = new BufferedReader(new FileReader(new File("./prueba.txt")));

            cliente.procesar(stdIn, lector, escritor);


            stdIn.close();
            escritor.close();
            lector.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
