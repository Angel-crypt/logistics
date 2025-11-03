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
            }
        } catch (InterruptedException e) {
            // Reloj detenido
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
     * Devuelve el día actual basado en las horas simuladas (cada 24 horas = 1 día).
     * @return número de día (empieza en 1)
     */
    public int getCurrentDay() {
        return (int)(currentSimulatedTime / 24) + 1;
    }

    /**
     * Devuelve la hora del día actual (0-23).
     * @return hora del día actual
     */
    public int getCurrentHourOfDay() {
        return (int)(currentSimulatedTime % 24);
    }

    /**
     * Obtiene una representación en texto del tiempo actual: "Día X, Hora Y".
     * @return string con día y hora actual
     */
    public String getCurrentTimeString() {
        return String.format("Día %d, Hora %d", getCurrentDay(), getCurrentHourOfDay());
    }

    /**
     * Detiene el reloj simulado.
     */
    public void stop() {
        running = false;
    }
}
