package domain.warehouse;

import java.util.*;
import java.util.concurrent.*;
import domain.categories.Category;
import domain.products.Product;
import domain.products.ProductFactory;
import utils.TimeUtils;
import utils.SimulatedClock;

/**
 * Gestiona el inventario concurrente de productos agrupados por categoría.
 * Permite rellenar el inventario aleatoriamente de forma manual o automática.
 *
 * Esta clase es thread-safe y utiliza estructuras concurrentes para manejar
 * operaciones simultáneas sobre el inventario.
 *
 * @author Sistema Logístico
 * @version 1.0
 */
public class Warehouse {

    private final ConcurrentHashMap<Category, List<Product>> inventory;
    private double maxCapacity; // Capacidad máxima en kg
    private final Object refillLock = new Object();
    private Thread refillThread;
    private volatile boolean running = false;

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
     * Inicia el proceso automático de rellenado nocturno del inventario.
     * Utiliza el reloj simulado para ejecutar el rellenado cada 24 horas simuladas.
     *
     * @param clock Reloj simulado del sistema
     */
    public void startNightlyRefill(SimulatedClock clock) {
        if (running) {
            System.out.println("⚠️  El rellenado nocturno ya está en ejecución");
            return;
        }

        running = true;
        refillThread = new Thread(() -> {
            System.out.println("🌙 Servicio de rellenado nocturno iniciado");

            try {
                while (running && !Thread.currentThread().isInterrupted()) {
                    // Esperar 24 horas simuladas
                    TimeUtils.sleepSimulated(24.0);

                    if (!running) break;

                    System.out.println("\n🌙 [Hora simulada: " + clock.getCurrentSimulatedTime() +
                            "] Rellenado nocturno automático");

                    // Calcular stock objetivo (70-90% de la capacidad máxima)
                    double targetStock = maxCapacity * (0.7 + Math.random() * 0.2);
                    fillRandomly(targetStock);
                }
            } catch (InterruptedException e) {
                System.out.println("🌙 Servicio de rellenado nocturno detenido");
                Thread.currentThread().interrupt();
            }
        });

        refillThread.setName("NightlyRefillThread");
        refillThread.setDaemon(true);
        refillThread.start();
    }

    /**
     * Detiene el servicio de rellenado nocturno.
     */
    public void stopNightlyRefill() {
        running = false;
        if (refillThread != null && refillThread.isAlive()) {
            refillThread.interrupt();
            System.out.println("🛑 Deteniendo servicio de rellenado nocturno...");
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
}