package utils;

/**
 * SimulatedWeek gestiona el paso de los días de la semana dentro del sistema simulado.
 * Utiliza SimulatedClock para determinar el avance de días (cada 24 horas = 1 día).
 * El ciclo de la semana se repite automáticamente de Lunes a Domingo.
 */
public class SimulatedWeek implements Runnable {

    /** Array con los nombres de los días de la semana */
    private final String[] daysOfWeek = {
            "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"
    };

    /** Índice del día actual (0 = Lunes, 6 = Domingo) */
    private int currentDayIndex = 0;

    /** Referencia al reloj simulado para sincronización */
    private final SimulatedClock simulatedClock;

    /** Última hora verificada para calcular cambios de día */
    private double lastHourCheck = 0.0;

    /** Indica si el ciclo de la semana está activo */
    private volatile boolean running = true;

    public SimulatedWeek(SimulatedClock simulatedClock) {
        this.simulatedClock = simulatedClock;
    }

    /**
     * Inicia el ciclo de la semana en un hilo separado.
     */
    @Override
    public void run() {
        try {
            while (running) {
                Thread.sleep(100); // Verificar cada 100ms
                double currentHour = simulatedClock.getCurrentSimulatedTime();

                // Cada 24 horas simuladas = 1 día
                if (currentHour - lastHourCheck >= 24) {
                    currentDayIndex++;
                    lastHourCheck = currentHour;

                    // Si llegamos al final de la semana, reiniciar
                    if (currentDayIndex >= daysOfWeek.length) {
                        currentDayIndex = 0;
                    }
                }
            }
        } catch (InterruptedException e) {
            // Ciclo detenido
        }
    }

    /**
     * Devuelve el nombre del día actual simulado.
     * @return nombre del día actual
     */
    public String getCurrentDay() {
        return daysOfWeek[currentDayIndex];
    }

    /**
     * Devuelve el índice del día actual (0-6).
     * @return índice del día actual
     */
    public int getCurrentDayIndex() {
        return currentDayIndex;
    }

    /**
     * Detiene el ciclo de la semana.
     */
    public void stop() {
        running = false;
    }
}
