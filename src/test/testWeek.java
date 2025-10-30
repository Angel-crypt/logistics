
package test;

import utils.SimulatedClock;
import utils.SimulatedWeek;

public class testWeek {
    public static void run(){
        long scale = 500; // 1 hora simulada = 500 milisegundos reales
        SimulatedClock clock = new SimulatedClock(scale);
        SimulatedWeek week = new SimulatedWeek(clock);

        Thread clockThread = new Thread(clock);
        Thread weekThread = new Thread(week);

        clockThread.start();
        weekThread.start();

        // Dejar correr la simulación por 7 días simulados (24 horas * 7 = 168 horas)
        try {
            Thread.sleep(scale * 168);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        week.stop();
        clock.stop();
    }
}
