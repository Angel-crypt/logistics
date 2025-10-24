package utils;

/**
 * Utilidad para simular el paso del tiempo en un sistema logístico.
 * La escala determina cuántos milisegundos reales equivalen a una hora simulada.
 * Permite convertir tiempos reales a simulados y pausar hilos acorde al tiempo virtual.
 */
public class TimeUtils {

    /**
     * Escala base: 5000 ms reales equivalen a 1 hora simulada.
     * Puedes cambiar este valor para acelerar o desacelerar la simulación.
     */
    public static final long MILLIS_PER_SIMULATED_HOUR = 5000;

    /**
     * Convierte horas simuladas a milisegundos reales según la escala del sistema.
     * @param simHours horas simuladas a convertir
     * @return milisegundos reales equivalentes
     */
    public static long toRealMillis(double simHours) {
        return (long) (simHours * MILLIS_PER_SIMULATED_HOUR);
    }

    /**
     * Detiene el hilo actual por un periodo equivalente al tiempo simulado.
     * @param simHours cantidad de horas simuladas que se desean esperar
     */
    public static void sleepSimulated(double simHours) throws InterruptedException {
        Thread.sleep(toRealMillis(simHours));
    }

    /**
     * Genera un valor aleatorio dentro de un rango, útil para tiempos, pesos u otros parámetros simulados.
     * @param min valor mínimo posible
     * @param max valor máximo posible
     * @return número aleatorio entre min y max
     */
    public static double randomInRange(double min, double max) {
        return min + (max - min) * Math.random();
    }
}
