package test;

import main.LogisticsSimulator;

public class testLogisticsSimulator {
    public static void run() {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║      PRUEBA DEL SIMULADOR LOGÍSTICO GUADALAJARA          ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");
        
        LogisticsSimulator simulator = new LogisticsSimulator();
        simulator.initialize();
        
        // Ejecutar simulación por 1 día para prueba rápida
        System.out.println("\n[TEST] Ejecutando prueba rápida de 1 día...\n");
        simulator.runSimulation(1);
        
        simulator.stop();
        
        System.out.println("\n[TEST] Prueba completada. Revisa la carpeta 'reports/' para ver los archivos generados.");
    }
}

