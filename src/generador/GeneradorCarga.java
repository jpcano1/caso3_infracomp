package generador;

import uniandes.gload.core.LoadGenerator;
import uniandes.gload.core.Task;

public class GeneradorCarga {

    private LoadGenerator generador;

    public GeneradorCarga()
    {
        //Task task = createTask();
        int numtareas = 80;
        int tiempoEntreTareas = 100;
        Task clientTask= new ClienteTask();

        generador = new LoadGenerator("", numtareas, clientTask ,tiempoEntreTareas );
        generador.generate();
    }

    public static void main(String[] args) {
        GeneradorCarga g = new GeneradorCarga();
    }
}
