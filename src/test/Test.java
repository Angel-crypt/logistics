package test;

import domain.categories.Category;
import utils.SimulatedClock;

public class Test {
    private static void testCategory(){
        Category electronic = Category.ELECTRONICS;

        String randomProductName = electronic.getRandomName();
        double randomProductWeight = electronic.getRandomWeight();

        System.out.println("Producto aleatorio: "+randomProductName+" tiene un peso de "+randomProductWeight);
    }

    private static void testTime(){
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

    public static void main(String[] args){
        testTime();
    }
}
