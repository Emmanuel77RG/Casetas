package Hilos;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Random;

/**
 * Clase que se encarga de generar coches a intervalos regulares.
 */
public class GeneradorCoches {
    private ExecutorService executor;
    private Carril carrilIda;
    private Carril carrilVenida;
    private Random random;
    private volatile boolean running; // Volatile para garantizar la visibilidad entre hilos

    // Constantes
    private static final int TIEMPO_GENERACION_SEGUNDOS = 2;
    private static final int TIEMPO_ESPERA_TERMINACION_SEGUNDOS = 5;
    private static final int TAMANO_POOL_HILOS = 10;

    /**
     * Constructor que inicializa el generador de coches.
     * @param carrilIda Carril para la dirección de ida.
     * @param carrilVenida Carril para la dirección de venida.
     */
    public GeneradorCoches(Carril carrilIda, Carril carrilVenida) {
        this.carrilIda = carrilIda;
        this.carrilVenida = carrilVenida;
        this.executor = Executors.newFixedThreadPool(TAMANO_POOL_HILOS); // Limitar el número de hilos
        this.random = new Random();
        this.running = true;
    }

    /**
     * Método para iniciar la generación de coches.
     */
    public void iniciarGeneracion() {
        Runnable generar = () -> {
            while (running) {
                boolean direccion = random.nextBoolean(); // true para ida, false para venida
                Carril carril = direccion ? carrilIda : carrilVenida;
                Coche coche = new Coche(carril, direccion);
                executor.submit(coche);
                try {
                    TimeUnit.SECONDS.sleep(TIEMPO_GENERACION_SEGUNDOS); // Crear un coche cada 2 segundos
                } catch (InterruptedException e) {
                    System.err.println("Generador de coches interrumpido.");
                    Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
                    break;
                }
            }
        };
        executor.submit(generar);
    }

    /**
     * Método para detener la generación de coches.
     */
    public void detenerGeneracion() {
        running = false;
        try {
            executor.shutdown();
            if (!executor.awaitTermination(TIEMPO_ESPERA_TERMINACION_SEGUNDOS, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(TIEMPO_ESPERA_TERMINACION_SEGUNDOS, TimeUnit.SECONDS)) {
                    System.err.println("ExecutorService no se pudo cerrar.");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
        }
    }
}