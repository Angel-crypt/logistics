package main;

/**
 * Punto de entrada principal del sistema logístico de Guadalajara.
 * Ejecuta la simulación completa por 7 días.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║     SISTEMA LOGÍSTICO GUADALAJARA - SIMULACIÓN            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");
        
        LogisticsSimulator simulator = new LogisticsSimulator();
        simulator.initialize();
        
        // Ejecutar simulación por 7 días (1 semana)
        simulator.runSimulation(7);
        
        simulator.stop();
        
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║              SIMULACIÓN FINALIZADA                         ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
    }
}
