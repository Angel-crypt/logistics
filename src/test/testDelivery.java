package test;

import domain.categories.Category;
import domain.products.Product;
import domain.warehouse.Warehouse;
import domain.vehicles.Truck;
import domain.vehicles.Airplane;
import services.DeliveryService;
import services.InventoryService;
import utils.SimulatedClock;

import java.util.List;

public class testDelivery {
    public static void run() {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║         PRUEBA DE SISTEMA DE ENTREGAS                      ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        // Crear reloj simulado (rápido para la prueba)
        SimulatedClock clock = new SimulatedClock(500); // 1 hora simulada = 0.5 segundos reales
        Thread clockThread = new Thread(clock);
        clockThread.setDaemon(true);
        clockThread.start();

        try {
            Thread.sleep(100); // Esperar a que el reloj inicie

            // Crear almacén
            Warehouse warehouse = new Warehouse(10000.0);
            
            // Llenar almacén inicialmente
            System.out.println("\n═══════════════════════════════════════════════════════════");
            System.out.println("FASE 1: Llenado inicial del almacén");
            System.out.println("═══════════════════════════════════════════════════════════");
            warehouse.fillRandomly(2000.0);

            // Crear servicios
            System.out.println("\n═══════════════════════════════════════════════════════════");
            System.out.println("FASE 2: Inicialización de servicios");
            System.out.println("═══════════════════════════════════════════════════════════");
            
            DeliveryService deliveryService = new DeliveryService(warehouse, clock);
            InventoryService inventoryService = new InventoryService(warehouse);

            // Registrar vehículos
            Truck truck1 = new Truck("Almacén Central");
            Truck truck2 = new Truck(15000.0, "Almacén Central");
            Airplane airplane1 = new Airplane("Aeropuerto Principal");

            deliveryService.registerVehicle(truck1);
            deliveryService.registerVehicle(truck2);
            deliveryService.registerVehicle(airplane1);

            System.out.println("\n═══════════════════════════════════════════════════════════");
            System.out.println("FASE 3: Creación de tareas de entrega");
            System.out.println("═══════════════════════════════════════════════════════════");

            // Obtener productos del inventario para entregas
            List<Product> products1 = inventoryService.getProductsByCategory(Category.ELECTRONICS, 3);
            List<Product> products2 = inventoryService.getProductsByCategory(Category.CONSOLES, 2);
            List<Product> products3 = inventoryService.getProductsByCategory(Category.VIDEOGAMES, 5);

            // Crear tareas de entrega
            System.out.println("\n--- Creando tarea de entrega 1 ---");
            var task1 = deliveryService.createDeliveryTask(products1, "Ciudad A");
            
            System.out.println("\n--- Creando tarea de entrega 2 ---");
            var task2 = deliveryService.createDeliveryTask(products2, "Ciudad B");
            
            System.out.println("\n--- Creando tarea de entrega 3 ---");
            var task3 = deliveryService.createDeliveryTask(products3, "Ciudad C");

            System.out.println("\n═══════════════════════════════════════════════════════════");
            System.out.println("FASE 4: Ejecución de entregas");
            System.out.println("═══════════════════════════════════════════════════════════");

            // Ejecutar entregas
            if (task1 != null) {
                System.out.println("\n--- Ejecutando entrega 1 ---");
                deliveryService.assignAndExecuteDelivery(task1);
            }

            Thread.sleep(2000); // Esperar un poco

            if (task2 != null) {
                System.out.println("\n--- Ejecutando entrega 2 ---");
                deliveryService.assignAndExecuteDelivery(task2);
            }

            Thread.sleep(2000); // Esperar un poco

            if (task3 != null) {
                System.out.println("\n--- Ejecutando entrega 3 ---");
                deliveryService.assignAndExecuteDelivery(task3);
            }

            System.out.println("\n═══════════════════════════════════════════════════════════");
            System.out.println("FASE 5: Resumen de entregas");
            System.out.println("═══════════════════════════════════════════════════════════");

            System.out.println("\n[STATUS] Estado del sistema de entregas:");
            System.out.println("   • Total de tareas: " + deliveryService.getAllTasks().size());
            System.out.println("   • Tareas completadas: " + deliveryService.getCompletedTasks().size());
            System.out.println("   • Tareas pendientes: " + deliveryService.getPendingTasks().size());

            System.out.println("\n[STATUS] Tareas completadas:");
            for (var task : deliveryService.getCompletedTasks()) {
                System.out.println("   • " + task);
                System.out.println("     Tiempo total: " + String.format("%.2f", task.getTotalTime()) + " horas");
            }

            System.out.println("\n[STATUS] Estado del almacén después de las entregas:");
            System.out.println("   • Productos restantes: " + warehouse.getTotalProducts());
            System.out.println("   • Carga actual: " + String.format("%.2f", warehouse.getCurrentLoad()) + " kg");
            System.out.println("   • Ocupación: " + String.format("%.1f", warehouse.getOccupancyPercentage()) + "%");

            System.out.println("\n═══════════════════════════════════════════════════════════");
            System.out.println("FASE 6: Generación de reporte");
            System.out.println("═══════════════════════════════════════════════════════════");

            // Generar reporte
            deliveryService.generateReport("delivery_report.txt");
            
            System.out.println("\n[OK] Prueba del sistema de entregas completada exitosamente");

            clock.stop();

        } catch (InterruptedException e) {
            System.out.println("[WARNING] Prueba interrumpida");
            Thread.currentThread().interrupt();
        }
    }
}

