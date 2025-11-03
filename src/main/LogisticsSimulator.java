package main;

import domain.categories.Category;
import domain.delivery.DeliveryTask;
import domain.products.Product;
import domain.vehicles.Airplane;
import domain.vehicles.Truck;
import domain.vehicles.Vehicle;
import domain.warehouse.Warehouse;
import services.DeliveryService;
import services.InventoryService;
import utils.SimulatedClock;
import utils.SimulatedWeek;
import utils.TimeUtils;

import java.util.*;

/**
 * Simulador del sistema logístico de Guadalajara.
 * Gestiona entregas foráneas y locales según el calendario establecido.
 */
public class LogisticsSimulator {

    private static final String DISTRIBUTION_CENTER = "GDL";
    private static final String[] FORANE_CITIES = {"SLP", "ZAC", "AGS"};
    
    private Warehouse warehouse;
    private DeliveryService deliveryService;
    private InventoryService inventoryService;
    private SimulatedClock clock;
    private SimulatedWeek week;
    
    // Contadores de envíos por ciudad
    private Map<String, Integer> deliveryCounters = new HashMap<>();
    
    // Vehículos foráneos
    private List<Vehicle> foraneTrucks = new ArrayList<>();
    private List<Vehicle> foraneAirplanes = new ArrayList<>();
    
    // Camiones locales
    private List<Truck> localTrucks = new ArrayList<>();

    /**
     * Inicializa el simulador logístico.
     */
    public void initialize() {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║     SISTEMA LOGÍSTICO GUADALAJARA - INICIALIZACIÓN        ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");
        
        // Crear reloj simulado (1 hora simulada = 10 segundos reales para simulación rápida)
        clock = new SimulatedClock(10000);
        Thread clockThread = new Thread(clock);
        clockThread.setDaemon(true);
        clockThread.start();
        
        // Crear semana simulado
        week = new SimulatedWeek(clock);
        Thread weekThread = new Thread(week);
        weekThread.setDaemon(true);
        weekThread.start();
        
        // Esperar inicialización
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Crear almacén en GDL
        warehouse = new Warehouse(50000.0); // Capacidad grande para centro de distribución
        
        // Crear servicios
        deliveryService = new DeliveryService(warehouse, clock);
        inventoryService = new InventoryService(warehouse);
        
        // Inicializar contadores de envíos
        for (String city : FORANE_CITIES) {
            deliveryCounters.put(city, 0);
        }
        deliveryCounters.put("GDL", 0);
        
        // Inicializar vehículos foráneos
        initializeForaneVehicles();
        
        // Inicializar camiones locales
        initializeLocalTrucks();
        
        // Llenar almacén inicial y establecer carga de referencia
        System.out.println("\n[INIT] Llenando almacén inicial...");
        warehouse.fillRandomly(20000.0); // Aumentado de 5000 a 20000 kg para soportar más entregas
        // La carga inicial será la referencia para el primer día
        warehouse.setInitialLoadReference();
        
        // Iniciar servicio de rellenado automático
        warehouse.startAutomatedRefill(clock, week);
        
        System.out.println("\n[OK] Sistema logístico inicializado correctamente");
    }

    /**
     * Inicializa vehículos para entregas foráneas.
     * IMPORTANTE: El orden de creación debe coincidir con FORANE_CITIES.
     */
    private void initializeForaneVehicles() {
        System.out.println("\n[INIT] Inicializando vehículos foráneos...");
        System.out.println("[INIT] Ciudades foráneas: " + String.join(", ", FORANE_CITIES));
        
        // Limpiar listas antes de inicializar
        foraneTrucks.clear();
        foraneAirplanes.clear();
        
        // 1 camión por ciudad para días terrestres (Lunes, Miércoles, Viernes)
        // El orden DEBE coincidir con FORANE_CITIES
        for (int i = 0; i < FORANE_CITIES.length; i++) {
            String city = FORANE_CITIES[i];
            Truck truck = new Truck(500.0, DISTRIBUTION_CENTER);
            truck.setCurrentLocation(DISTRIBUTION_CENTER);
            foraneTrucks.add(truck);
            deliveryService.registerVehicle(truck);
            System.out.println(String.format("[INIT] Camión %d asignado a %s", i, city));
        }
        
        // 1 avión por ciudad para días aéreos (Domingo, Jueves)
        // El orden DEBE coincidir con FORANE_CITIES
        for (int i = 0; i < FORANE_CITIES.length; i++) {
            String city = FORANE_CITIES[i];
            Airplane airplane = new Airplane(1000.0, DISTRIBUTION_CENTER + " Aeropuerto");
            airplane.setCurrentLocation(DISTRIBUTION_CENTER + " Aeropuerto");
            foraneAirplanes.add(airplane);
            deliveryService.registerVehicle(airplane);
            System.out.println(String.format("[INIT] Avión %d asignado a %s", i, city));
        }
        
        System.out.println(String.format("[INIT] Total camiones: %d, Total aviones: %d", 
            foraneTrucks.size(), foraneAirplanes.size()));
    }

