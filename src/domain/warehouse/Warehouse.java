package domain.warehouse;

import java.util.*;
import java.util.concurrent.*;
import domain.categories.Category;
import domain.products.Product;
import domain.products.ProductFactory;
import utils.TimeUtils;
import utils.SimulatedClock;
import utils.SimulatedWeek;

/**
 * Gestiona el inventario concurrente de productos agrupados por categoría.
 * Permite rellenar el inventario aleatoriamente de forma manual o automática.
 * El llenado automático solo ocurre entre las 20:00 y las 09:00 horas simuladas.
 *
 * Esta clase es thread-safe y utiliza estructuras concurrentes para manejar
 * operaciones simultáneas sobre el inventario.
 *
 * @author Sistema Logístico
 * @version 2.0
 */
public class Warehouse {

    private final ConcurrentHashMap<Category, List<Product>> inventory;
    private double maxCapacity; // Capacidad máxima en kg
    private final Object refillLock = new Object();
    private Thread refillThread;
    private volatile boolean running = false;
    private SimulatedClock clock;
    private SimulatedWeek week;

    /**
     * Constructor del almacén.
     * Inicializa el inventario vacío y establece la capacidad máxima.
     *
     * @param maxCapacity Capacidad máxima del almacén en kilogramos
     */
    public Warehouse(double maxCapacity) {
        this.inventory = new ConcurrentHashMap<>();
        this.maxCapacity = maxCapacity;

        // Inicializar listas para cada categoría
        for (Category category : Category.values()) {
            inventory.put(category, new CopyOnWriteArrayList<>());
        }

        System.out.println("🏭 Warehouse inicializado con capacidad máxima: " + maxCapacity + " kg");
    }

