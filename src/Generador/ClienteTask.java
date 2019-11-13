package Generador;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import seguro.cliente_seguro.Cliente;
import uniandes.gload.core.Task;

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

        System.out.println("Cliente... ");

        System.out.println("Ingrese el puerto al que se quiere conectar: ");


        try
        {
            socket = new Socket(Cliente.SERVIDOR, 1111);

            escritor = new PrintWriter(socket.getOutputStream(), true);

            lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }

        Cliente cliente = new Cliente();
        try {
            BufferedReader stdIn = new BufferedReader(new FileReader(new File("./prueba.txt")));

			cliente.procesar(stdIn, lector, escritor);
	

        stdIn.close();
        escritor.close();
        lector.close();
        socket.close();
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