    /**
     * Inicializa camiones para entregas locales en GDL.
     */
    private void initializeLocalTrucks() {
        System.out.println("\n[INIT] Inicializando camiones locales...");
        
        // Camión grande (500 kg)
        Truck grande = new Truck(Truck.TruckSize.GRANDE, DISTRIBUTION_CENTER);
        localTrucks.add(grande);
        deliveryService.registerVehicle(grande);
        
        // Camión mediano (250 kg)
        Truck mediano = new Truck(Truck.TruckSize.MEDIANO, DISTRIBUTION_CENTER);
        localTrucks.add(mediano);
        deliveryService.registerVehicle(mediano);
        
        // Camión pequeño (150 kg)
        Truck pequeño = new Truck(Truck.TruckSize.PEQUEÑO, DISTRIBUTION_CENTER);
        localTrucks.add(pequeño);
        deliveryService.registerVehicle(pequeño);
    }

    /**
     * Ejecuta el proceso de entregas foráneas según el día.
     * Cada vehículo ejecuta su entrega en un hilo separado (paralelo).
     */
    private void processForaneDeliveries() {
        String currentDay = week.getCurrentDay();
        boolean isTruckDay = currentDay.equals("Lunes") || currentDay.equals("Miércoles") || currentDay.equals("Viernes");
        boolean isAirplaneDay = currentDay.equals("Domingo") || currentDay.equals("Jueves");
        
        if (!isTruckDay && !isAirplaneDay) {
            return; // Martes y Sábado no hay entregas foráneas
        }
        
        List<Vehicle> availableVehicles = isTruckDay ? new ArrayList<>(foraneTrucks) : new ArrayList<>(foraneAirplanes);
        double capacityPerVehicle = isTruckDay ? 500.0 : 1000.0;
        String vehicleType = isTruckDay ? "Camión" : "Avión";
        
        System.out.println(String.format("\n[FORANE] %s - Entregas foráneas por %s (capacidad: %.0f kg)", 
            currentDay, vehicleType, capacityPerVehicle));
        
        List<Thread> deliveryThreads = new ArrayList<>();
        
        for (int i = 0; i < FORANE_CITIES.length; i++) {
            String city = FORANE_CITIES[i];
            
            // Verificar que tenemos suficientes vehículos
            if (i >= availableVehicles.size()) {
                System.out.println(String.format("[ERROR] No hay vehículo disponible para %s (índice %d, vehículos disponibles: %d)", 
                    city, i, availableVehicles.size()));
                continue;
            }
            
            Vehicle vehicle = availableVehicles.get(i);
            final boolean isTruck = isTruckDay;
            final double capacity = capacityPerVehicle;
            final int cityIndex = i; // Capturar índice para uso en el hilo
            
            System.out.println(String.format("[FORANE] Asignando vehículo %d (%s) a ciudad %s", 
                i, vehicle.getClass().getSimpleName(), city));
            
            // Crear un hilo para cada entrega
            Thread deliveryThread = new Thread(() -> {
                // Verificar que el vehículo esté disponible
                if (vehicle.isInTransit()) {
                    System.out.println(String.format("[WARNING] Vehículo %d para %s aún en tránsito", cityIndex, city));
                    return;
                }
                
                // Asegurar que el vehículo esté en GDL
                if (isTruck) {
                    vehicle.setCurrentLocation(DISTRIBUTION_CENTER);
                } else {
                    vehicle.setCurrentLocation(DISTRIBUTION_CENTER + " Aeropuerto");
                }
                
                try {
                    // Obtener productos del inventario
                    List<Product> products = selectProductsForDelivery(capacity);
                    
                    if (products.isEmpty()) {
                        System.out.println(String.format("[WARNING] No hay productos suficientes para entrega a %s", city));
                        return;
                    }
                    
                    System.out.println(String.format("[FORANE] Ciudad %s: Seleccionados %d productos (%.2f kg)", 
                        city, products.size(), products.stream().mapToDouble(Product::getWeight).sum()));
                    
                    // Crear tarea de entrega
                    DeliveryTask task = deliveryService.createDeliveryTask(products, city);
                    
                    if (task == null) {
                        System.out.println(String.format("[WARNING] No se pudo crear tarea de entrega para %s", city));
                        return;
                    }
                    
                    // Asegurar que el vehículo esté disponible (no en tránsito y vacío)
                    if (vehicle.isInTransit()) {
                        System.out.println(String.format("[WARNING] Vehículo %d para %s aún está en tránsito", cityIndex, city));
                        return;
                    }
                    
                    // Si el vehículo tiene carga, debe descargar primero
                    if (!vehicle.isEmpty()) {
                        vehicle.unload();
                    }
                    
                    // Asignar vehículo específico antes de ejecutar
                    if (!task.assignVehicle(vehicle)) {
                        System.out.println(String.format("[WARNING] No se pudo asignar vehículo %d a la tarea para %s", cityIndex, city));
                        return;
                    }
                    
                    // Ejecutar entrega (assignAndExecuteDelivery verificará nuevamente la asignación)
                    boolean success = deliveryService.assignAndExecuteDelivery(task);
                    
                    if (success) {
                        synchronized (deliveryCounters) {
                            deliveryCounters.put(city, deliveryCounters.get(city) + 1);
                            int deliveryNum = deliveryCounters.get(city);
                            
                            System.out.println(String.format("[OK] Entrega a %s completada exitosamente (envío #%d)", city, deliveryNum));
                            
                            // Generar reporte por ciudad-envío
                            deliveryService.generateCityDeliveryReport(city, deliveryNum, task);
                        }
                    } else {
                        System.out.println(String.format("[ERROR] Falló la ejecución de entrega a %s", city));
                    }
                    
                    // Regresar vehículo a GDL después de la entrega
                    try {
                        TimeUtils.sleepSimulated(0.5); // Tiempo de retorno
                        if (isTruck) {
                            vehicle.setCurrentLocation(DISTRIBUTION_CENTER);
                        } else {
                            vehicle.setCurrentLocation(DISTRIBUTION_CENTER + " Aeropuerto");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                } catch (Exception e) {
                    System.out.println(String.format("[ERROR] Excepción en entrega a %s: %s", city, e.getMessage()));
                    e.printStackTrace();
                }
            }, "DeliveryThread-" + city + "-" + vehicleType);
            
            deliveryThreads.add(deliveryThread);
            deliveryThread.start();
        }
        
        // Esperar a que todas las entregas terminen
        for (Thread thread : deliveryThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.println("[WARNING] Hilo de entrega interrumpido");
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Ejecuta el proceso de entregas locales en GDL.
     * Cada camión ejecuta sus entregas en un hilo separado (paralelo).
     */
    private void processLocalDeliveries() {
        System.out.println("\n[LOCAL] Iniciando entregas locales en GDL...");
        
        // Cada camión realiza 8 envíos diarios
        int deliveriesPerTruck = 8;
        
        List<Thread> truckThreads = new ArrayList<>();
        
        for (Truck truck : localTrucks) {
            double truckCapacity = truck.getMaxCapacity();
            
            // Crear un hilo para cada camión
            Thread truckThread = new Thread(() -> {
                for (int delivery = 1; delivery <= deliveriesPerTruck; delivery++) {
                    // Seleccionar productos según capacidad del camión
                    double targetWeight = truckCapacity * (0.5 + Math.random() * 0.4); // Entre 50-90% de capacidad
                    List<Product> products = selectProductsForDelivery(targetWeight);
                    
                    if (products.isEmpty()) {
                        System.out.println(String.format("[WARNING] No hay productos suficientes para entrega local #%d", delivery));
                        continue;
                    }
                    
                    // Crear tarea de entrega local
                    DeliveryTask task = deliveryService.createDeliveryTask(products, "GDL");
                    
                    if (task != null) {
                        // Esperar a que el camión regrese si está en tránsito
                        while (truck.isInTransit()) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }
                        
                        // Asegurar que el camión esté en GDL
                        truck.setCurrentLocation(DISTRIBUTION_CENTER);
                        
                        // Asignar camión específico
                        if (!task.assignVehicle(truck)) {
                            System.out.println(String.format("[WARNING] No se pudo asignar camión a la tarea local #%d", delivery));
                            continue;
                        }
                        
                        // Ejecutar entrega
                        boolean success = deliveryService.assignAndExecuteDelivery(task);
                        
                        if (success) {
                            synchronized (deliveryCounters) {
                                deliveryCounters.put("GDL", deliveryCounters.get("GDL") + 1);
                                int deliveryNum = deliveryCounters.get("GDL");
                                String currentDay = week.getCurrentDay();
                                int dayNumber = clock.getCurrentDay();
                                
                                // Generar reporte por ciudad-envío con estructura por día para GDL
                                deliveryService.generateCityDeliveryReport("GDL", deliveryNum, task, currentDay, dayNumber);
                            }
                        }
                    }
                    
                    // Pequeña pausa entre entregas del mismo camión
                    try {
                        TimeUtils.sleepSimulated(0.5);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }, "LocalTruckThread-" + truck.getClass().getSimpleName() + "-" + truck.getMaxCapacity() + "kg");
            
            truckThreads.add(truckThread);
            truckThread.start();
        }
        
        // Esperar a que todas las entregas locales terminen
        for (Thread thread : truckThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.println("[WARNING] Hilo de entrega local interrumpido");
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Selecciona productos del inventario para una entrega.
     * NOTA: Los productos NO se remueven del inventario aquí,
     * solo se seleccionan. Se remueven cuando se ejecuta la entrega.
     * 
     * Sincronizado para evitar que múltiples hilos seleccionen los mismos productos.
     */
    private final Object selectionLock = new Object();
    
    private List<Product> selectProductsForDelivery(double targetWeight) {
        synchronized (selectionLock) {
            List<Product> selectedProducts = new ArrayList<>();
            double currentWeight = 0.0;
            int maxIterations = 1000; // Limitar iteraciones para evitar bucles infinitos
            int iteration = 0;
            
            Map<Category, List<Product>> inventory = warehouse.getInventory();
            List<Category> categories = new ArrayList<>(inventory.keySet());
            Collections.shuffle(categories);
            
            // Crear una copia de las categorías para poder reintentar
            List<Category> allCategories = new ArrayList<>(categories);
            
            while (currentWeight < targetWeight && iteration < maxIterations) {
                iteration++;
                
                // Si no quedan categorías, reintentar desde el principio
                if (categories.isEmpty()) {
                    if (selectedProducts.isEmpty()) {
                        break; // No hay productos disponibles
                    }
                    categories = new ArrayList<>(allCategories);
                }
                
                Category category = categories.get(0);
                List<Product> categoryProducts = inventory.get(category);
                
                if (categoryProducts != null && !categoryProducts.isEmpty()) {
                    // Buscar un producto que quepa y no esté ya seleccionado
                    boolean found = false;
                    // Iterar sobre una copia para evitar problemas de concurrencia
                    List<Product> productsCopy = new ArrayList<>(categoryProducts);
                    for (Product product : productsCopy) {
                        // Verificar que el producto no haya sido ya seleccionado
                        // y que todavía esté disponible en el inventario
                        if (!selectedProducts.contains(product) && 
                            categoryProducts.contains(product) && // Verificar que todavía existe
                            currentWeight + product.getWeight() <= targetWeight) {
                            selectedProducts.add(product);
                            currentWeight += product.getWeight();
                            found = true;
                            break;
                        }
                    }
                    
                    if (!found) {
                        // No hay productos que quepan de esta categoría
                        categories.remove(0);
                    }
                } else {
                    categories.remove(0);
                }
                
                // Si ya tenemos algo cercano al peso objetivo, detener
                if (currentWeight >= targetWeight * 0.8) {
                    break;
                }
            }
            
            return selectedProducts;
        }
    }

    /**
     * Ejecuta un ciclo de simulación (un día).
     */
    public void runSimulationDay() {
        String currentDay = week.getCurrentDay();
        String currentTime = clock.getCurrentTimeString();
        System.out.println("\n" + "=".repeat(80));
        System.out.println(String.format("DÍA: %s - %s", currentDay, currentTime));
        System.out.println("=".repeat(80));
        
        try {
            // Procesar entregas foráneas según el día
            processForaneDeliveries();
            
            // Esperar un poco antes de entregas locales
            TimeUtils.sleepSimulated(2.0);
            
            // Procesar entregas locales (todos los días)
            processLocalDeliveries();
            
            // Esperar hasta el siguiente día
            double currentHour = clock.getCurrentSimulatedTime();
            double nextDayHour = Math.ceil(currentHour / 24.0) * 24.0;
            double waitTime = nextDayHour - currentHour;
            
            if (waitTime > 0) {
                int currentDayNum = clock.getCurrentDay();
                int currentHourNum = clock.getCurrentHourOfDay();
                int targetHour = (currentHourNum + (int)waitTime) % 24;
                int targetDay = targetHour < currentHourNum ? currentDayNum + 1 : currentDayNum;
                System.out.println(String.format("\n[WAIT] Esperando hasta Día %d, Hora %d...", targetDay, targetHour));
                TimeUtils.sleepSimulated(waitTime);
            }
            
        } catch (InterruptedException e) {
            System.out.println("[WARNING] Simulación interrumpida");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Ejecuta la simulación por un número de días.
     */
    public void runSimulation(int days) {
        System.out.println(String.format("\n[START] Iniciando simulación por %d días", days));
        
        for (int day = 1; day <= days; day++) {
            runSimulationDay();
        }
        
        // Generar reporte final
        System.out.println("\n[REPORT] Generando reporte final...");
        deliveryService.generateReport("reports/reporte_final.txt");
        
        System.out.println("\n[OK] Simulación completada");
        System.out.println("\n[STATS] Estadísticas de entregas:");
        for (Map.Entry<String, Integer> entry : deliveryCounters.entrySet()) {
            System.out.println(String.format("   %s: %d envíos completados", entry.getKey(), entry.getValue()));
        }
    }

    /**
     * Detiene la simulación.
     */
    public void stop() {
        if (clock != null) {
            clock.stop();
        }
        if (week != null) {
            week.stop();
        }
        if (warehouse != null) {
            warehouse.stopAutomatedRefill();
        }
        System.out.println("\n[STOP] Simulación detenida");
    }

    // ============================================================
    // MÉTODOS PÚBLICOS - API PARA USO EXTERNO
    // ============================================================
    
    /**
     * Obtiene el reloj simulado del sistema.
     * @return instancia de SimulatedClock
     */
    public SimulatedClock getClock() {
        return clock;
    }

    /**
     * Obtiene la semana simulada del sistema.
     * @return instancia de SimulatedWeek
     */
    public SimulatedWeek getWeek() {
        return week;
    }

    /**
     * Obtiene el almacén del sistema.
     * @return instancia de Warehouse
     */
    public Warehouse getWarehouse() {
        return warehouse;
    }

    /**
     * Obtiene el servicio de entregas.
     * @return instancia de DeliveryService
     */
    public DeliveryService getDeliveryService() {
        return deliveryService;
    }

    /**
     * Obtiene el servicio de inventario.
     * @return instancia de InventoryService
     */
    public InventoryService getInventoryService() {
        return inventoryService;
    }

    /**
     * Obtiene las estadísticas de entregas por ciudad.
     * @return mapa con ciudad -> número de envíos completados
     */
    public Map<String, Integer> getDeliveryStatistics() {
        return new HashMap<>(deliveryCounters);
    }

    /**
     * Obtiene el tiempo actual en formato "Día X, Hora Y".
     * @return string con el tiempo actual
     */
    public String getCurrentTimeString() {
        if (clock != null) {
            return clock.getCurrentTimeString();
        }
        return "No inicializado";
    }

    /**
     * Obtiene el día de la semana actual.
     * @return nombre del día actual
     */
    public String getCurrentDayName() {
        if (week != null) {
            return week.getCurrentDay();
        }
        return "No inicializado";
    }

    /**
     * Método principal para ejecutar el simulador.
     */
    public static void main(String[] args) {
        LogisticsSimulator simulator = new LogisticsSimulator();
        simulator.initialize();
        
        // Ejecutar simulación por 7 días (1 semana)
        simulator.runSimulation(7);
        
        simulator.stop();
    }
    
    /**
     * Ejecuta una prueba corta del simulador (2 días).
     */
    public static void runQuickTest() {
        LogisticsSimulator simulator = new LogisticsSimulator();
        simulator.initialize();
        
        // Ejecutar simulación por 2 días para prueba rápida
        simulator.runSimulation(2);
        
        simulator.stop();
    }
}
