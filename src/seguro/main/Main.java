package seguro.main;

import seguro.cliente_seguro.Cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        Scanner sc = new Scanner(System.in);
        Socket socket = null;

        PrintWriter escritor = null;

        BufferedReader lector = null;

        System.out.println("Cliente... ");

        System.out.println("Ingrese el puerto al que se quiere conectar: ");

        int puerto = Integer.parseInt(sc.nextLine());

        try
        {
            socket = new Socket(Cliente.SERVIDOR, puerto);

            escritor = new PrintWriter(socket.getOutputStream(), true);

            lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        Cliente cliente = new Cliente();
        cliente.procesar(stdIn, lector, escritor);

        sc.close();
        stdIn.close();
        escritor.close();
        lector.close();
        socket.close();
    }
}
