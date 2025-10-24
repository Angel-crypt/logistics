package utils;

/**
 * SimulatedClock gestiona el tiempo dentro del sistema simulado.
 * Avanza automáticamente en función de un factor de escala entre tiempo real y simulado.
 * Permite registrar listeners (observadores) que reaccionan cuando avanza el tiempo.
 */
public class SimulatedClock implements Runnable {

    /** Tiempo actual simulado en horas */
    private double currentSimulatedTime = 0.0;

    /** Factor: milisegundos reales que equivalen a una hora simulada */
    private final long millisPerSimulatedHour;

    /** Indica si el reloj está activo */
    private volatile boolean running = true;

    public SimulatedClock(long millisPerSimulatedHour) {
        this.millisPerSimulatedHour = millisPerSimulatedHour;
    }

    /**
     * Inicia el reloj simulado en un hilo separado.
     */
    @Override
    public void run() {
        try {
            while (running) {
                Thread.sleep(millisPerSimulatedHour); // Espera 1 hora simulada (en tiempo real)
                currentSimulatedTime++;
                System.out.println("[Clock] Hora simulada actual: " + currentSimulatedTime);
            }
        } catch (InterruptedException e) {
            System.out.println("[Clock] Reloj simulado detenido.");
        }
    }

    /**
     * Devuelve la hora simulada actual.
     * @return hora simulada
     */
    public double getCurrentSimulatedTime() {
        return currentSimulatedTime;
    }

    /**
     * Detiene el reloj simulado.
     */
    public void stop() {
        running = false;
    }
}
