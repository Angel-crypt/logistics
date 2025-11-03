package services;

import domain.delivery.DeliveryTask;
import domain.delivery.DeliveryReport;
import domain.products.Product;
import domain.vehicles.Vehicle;
import domain.warehouse.Warehouse;
import interfaces.Loadable;
import utils.SimulatedClock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Servicio de gestión de entregas.
 * Coordina la creación, asignación y ejecución de tareas de entrega.
 */
public class DeliveryService {

    /** Lista de tareas de entrega */
    private final List<DeliveryTask> deliveryTasks;

    /** Almacén del que se toman los productos */
    private final Warehouse warehouse;

    /** Lista de vehículos disponibles */
    private final List<Vehicle> availableVehicles;

    /** Reporte de entregas */
    private final DeliveryReport deliveryReport;

    /** Reloj simulado para timestamps */
    private SimulatedClock clock;

    /**
     * Constructor del servicio de entregas.
     *
     * @param warehouse Almacén del que se tomarán los productos
     * @param clock Reloj simulado para timestamps
     */
    public DeliveryService(Warehouse warehouse, SimulatedClock clock) {
        this.warehouse = warehouse;
        this.clock = clock;
        this.deliveryTasks = new CopyOnWriteArrayList<>();
        this.availableVehicles = new CopyOnWriteArrayList<>();
        this.deliveryReport = new DeliveryReport("Reporte de Entregas");
        
        System.out.println("[DELIVERY_SERVICE] Servicio de entregas inicializado");
    }

    /**
     * Crea una nueva tarea de entrega.
     *
     * @param products Productos a entregar
     * @param destination Destino de la entrega
     * @return tarea de entrega creada, o null si no hay suficientes productos
     */
    public DeliveryTask createDeliveryTask(List<Product> products, String destination) {
        if (products == null || products.isEmpty()) {
            System.out.println("[WARNING] No se pueden crear entregas sin productos");
            return null;
        }

        if (destination == null || destination.trim().isEmpty()) {
            System.out.println("[WARNING] Destino no válido");
            return null;
        }

        // Verificar si hay suficientes productos en el almacén
        if (!warehouse.hasEnoughProducts(products)) {
            System.out.println("[WARNING] No hay suficientes productos en el almacén para esta entrega");
            return null;
        }

        double currentTime = clock != null ? clock.getCurrentSimulatedTime() : 0.0;
        DeliveryTask task = new DeliveryTask(products, destination, currentTime);
        deliveryTasks.add(task);

        System.out.println("[DELIVERY_SERVICE] Nueva tarea de entrega creada:");
        System.out.println("   - ID: " + task.getTaskId());
        System.out.println("   - Destino: " + destination);
        System.out.println("   - Productos: " + products.size());
        System.out.println("   - Peso total: " + String.format("%.2f", task.getTotalWeight()) + " kg");

        deliveryReport.addEntryWithTimestamp(
            String.format("Tarea %s creada: %d productos, %.2f kg, destino: %s",
                task.getTaskId(), products.size(), task.getTotalWeight(), destination)
        );

        return task;
    }

    /**
     * Registra un vehículo como disponible para entregas.
     *
     * @param vehicle Vehículo a registrar
     */
    public void registerVehicle(Vehicle vehicle) {
        if (vehicle != null && !availableVehicles.contains(vehicle)) {
            availableVehicles.add(vehicle);
            System.out.println("[DELIVERY_SERVICE] Vehículo registrado: " + vehicle.getClass().getSimpleName());
        }
    }

    /**
     * Remueve un vehículo del servicio.
     *
     * @param vehicle Vehículo a remover
     */
    public void unregisterVehicle(Vehicle vehicle) {
        availableVehicles.remove(vehicle);
    }

