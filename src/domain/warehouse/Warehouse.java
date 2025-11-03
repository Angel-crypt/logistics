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
    private double loadAtDayStart = 0.0; // Carga del almacén al inicio del día (8:00 AM)
    private boolean hasRefilledToday = false; // Flag para controlar rellenado una vez por día

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

        System.out.println("[WAREHOUSE] Warehouse inicializado con capacidad máxima: " + maxCapacity + " kg");
        this.loadAtDayStart = 0.0;
        this.hasRefilledToday = false;
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
                System.out.println("[WARNING] Warehouse lleno. Capacidad actual: " +
                        String.format("%.2f", currentLoad) + "/" + maxCapacity + " kg");
                return;
            }

            System.out.println("\n[WAREHOUSE] Iniciando rellenado de inventario...");
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

                        System.out.println("   [+] Agregado: " + product.getName() +
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
                                    System.out.println("   [+] Agregado: " + lightProduct.getName() +
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
                    System.out.println("[WARNING] Proceso de rellenado interrumpido");
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            System.out.println("\n[OK] Rellenado completado:");
            System.out.println("   - Productos agregados: " + addedProducts);
            System.out.println("   - Peso agregado: " + String.format("%.2f", addedWeight) + " kg");
            System.out.println("   - Carga total: " + String.format("%.2f", getCurrentLoad()) + "/" + maxCapacity + " kg");
            printInventorySummary();
        }
    }

    /**
     * Determina si el almacén debe realizar el llenado en la hora actual.
     * El llenado solo ocurre entre las 21:00 (9 PM) y las 08:00 (8 AM) horas.
     *
     * @param currentHour Hora simulada actual
     * @return true si está dentro del horario de llenado, false en caso contrario
     */
    private boolean shouldRefill(double currentHour) {
        int hour = (int) currentHour % 24;
        // Entre 21:00 (9 PM) y 08:00 (8 AM)
        return hour >= 21 || hour < 8;
    }

    /**
     * Inicia el proceso automático de rellenado nocturno del inventario.
     * El llenado solo ocurre entre las 21:00 (9 PM) y las 08:00 (8 AM) horas simuladas.
     * Solo rellena el faltante del día anterior (lo consumido durante el día).
     *
     * @param clock Reloj simulado del sistema
     * @param week Semana simulada del sistema
     */
    public void startAutomatedRefill(SimulatedClock clock, SimulatedWeek week) {
        if (running) {
            System.out.println("[WARNING] El rellenado automático ya está en ejecución");
            return;
        }

        this.clock = clock;
        this.week = week;
        running = true;

        refillThread = new Thread(() -> {
            System.out.println("[WAREHOUSE] Servicio de rellenado automático iniciado");
            System.out.println("   Horario de operación: 21:00 (9 PM) - 08:00 (8 AM)");

            int lastDay = -1;
            double lastSaveHour = -1;
            double lastRefillHour = -1;

            try {
                while (running && !Thread.currentThread().isInterrupted()) {
                    Thread.sleep(100); // Verificar cada 100ms

                    double currentHour = clock.getCurrentSimulatedTime();
                    int hourOfDay = (int) currentHour % 24;
                    int currentDay = (int) (currentHour / 24);
                    boolean shouldRefillNow = shouldRefill(currentHour);

                    // Detectar cambio de día (cuando pasa de un día a otro)
                    if (lastDay != -1 && currentDay != lastDay) {
                        hasRefilledToday = false; // Resetear flag al cambiar de día
                        lastDay = currentDay;
                    } else if (lastDay == -1) {
                        lastDay = currentDay;
                    }

                    // A las 8:00 AM, solo mostrar la carga de referencia actual
                    // (La carga de referencia se actualiza automáticamente después del rellenado a las 21:00)
                    if (hourOfDay == 8 && !shouldRefillNow) {
                        int dayOfSave = (int)(lastSaveHour / 24);
                        if (lastSaveHour == -1 || currentDay > dayOfSave) {
                            synchronized (refillLock) {
                                // Solo mostrar la carga de referencia, que ya fue establecida después del rellenado anterior
                                System.out.println("\n[WAREHOUSE] Día: " + week.getCurrentDay() +
                                        " - Carga de referencia para el día (8:00 AM): " + String.format("%.2f", loadAtDayStart) + " kg");
                                lastSaveHour = currentHour;
                            }
                        }
                    }

                    // Detectar el inicio exacto del horario de rellenado (21:00) y rellenar solo una vez por día
                    if (shouldRefillNow && hourOfDay == 21 && !hasRefilledToday) {
                        int dayOfLastRefill = lastRefillHour == -1 ? -1 : (int)(lastRefillHour / 24);
                        if (lastRefillHour == -1 || currentDay > dayOfLastRefill) {
                            synchronized (refillLock) {
                                double currentLoad = getCurrentLoad();
                                
                                // Calcular el faltante: lo que había al inicio del día (8:00 AM) menos lo que hay ahora (21:00)
                                // (Esto es lo que se consumió/entregó durante el día de operación)
                                double consumed = loadAtDayStart - currentLoad;
                                
                                if (consumed > 0) {
                                    System.out.println("\n[WAREHOUSE] Día: " + week.getCurrentDay() +
                                            " - Iniciando rellenado de faltante (21:00)");
                                    System.out.println("   - Carga al inicio del día (8:00 AM): " + String.format("%.2f", loadAtDayStart) + " kg");
                                    System.out.println("   - Carga actual (21:00): " + String.format("%.2f", currentLoad) + " kg");
                                    System.out.println("   - Faltante a reponer: " + String.format("%.2f", consumed) + " kg");
                                    
                                    // Solo rellenar el faltante (lo consumido durante el día)
                                    fillRandomly(consumed);
                                    
                                    // Actualizar la carga de referencia con la nueva carga después del rellenado
                                    loadAtDayStart = getCurrentLoad();
                                    
                                    hasRefilledToday = true;
                                    lastRefillHour = currentHour;
                                    
                                    System.out.println("[WAREHOUSE] Rellenado completado. Carga nueva: " + 
                                            String.format("%.2f", getCurrentLoad()) + " kg (esta será la referencia para el día siguiente)");
                                } else if (consumed < 0) {
                                    // Si hay más carga que al inicio (podría pasar si se agregó manualmente)
                                    System.out.println("\n[WAREHOUSE] Día: " + week.getCurrentDay() +
                                            " - No hay faltante. Carga actual (" + String.format("%.2f", currentLoad) + 
                                            " kg) mayor que al inicio del día (" + String.format("%.2f", loadAtDayStart) + " kg).");
                                    hasRefilledToday = true;
                                    lastRefillHour = currentHour;
                                } else {
                                    // No se consumió nada
                                    System.out.println("\n[WAREHOUSE] Día: " + week.getCurrentDay() +
                                            " - No hay faltante. Carga se mantiene igual: " + String.format("%.2f", currentLoad) + " kg");
                                    hasRefilledToday = true;
                                    lastRefillHour = currentHour;
                                }
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("[WAREHOUSE] Servicio de rellenado automático detenido");
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
            System.out.println("[WAREHOUSE] Deteniendo servicio de rellenado automático...");
        }
    }

    /**
     * Establece la carga inicial como referencia para el cálculo de faltantes.
     * Debe llamarse después de llenar el almacén inicialmente.
     */
    public void setInitialLoadReference() {
        synchronized (refillLock) {
            loadAtDayStart = getCurrentLoad();
            System.out.println("[WAREHOUSE] Carga inicial establecida como referencia: " + 
                    String.format("%.2f", loadAtDayStart) + " kg");
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
        System.out.println("\n[INFO] Resumen de inventario por categoría:");

        for (Category category : Category.values()) {
            List<Product> products = inventory.get(category);
            double categoryWeight = products.stream()
                    .mapToDouble(Product::getWeight)
                    .sum();

            System.out.println("   - " + category + ": " + products.size() +
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
        System.out.println("[WAREHOUSE] Capacidad máxima actualizada a: " + maxCapacity + " kg");
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
                    // Primero intentar remover por referencia directa (más eficiente y seguro)
                    boolean removed = categoryProducts.remove(productToRemove);
                    
                    // Si no se encontró por referencia, intentar por comparación
                    if (!removed) {
                        removed = categoryProducts.removeIf(p -> 
                            p == productToRemove || // Comparación por referencia primero
                            (p.getName().equals(productToRemove.getName()) &&
                             Math.abs(p.getWeight() - productToRemove.getWeight()) < 0.01)
                        );
                    }

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