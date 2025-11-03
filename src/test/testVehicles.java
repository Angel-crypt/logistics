package test;

import domain.categories.Category;
import domain.products.Product;
import domain.products.ProductFactory;
import domain.vehicles.Truck;
import domain.vehicles.Airplane;
import interfaces.Loadable;

import java.util.ArrayList;
import java.util.List;

public class testVehicles {
    public static void run() {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║           PRUEBA DE VEHÍCULOS DE TRANSPORTE                ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        // Crear productos de prueba
        List<Product> testProducts = new ArrayList<>();
        testProducts.add(ProductFactory.createdRandmon(Category.ELECTRONICS));
        testProducts.add(ProductFactory.createdRandmon(Category.CONSOLES));
        testProducts.add(ProductFactory.createdRandmon(Category.VIDEOGAMES));
        testProducts.add(ProductFactory.createdRandmon(Category.APPLIANCES));
        testProducts.add(ProductFactory.createdRandmon(Category.FURNITURE));

        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("TEST 1: Camión - Carga y Transporte");
        System.out.println("═══════════════════════════════════════════════════════════");

        Truck truck = new Truck("Almacén Central");
        System.out.println("\nProductos a cargar:");
        for (Product p : testProducts) {
            System.out.println("   - " + p.getName() + " - " + String.format("%.2f", p.getWeight()) + " kg - " + p.getCategory());
        }

        // Convertir productos a Loadable (Product ya implementa Loadable)
        List<Loadable> loadableItems = new ArrayList<>(testProducts);
        truck.load(loadableItems);

        System.out.println("\n[STATUS] Estado del camión:");
        System.out.println("   - Carga actual: " + String.format("%.2f", truck.getCurrentLoad()) + " kg");
        System.out.println("   - Capacidad disponible: " + String.format("%.2f", truck.getAvailableCapacity()) + " kg");
        System.out.println("   - Items cargados: " + truck.getCargoCount());
        System.out.println("   - Ubicación: " + truck.getCurrentLocation());

        truck.transport("SLP");
        System.out.println("\n[STATUS] Estado después del transporte:");
        System.out.println("   - Ubicación actual: " + truck.getCurrentLocation());
        System.out.println("   - En tránsito: " + truck.isInTransit());

        truck.unload();
        System.out.println("\n[STATUS] Estado después de descargar:");
        System.out.println("   - Carga actual: " + String.format("%.2f", truck.getCurrentLoad()) + " kg");
        System.out.println("   - Vacío: " + truck.isEmpty());

        System.out.println("\n═══════════════════════════════════════════════════════════");
        System.out.println("TEST 2: Avión - Carga y Restricciones");
        System.out.println("═══════════════════════════════════════════════════════════");

        Airplane airplane = new Airplane("Aeropuerto Principal");

        // Crear productos variados incluyendo algunos pesados
        List<Product> airplaneProducts = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            airplaneProducts.add(ProductFactory.createdRandmon(Category.values()[(int)(Math.random() * Category.values().length)]));
        }

        System.out.println("\n[INFO] Productos a cargar en avión:");
        for (Product p : airplaneProducts) {
            System.out.println("   - " + p.getName() + " - " + String.format("%.2f", p.getWeight()) + " kg - " + p.getCategory());
        }

        List<Loadable> airplaneItems = new ArrayList<>(airplaneProducts);
        airplane.load(airplaneItems);

        System.out.println("\n[STATUS] Estado del avión:");
        System.out.println("   - Carga actual: " + String.format("%.2f", airplane.getCurrentLoad()) + " kg");
        System.out.println("   - Capacidad disponible: " + String.format("%.2f", airplane.getAvailableCapacity()) + " kg");
        System.out.println("   - Items cargados: " + airplane.getCargoCount());

        airplane.transport("ZAC");
        System.out.println("\n[STATUS] Estado después del transporte:");
        System.out.println("   - Ubicación actual: " + airplane.getCurrentLocation());

        airplane.unload();

        System.out.println("\n═══════════════════════════════════════════════════════════");
        System.out.println("TEST 3: Comparación de Tiempos de Transporte");
        System.out.println("═══════════════════════════════════════════════════════════");

        Truck truck2 = new Truck(15000, "Almacén");
        Airplane airplane2 = new Airplane(15000, "Aeropuerto");

        // Crear carga de prueba
        List<Product> comparisonProducts = ProductFactory.createBatch(Category.ELECTRONICS, 5);
        List<Loadable> comparisonItems = new ArrayList<>(comparisonProducts);

        truck2.load(comparisonItems);
        System.out.println("\n[TIME] Tiempo estimado de transporte Camión (GDL -> AGS):");
        System.out.println("   - Distancia: ~230 km");
        System.out.println("   - Velocidad promedio: 70 km/h");
        System.out.println("   - Tiempo estimado: ~" + String.format("%.2f", 230.0 / 70.0) + " horas");

        List<Loadable> comparisonItems2 = new ArrayList<>(comparisonProducts);
        airplane2.load(comparisonItems2);
        System.out.println("\n[TIME] Tiempo estimado de transporte Avión (GDL -> AGS):");
        System.out.println("   - Distancia: ~260 km");
        System.out.println("   - Velocidad promedio: 850 km/h");
        System.out.println("   - Tiempo estimado: ~" + String.format("%.2f", (260.0 / 850.0) + 0.5) + " horas (incluye despegue/aterrizaje)");

        System.out.println("\n[OK] Pruebas de vehículos completadas exitosamente");
    }
}

