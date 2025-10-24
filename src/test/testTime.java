package test;

import utils.SimulatedClock;

public class testTime {
    public static void run(){
        long scale = 5000; // 1 hora simulada = 5 segundos reales
        SimulatedClock clock = new SimulatedClock(scale);

        Thread clockThread = new Thread(clock);
        clockThread.start();

        // Dejar correr la simulación por 5 horas simuladas
        try {
            Thread.sleep(scale * 5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        clock.stop();
    }
}
