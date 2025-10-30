package test;

import domain.warehouse.Warehouse;
import utils.SimulatedClock;
import utils.SimulatedWeek;
import utils.TimeUtils;

public class testWarehouse {
    public static void run(){
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║     SISTEMA DE GESTIÓN DE ALMACÉN - DEMO EJECUTABLE       ║");
        System.out.println("║          Llenado automático 20:00 - 09:00                 ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        // Crear el reloj simulado (1 hora simulada = 1 segundo real)
        SimulatedClock clock = new SimulatedClock(1000);
        Thread clockThread = new Thread(clock);
        clockThread.setDaemon(true);
        clockThread.start();

        // Crear la semana simulada
        SimulatedWeek week = new SimulatedWeek(clock);
        Thread weekThread = new Thread(week);
        weekThread.setDaemon(true);
        weekThread.start();

        // Crear el almacén con capacidad de 5000 kg
        Warehouse warehouse = new Warehouse(5000.0);

        try {
            // Esperar un poco para que el reloj inicie
            Thread.sleep(500);

            System.out.println("\n═══════════════════════════════════════════════════════════");
            System.out.println("FASE 1: Llenado inicial del almacén (manual)");
            System.out.println("═══════════════════════════════════════════════════════════");

            // Llenar el almacén manualmente con 2000 kg iniciales
            warehouse.fillRandomly(2000.0);

            // Esperar 2 horas simuladas
            System.out.println("\n⏰ Esperando 2 horas simuladas...");
            TimeUtils.sleepSimulated(2.0);

            System.out.println("\n═══════════════════════════════════════════════════════════");
            System.out.println("FASE 2: Iniciar sistema de llenado automático");
            System.out.println("═══════════════════════════════════════════════════════════");

            // Activar el rellenado automático (horario 20:00 - 09:00)
            warehouse.startAutomatedRefill(clock, week);

            System.out.println("\n⏰ El sistema operará automáticamente con el horario nocturno");
            System.out.println("   Esperando para observar el ciclo de llenado...");
            System.out.println("   Hora actual simulada: " +
                    String.format("%.1f", clock.getCurrentSimulatedTime() % 24) + ":00");

            // Esperar 15 horas simuladas para ver el comportamiento
            System.out.println("\n⏰ Observando operación durante 15 horas simuladas...");
            TimeUtils.sleepSimulated(15.0);

            System.out.println("\n═══════════════════════════════════════════════════════════");
            System.out.println("FASE 3: Estado después del primer período");
            System.out.println("═══════════════════════════════════════════════════════════");

            System.out.println("📊 Estado actual:");
            System.out.println("   • Día: " + week.getCurrentDay());
            System.out.println("   • Hora: " + String.format("%.1f", clock.getCurrentSimulatedTime() % 24) + ":00");
            System.out.println("   • Productos: " + warehouse.getTotalProducts());
            System.out.println("   • Carga: " + String.format("%.2f", warehouse.getCurrentLoad()) + " kg");
            System.out.println("   • Ocupación: " + String.format("%.1f", warehouse.getOccupancyPercentage()) + "%");

            warehouse.printInventorySummary();

            System.out.println("\n⏰ Esperando 20 horas más para observar ciclo completo...");
            TimeUtils.sleepSimulated(20.0);

            System.out.println("\n═══════════════════════════════════════════════════════════");
            System.out.println("FASE 4: Estado después del ciclo completo");
            System.out.println("═══════════════════════════════════════════════════════════");

            System.out.println("📊 Estado actual:");
            System.out.println("   • Día: " + week.getCurrentDay());
            System.out.println("   • Hora: " + String.format("%.1f", clock.getCurrentSimulatedTime() % 24) + ":00");
            System.out.println("   • Productos: " + warehouse.getTotalProducts());
            System.out.println("   • Carga: " + String.format("%.2f", warehouse.getCurrentLoad()) + " kg");
            System.out.println("   • Ocupación: " + String.format("%.1f", warehouse.getOccupancyPercentage()) + "%");

            warehouse.printInventorySummary();

            System.out.println("\n⏰ Esperando 30 horas más para observar múltiples ciclos...");
            TimeUtils.sleepSimulated(30.0);

            // Resumen final
            System.out.println("\n╔════════════════════════════════════════════════════════════╗");
            System.out.println("║                    RESUMEN FINAL DEL SISTEMA               ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝");
            System.out.println("\n📊 Estado final del almacén:");
            System.out.println("   • Día actual: " + week.getCurrentDay());
            System.out.println("   • Hora simulada: " + String.format("%.1f", clock.getCurrentSimulatedTime() % 24) + ":00");
            System.out.println("   • Tiempo total transcurrido: " + String.format("%.1f", clock.getCurrentSimulatedTime()) + " horas");
            System.out.println("   • Productos en inventario: " + warehouse.getTotalProducts());
            System.out.println("   • Carga actual: " + String.format("%.2f", warehouse.getCurrentLoad()) + " kg");
            System.out.println("   • Capacidad máxima: " + warehouse.getMaxCapacity() + " kg");
            System.out.println("   • Ocupación: " + String.format("%.1f", warehouse.getOccupancyPercentage()) + "%");

            warehouse.printInventorySummary();

            System.out.println("\n✅ Demostración completada exitosamente");
            System.out.println("   El servicio de llenado automático seguirá activo en segundo plano.");
            System.out.println("   Opera solo entre 20:00 y 09:00 horas simuladas.");
            System.out.println("   Presiona Ctrl+C para finalizar el programa.\n");

            // Mantener el programa activo para ver más ciclos de llenado
            Thread.sleep(120000); // 120 segundos reales = 120 horas simuladas = 5 días

        } catch (InterruptedException e) {
            System.out.println("\n⚠️  Programa interrumpido por el usuario");
            Thread.currentThread().interrupt();
        } finally {
            // Limpiar recursos
            warehouse.stopAutomatedRefill();
            week.stop();
            clock.stop();
            System.out.println("\n🛑 Sistema detenido. ¡Hasta luego!");
        }
    }
}
