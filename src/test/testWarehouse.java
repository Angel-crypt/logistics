package test;

import domain.products.Product;
import domain.warehouse.Warehouse;
import utils.SimulatedClock;
import utils.TimeUtils;

import java.util.List;

public class testWarehouse {
    public static void run(){
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║     SISTEMA DE GESTIÓN DE ALMACÉN - DEMO EJECUTABLE       ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        // Crear el reloj simulado (1 hora simulada = 1 segundo real)
        SimulatedClock clock = new SimulatedClock(1000);
        Thread clockThread = new Thread(clock);
        clockThread.setDaemon(true);
        clockThread.start();

        // Crear el almacén con capacidad de 5000 kg
        Warehouse warehouse = new Warehouse(5000.0);

        try {
            // Esperar un poco para que el reloj inicie
            Thread.sleep(500);

            System.out.println("\n═══════════════════════════════════════════════════════════");
            System.out.println("FASE 1: Llenado inicial del almacén");
            System.out.println("═══════════════════════════════════════════════════════════");

            // Llenar el almacén con 3000 kg iniciales
            warehouse.fillRandomly(3000.0);

            // Esperar 2 horas simuladas
            System.out.println("\n⏰ Esperando 2 horas simuladas...");
            TimeUtils.sleepSimulated(2.0);

            System.out.println("\n═══════════════════════════════════════════════════════════");
            System.out.println("FASE 2: Rellenado adicional");
            System.out.println("═══════════════════════════════════════════════════════════");

            // Rellenar nuevamente el almacén
            warehouse.fillRandomly(1500.0);

            // Esperar 3 horas simuladas
            System.out.println("\n⏰ Esperando 3 horas simuladas...");
            TimeUtils.sleepSimulated(3.0);

            System.out.println("\n═══════════════════════════════════════════════════════════");
            System.out.println("FASE 3: Tercer llenado");
            System.out.println("═══════════════════════════════════════════════════════════");

            // Rellenar una vez más
            warehouse.fillRandomly(500.0);

            System.out.println("\n═══════════════════════════════════════════════════════════");
            System.out.println("FASE 4: Iniciar servicio de rellenado nocturno automático");
            System.out.println("═══════════════════════════════════════════════════════════");

            // Activar el rellenado nocturno automático
            warehouse.startNightlyRefill(clock);

            System.out.println("\n⏰ El sistema continuará operando con rellenado automático...");
            System.out.println("   Esperando 30 horas simuladas para observar el comportamiento...");

            // Esperar para ver al menos un ciclo de rellenado nocturno
            TimeUtils.sleepSimulated(30.0);

            System.out.println("\n═══════════════════════════════════════════════════════════");
            System.out.println("FASE 5: Estado después del primer ciclo automático");
            System.out.println("═══════════════════════════════════════════════════════════");

            warehouse.printInventorySummary();

            System.out.println("\n⏰ Esperando 25 horas más para observar el segundo ciclo...");
            TimeUtils.sleepSimulated(25.0);

            // Resumen final
            System.out.println("\n╔════════════════════════════════════════════════════════════╗");
            System.out.println("║                    RESUMEN FINAL DEL SISTEMA               ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝");
            System.out.println("\n📊 Estado actual del almacén:");
            System.out.println("   • Hora simulada: " + clock.getCurrentSimulatedTime() + " horas");
            System.out.println("   • Productos en inventario: " + warehouse.getTotalProducts());
            System.out.println("   • Carga actual: " + String.format("%.2f", warehouse.getCurrentLoad()) + " kg");
            System.out.println("   • Capacidad máxima: " + warehouse.getMaxCapacity() + " kg");
            System.out.println("   • Ocupación: " + String.format("%.1f", warehouse.getOccupancyPercentage()) + "%");

            warehouse.printInventorySummary();

            System.out.println("\n✅ Demostración completada exitosamente");
            System.out.println("   El servicio de rellenado nocturno seguirá activo en segundo plano.");
            System.out.println("   Presiona Ctrl+C para finalizar el programa.\n");

            // Mantener el programa activo para ver más ciclos de rellenado
            Thread.sleep(60000); // 60 segundos reales = 60 horas simuladas

        } catch (InterruptedException e) {
            System.out.println("\n⚠️  Programa interrumpido por el usuario");
            Thread.currentThread().interrupt();
        } finally {
            // Limpiar recursos
            warehouse.stopNightlyRefill();
            clock.stop();
            System.out.println("\n🛑 Sistema detenido. ¡Hasta luego!");
        }
    }
}
