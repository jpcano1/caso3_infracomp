package Generador;

import uniandes.gload.core.LoadGenerator;
import uniandes.gload.core.Task;

public class GeneradorCarga {

	private LoadGenerator generador;
	
	public GeneradorCarga()
	{
		//Task task = createTask();
		int numtareas = 400;
		int tiempoEntreTareas = 20;
		Task clientTask= new ClienteTask();
		
		generador = new LoadGenerator("", numtareas, clientTask ,tiempoEntreTareas );
		generador.generate();
	} 
	
	
	public static void main(String[] args) {
		GeneradorCarga g = new GeneradorCarga();
	}
}