    /**
     * Rellena el inventario aleatoriamente con productos hasta alcanzar el stock objetivo.
     * Respeta la capacidad máxima del almacén y simula tiempo de carga por categoría.
     *
     * @param targetStock Peso objetivo de stock en kilogramos
     */
    public void fillRandomly(double targetStock) {
        synchronized (refillLock) {
            double currentLoad = getCurrentLoad();
            double availableCapacity = Math.min(maxCapacity - currentLoad, targetStock);

            if (availableCapacity <= 0) {
                System.out.println("⚠️  Warehouse lleno. Capacidad actual: " +
                        String.format("%.2f", currentLoad) + "/" + maxCapacity + " kg");
                return;
            }

            System.out.println("\n📦 Iniciando rellenado de inventario...");
            System.out.println("   Capacidad disponible: " + String.format("%.2f", availableCapacity) + " kg");

            double addedWeight = 0;
            int addedProducts = 0;
            Category[] categories = Category.values();
            Random random = new Random();

            // Distribuir productos entre categorías
            while (addedWeight < availableCapacity) {
                Category category = categories[random.nextInt(categories.length)];

                try {
                    // Simular tiempo de carga (0.01-0.04 horas simuladas por producto)
                    double loadTime = TimeUtils.randomInRange(0.01, 0.04);
                    TimeUtils.sleepSimulated(loadTime);

                    // Generar producto aleatorio usando ProductFactory
                    Product product = ProductFactory.createdRandmon(category);

                    // Verificar si cabe el producto en la capacidad disponible
                    if (addedWeight + product.getWeight() <= availableCapacity) {
                        inventory.get(category).add(product);
                        addedWeight += product.getWeight();
                        addedProducts++;

                        System.out.println("   ✓ Agregado: " + product.getName() +
                                " (" + String.format("%.2f", product.getWeight()) + " kg) - " +
                                category.name());
                    } else {
                        // Si el producto no cabe, intentamos con categorías más ligeras
                        boolean added = false;
                        for (Category lightCategory : categories) {
                            if (lightCategory.getMaxWeight() <= availableCapacity - addedWeight) {
                                Product lightProduct = ProductFactory.createdRandmon(lightCategory);
                                if (addedWeight + lightProduct.getWeight() <= availableCapacity) {
                                    inventory.get(lightCategory).add(lightProduct);
                                    addedWeight += lightProduct.getWeight();
                                    addedProducts++;
                                    System.out.println("   ✓ Agregado: " + lightProduct.getName() +
                                            " (" + String.format("%.2f", lightProduct.getWeight()) + " kg) - " +
                                            lightCategory.name());
                                    added = true;
                                    break;
                                }
                            }
                        }
                        if (!added) break; // No hay más espacio disponible
                    }

                } catch (InterruptedException e) {
                    System.out.println("⚠️  Proceso de rellenado interrumpido");
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            System.out.println("\n✅ Rellenado completado:");
            System.out.println("   • Productos agregados: " + addedProducts);
            System.out.println("   • Peso agregado: " + String.format("%.2f", addedWeight) + " kg");
            System.out.println("   • Carga total: " + String.format("%.2f", getCurrentLoad()) + "/" + maxCapacity + " kg");
            printInventorySummary();
        }
    }

    /**
     * Determina si el almacén debe realizar el llenado en la hora actual.
     * El llenado solo ocurre entre las 20:00 y las 09:00 horas.
     *
     * @param currentHour Hora simulada actual
     * @return true si está dentro del horario de llenado, false en caso contrario
     */
    private boolean shouldRefill(double currentHour) {
        int hour = (int) currentHour % 24;
        // Entre 20:00 (8 PM) y 09:00 (9 AM)
        return hour >= 20 || hour < 9;
    }

    /**
     * Inicia el proceso automático de rellenado nocturno del inventario.
     * El llenado solo ocurre entre las 20:00 y las 09:00 horas simuladas.
     *
     * @param clock Reloj simulado del sistema
     * @param week Semana simulada del sistema
     */
    public void startAutomatedRefill(SimulatedClock clock, SimulatedWeek week) {
        if (running) {
            System.out.println("⚠️  El rellenado automático ya está en ejecución");
            return;
        }

        this.clock = clock;
        this.week = week;
        running = true;

        refillThread = new Thread(() -> {
            System.out.println("🌙 Servicio de rellenado automático iniciado");
            System.out.println("   Horario de operación: 20:00 - 09:00");

            boolean wasRefilling = false;
            double lastRefillCheck = -1;

            try {
                while (running && !Thread.currentThread().isInterrupted()) {
                    Thread.sleep(100); // Verificar cada 100ms

                    double currentHour = clock.getCurrentSimulatedTime();
                    int hourOfDay = (int) currentHour % 24;
                    boolean shouldRefillNow = shouldRefill(currentHour);

                    // Detectar el cambio exacto a las 20:00
                    if (shouldRefillNow && !wasRefilling && hourOfDay == 20) {
                        System.out.println("\n[Warehouse] Día: " + week.getCurrentDay() +
                                " — Iniciando proceso de llenado (20:00)");
                        wasRefilling = true;
                        lastRefillCheck = currentHour;
                    }

                    // Detectar el cambio exacto a las 09:00
                    if (!shouldRefillNow && wasRefilling && hourOfDay == 9) {
                        System.out.println("\n[Warehouse] Día: " + week.getCurrentDay() +
                                " — Finalizando proceso de llenado (09:00)");
                        wasRefilling = false;
                        lastRefillCheck = currentHour;
                    }

                    // Realizar llenado si estamos en horario permitido
                    if (shouldRefillNow && wasRefilling) {
                        // Realizar llenado cada hora simulada aproximadamente
                        if (currentHour - lastRefillCheck >= 1.0) {
                            double currentLoad = getCurrentLoad();
                            double capacityNeeded = maxCapacity - currentLoad;

                            if (capacityNeeded > 100) { // Solo llenar si necesita más de 100kg
                                System.out.println("\n[Warehouse] Llenando almacén... Día: " +
                                        week.getCurrentDay() + ", Hora: " +
                                        String.format("%.1f", currentHour % 24));

                                // Calcular stock objetivo (llenar hasta 70-90% de capacidad)
                                double targetStock = maxCapacity * (0.7 + Math.random() * 0.2) - currentLoad;
                                if (targetStock > 0) {
                                    fillRandomly(targetStock);
                                }
                            }
                            lastRefillCheck = currentHour;
                        }
                    }

                    // Mostrar mensaje si estamos fuera del horario
                    if (!shouldRefillNow && !wasRefilling) {
                        // Solo mostrar una vez al cambiar de estado
                        if (lastRefillCheck == -1 || (int)(lastRefillCheck % 24) != hourOfDay) {
                            if (hourOfDay >= 9 && hourOfDay < 20) {
                                System.out.println("\n[Warehouse] Llenado pausado — fuera del horario permitido. " +
                                        "(Hora actual: " + hourOfDay + ":00)");
                                lastRefillCheck = currentHour;
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("🌙 Servicio de rellenado automático detenido");
                Thread.currentThread().interrupt();
            }
        });

        refillThread.setName("AutomatedRefillThread");
        refillThread.setDaemon(true);
        refillThread.start();
    }

    /**
     * Detiene el servicio de rellenado automático.
     */
    public void stopAutomatedRefill() {
        running = false;
        if (refillThread != null && refillThread.isAlive()) {
            refillThread.interrupt();
            System.out.println("🛑 Deteniendo servicio de rellenado automático...");
        }
    }

    /**
     * Calcula y retorna la carga actual total del inventario.
     *
     * @return Peso total actual en kilogramos
     */
    public double getCurrentLoad() {
        return inventory.values().stream()
                .flatMap(List::stream)
                .mapToDouble(Product::getWeight)
                .sum();
    }

    /**
     * Imprime un resumen del inventario actual por categoría.
     */
    public void printInventorySummary() {
        System.out.println("\n📊 Resumen de inventario por categoría:");

        for (Category category : Category.values()) {
            List<Product> products = inventory.get(category);
            double categoryWeight = products.stream()
                    .mapToDouble(Product::getWeight)
                    .sum();

            System.out.println("   • " + category + ": " + products.size() +
                    " productos (" + String.format("%.2f", categoryWeight) + " kg)");
        }
    }

    /**
     * Obtiene el inventario completo actual.
     *
     * @return Mapa inmutable del inventario por categoría
     */
    public Map<Category, List<Product>> getInventory() {
        return Collections.unmodifiableMap(inventory);
    }

    /**
     * Obtiene la capacidad máxima del almacén.
     *
     * @return Capacidad máxima en kilogramos
     */
    public double getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * Establece una nueva capacidad máxima para el almacén.
     *
     * @param maxCapacity Nueva capacidad máxima en kilogramos
     */
    public void setMaxCapacity(double maxCapacity) {
        this.maxCapacity = maxCapacity;
        System.out.println("📏 Capacidad máxima actualizada a: " + maxCapacity + " kg");
    }

    /**
     * Obtiene el número total de productos en el inventario.
     *
     * @return Cantidad total de productos
     */
    public int getTotalProducts() {
        return inventory.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    /**
     * Obtiene el porcentaje de ocupación actual del almacén.
     *
     * @return Porcentaje de ocupación (0-100)
     */
    public double getOccupancyPercentage() {
        return (getCurrentLoad() / maxCapacity) * 100;
    }

    /**
     * Remueve productos del inventario.
     *
     * @param productsToRemove Lista de productos a remover
     * @return Lista de productos que fueron removidos exitosamente
     */
    public List<Product> removeProducts(List<Product> productsToRemove) {
        List<Product> removedProducts = new ArrayList<>();
        
        if (productsToRemove == null || productsToRemove.isEmpty()) {
            return removedProducts;
        }

        synchronized (refillLock) {
            for (Product productToRemove : productsToRemove) {
                if (productToRemove == null) continue;

                Category category = productToRemove.getCategory();
                List<Product> categoryProducts = inventory.get(category);

                if (categoryProducts != null) {
                    // Buscar y remover el producto (comparando por nombre y peso para coincidencia)
                    boolean removed = categoryProducts.removeIf(p -> 
                        p.getName().equals(productToRemove.getName()) &&
                        Math.abs(p.getWeight() - productToRemove.getWeight()) < 0.01
                    );

                    if (removed) {
                        removedProducts.add(productToRemove);
                    }
                }
            }
        }

        return removedProducts;
    }

    /**
     * Obtiene productos de una categoría específica del inventario.
     *
     * @param category Categoría de productos
     * @param count Cantidad de productos a obtener
     * @return Lista de productos obtenidos
     */
    public List<Product> getProductsByCategory(Category category, int count) {
        List<Product> result = new ArrayList<>();
        
        if (category == null || count <= 0) {
            return result;
        }

        List<Product> categoryProducts = inventory.get(category);
        if (categoryProducts == null || categoryProducts.isEmpty()) {
            return result;
        }

        int toTake = Math.min(count, categoryProducts.size());
        for (int i = 0; i < toTake; i++) {
            result.add(categoryProducts.get(i));
        }

        return result;
    }

    /**
     * Obtiene todos los productos de una categoría específica.
     *
     * @param category Categoría de productos
     * @return Lista de productos de la categoría
     */
    public List<Product> getAllProductsByCategory(Category category) {
        if (category == null) {
            return new ArrayList<>();
        }

        List<Product> categoryProducts = inventory.get(category);
        return categoryProducts != null ? new ArrayList<>(categoryProducts) : new ArrayList<>();
    }

    /**
     * Verifica si hay suficientes productos en el inventario para una entrega.
     *
     * @param requiredProducts Lista de productos requeridos
     * @return true si hay suficientes productos disponibles
     */
    public boolean hasEnoughProducts(List<Product> requiredProducts) {
        if (requiredProducts == null || requiredProducts.isEmpty()) {
            return true;
        }

        Map<Category, Integer> requiredCount = new HashMap<>();
        for (Product product : requiredProducts) {
            requiredCount.merge(product.getCategory(), 1, Integer::sum);
        }

        for (Map.Entry<Category, Integer> entry : requiredCount.entrySet()) {
            Category category = entry.getKey();
            int required = entry.getValue();
            List<Product> available = inventory.get(category);
            
            if (available == null || available.size() < required) {
                return false;
            }
        }

        return true;
    }
}