    /**
     * Asigna un vehículo a una tarea de entrega y la ejecuta.
     *
     * @param task Tarea de entrega
     * @return true si la asignación y ejecución fue exitosa
     */
    public boolean assignAndExecuteDelivery(DeliveryTask task) {
        if (task == null) {
            return false;
        }

        // Verificar si la tarea ya tiene un vehículo asignado
        Vehicle vehicle = task.getAssignedVehicle();
        
        if (vehicle == null) {
            // Si no hay vehículo asignado, buscar uno disponible
            if (!task.canBeAssigned()) {
                System.out.println("[WARNING] La tarea " + task.getTaskId() + " no puede ser asignada");
                return false;
            }

            vehicle = findAvailableVehicle(task.getTotalWeight());
            if (vehicle == null) {
                System.out.println("[WARNING] No hay vehículos disponibles para la tarea " + task.getTaskId());
                deliveryReport.addEntryWithTimestamp(
                    String.format("Tarea %s: No se pudo asignar vehículo (peso: %.2f kg)",
                        task.getTaskId(), task.getTotalWeight())
                );
                return false;
            }

            // Asignar vehículo
            if (!task.assignVehicle(vehicle)) {
                System.out.println("[WARNING] No se pudo asignar el vehículo a la tarea " + task.getTaskId());
                return false;
            }
        } else {
            // Verificar que el vehículo asignado esté disponible
            if (vehicle.isInTransit()) {
                System.out.println("[WARNING] El vehículo asignado a la tarea " + task.getTaskId() + " está en tránsito");
                return false;
            }
            
            if (vehicle.getAvailableCapacity() < task.getTotalWeight()) {
                System.out.println("[WARNING] El vehículo asignado a la tarea " + task.getTaskId() + " no tiene suficiente capacidad");
                return false;
            }
        }

        System.out.println("[DELIVERY_SERVICE] Ejecutando entrega " + task.getTaskId() + "...");

        // Remover productos del almacén
        List<Product> removedProducts = warehouse.removeProducts(task.getProducts());
        if (removedProducts.size() != task.getProductCount()) {
            System.out.println("[WARNING] No se pudieron remover todos los productos del almacén");
            task.cancel();
            return false;
        }

        // Cargar productos en el vehículo
        task.startLoading();
        List<Loadable> loadableProducts = new ArrayList<>(task.getProducts());
        vehicle.load(loadableProducts);

        // Transportar
        task.startTransit();
        vehicle.transport(task.getDestination());

        // Descargar
        vehicle.unload();

        // Marcar como entregada
        double completedTime = clock != null ? clock.getCurrentSimulatedTime() : 0.0;
        task.markDelivered(completedTime);

        System.out.println("[OK] Entrega " + task.getTaskId() + " completada exitosamente");

        // Agregar al reporte general
        deliveryReport.addEntryWithTimestamp(
            String.format("Tarea %s completada: %d productos entregados en %.2f horas",
                task.getTaskId(), task.getProductCount(), task.getTotalTime())
        );

        return true;
    }

    /**
     * Busca un vehículo disponible que pueda transportar el peso especificado.
     *
     * @param requiredWeight Peso requerido en kilogramos
     * @return vehículo disponible o null si no hay ninguno
     */
    private Vehicle findAvailableVehicle(double requiredWeight) {
        for (Vehicle vehicle : availableVehicles) {
            if (!vehicle.isInTransit() && vehicle.getAvailableCapacity() >= requiredWeight) {
                return vehicle;
            }
        }
        return null;
    }

    /**
     * Obtiene todas las tareas pendientes.
     *
     * @return lista de tareas pendientes
     */
    public List<DeliveryTask> getPendingTasks() {
        return deliveryTasks.stream()
                .filter(task -> task.getStatus() == DeliveryTask.TaskStatus.PENDING)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Obtiene todas las tareas completadas.
     *
     * @return lista de tareas completadas
     */
    public List<DeliveryTask> getCompletedTasks() {
        return deliveryTasks.stream()
                .filter(task -> task.getStatus() == DeliveryTask.TaskStatus.DELIVERED)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Obtiene todas las tareas.
     *
     * @return lista de todas las tareas
     */
    public List<DeliveryTask> getAllTasks() {
        return new ArrayList<>(deliveryTasks);
    }

    /**
     * Genera y guarda un reporte de entregas.
     *
     * @param filePath Ruta del archivo donde guardar el reporte
     */
    public void generateReport(String filePath) {
        deliveryReport.setTitle("Reporte Completo de Entregas");
        
        deliveryReport.addEntry("=== RESUMEN GENERAL ===");
        deliveryReport.addEntry("Total de tareas: " + deliveryTasks.size());
        deliveryReport.addEntry("Tareas completadas: " + getCompletedTasks().size());
        deliveryReport.addEntry("Tareas pendientes: " + getPendingTasks().size());
        deliveryReport.addEntry("");

        deliveryReport.addEntry("=== TAREAS COMPLETADAS ===");
        for (DeliveryTask task : getCompletedTasks()) {
            deliveryReport.addEntry(String.format(
                "Tarea %s: %s - %d productos (%.2f kg) - Tiempo: %.2f horas",
                task.getTaskId(), task.getDestination(), task.getProductCount(),
                task.getTotalWeight(), task.getTotalTime()
            ));
        }

        // Asegurar que el path esté en la carpeta reports
        String finalPath = ensureReportsFolder(filePath);
        deliveryReport.saveEntry(finalPath);
    }

    /**
     * Genera un reporte específico por ciudad y envío.
     *
     * @param city Ciudad de destino
     * @param deliveryNumber Número de envío
     * @param task Tarea de entrega
     */
    public void generateCityDeliveryReport(String city, int deliveryNumber, DeliveryTask task) {
        DeliveryReport cityReport = new DeliveryReport(
            String.format("Reporte de Entrega - %s - Envío #%d", city, deliveryNumber)
        );
        
        cityReport.addEntry("=== INFORMACIÓN DE LA ENTREGA ===");
        cityReport.addEntry("Ciudad: " + city);
        cityReport.addEntry("Número de envío: " + deliveryNumber);
        cityReport.addEntry("ID de tarea: " + task.getTaskId());
        cityReport.addEntry("Fecha de creación: " + String.format("%.2f", task.getCreatedAt()) + " horas simuladas");
        cityReport.addEntry("");
        
        cityReport.addEntry("=== DETALLES DEL ENVÍO ===");
        cityReport.addEntry("Estado: " + task.getStatus());
        cityReport.addEntry("Número de productos: " + task.getProductCount());
        cityReport.addEntry("Peso total: " + String.format("%.2f", task.getTotalWeight()) + " kg");
        
        if (task.getAssignedVehicle() != null) {
            cityReport.addEntry("Vehículo: " + task.getAssignedVehicle().getClass().getSimpleName());
            cityReport.addEntry("Capacidad del vehículo: " + String.format("%.2f", task.getAssignedVehicle().getMaxCapacity()) + " kg");
        }
        
        if (task.getTotalTime() > 0) {
            cityReport.addEntry("Tiempo de entrega: " + String.format("%.2f", task.getTotalTime()) + " horas simuladas");
            cityReport.addEntry("Fecha de finalización: " + String.format("%.2f", task.getCompletedAt()) + " horas simuladas");
        }
        
        cityReport.addEntry("");
        cityReport.addEntry("=== PRODUCTOS ENTREGADOS ===");
        for (Product product : task.getProducts()) {
            cityReport.addEntry(String.format(
                "- %s (%.2f kg) - %s - Tamaño: %s",
                product.getName(), product.getWeight(), product.getCategory(), product.getSizeClassification()
            ));
        }
        
        cityReport.addEntry("");
        cityReport.addEntry("=== RESUMEN POR CATEGORÍA ===");
        var categorySummary = task.getCategorySummary();
        for (var entry : categorySummary.entrySet()) {
            cityReport.addEntry(entry.getKey() + ": " + entry.getValue() + " productos");
        }
        
        // Guardar en carpeta reports con estructura: reports/{ciudad}/envio_{numero}.txt
        String fileName = String.format("reports/%s/envio_%d.txt", city, deliveryNumber);
        cityReport.saveEntry(fileName);
    }

    /**
     * Asegura que la ruta del archivo esté en la carpeta reports.
     *
     * @param filePath Ruta original del archivo
     * @return ruta corregida en la carpeta reports
     */
    private String ensureReportsFolder(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return "reports/reporte_general.txt";
        }
        
        // Si ya está en reports, devolver tal cual
        if (filePath.startsWith("reports/") || filePath.startsWith("./reports/")) {
            return filePath.startsWith("./") ? filePath : filePath;
        }
        
        // Si no tiene extensión, agregar .txt
        if (!filePath.endsWith(".txt")) {
            filePath += ".txt";
        }
        
        // Agregar prefijo reports/
        return "reports/" + filePath;
    }

    /**
     * Obtiene el reporte de entregas.
     *
     * @return reporte de entregas
     */
    public DeliveryReport getDeliveryReport() {
        return deliveryReport;
    }
}
