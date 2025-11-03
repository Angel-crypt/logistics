package domain.delivery;

import domain.products.Product;
import domain.vehicles.Vehicle;
import domain.categories.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Representa una tarea de entrega que debe ser completada.
 * Incluye productos a entregar, destino, vehículo asignado y estado.
 */
public class DeliveryTask {

    /** Estados posibles de una tarea de entrega */
    public enum TaskStatus {
        PENDING,        // Tarea creada, esperando asignación de vehículo
        ASSIGNED,       // Vehículo asignado, esperando carga
        LOADING,        // Productos siendo cargados en el vehículo
        IN_TRANSIT,     // Vehículo en camino al destino
        DELIVERED,      // Entrega completada
        CANCELLED       // Tarea cancelada
    }

    /** Identificador único de la tarea */
    private final String taskId;

    /** Productos a entregar */
    private final List<Product> products;

    /** Destino de la entrega */
    private final String destination;

    /** Vehículo asignado para la entrega */
    private Vehicle assignedVehicle;

    /** Estado actual de la tarea */
    private TaskStatus status;

    /** Fecha/hora de creación de la tarea */
    private final double createdAt;

    /** Fecha/hora de finalización de la tarea */
    private double completedAt;

    /**
     * Constructor de la tarea de entrega.
     *
     * @param products Lista de productos a entregar
     * @param destination Destino de la entrega
     * @param createdAt Tiempo simulado de creación
     */
    public DeliveryTask(List<Product> products, String destination, double createdAt) {
        this.taskId = UUID.randomUUID().toString().substring(0, 8);
        this.products = new ArrayList<>(products);
        this.destination = destination;
        this.status = TaskStatus.PENDING;
        this.createdAt = createdAt;
        this.completedAt = -1;
        this.assignedVehicle = null;
    }

    /**
     * Calcula el peso total de los productos a entregar.
     *
     * @return peso total en kilogramos
     */
    public double getTotalWeight() {
        return products.stream()
                .mapToDouble(Product::getWeight)
                .sum();
    }

    /**
     * Obtiene la cantidad de productos.
     *
     * @return número de productos
     */
    public int getProductCount() {
        return products.size();
    }

    /**
     * Verifica si la tarea puede ser asignada a un vehículo.
     *
     * @return true si puede ser asignada
     */
    public boolean canBeAssigned() {
        return status == TaskStatus.PENDING;
    }

    /**
     * Asigna un vehículo a la tarea.
     *
     * @param vehicle Vehículo a asignar
     * @return true si la asignación fue exitosa
     */
    public boolean assignVehicle(Vehicle vehicle) {
        if (!canBeAssigned()) {
            return false;
        }
        if (vehicle == null) {
            return false;
        }
        if (vehicle.getAvailableCapacity() < getTotalWeight()) {
            return false;
        }
        this.assignedVehicle = vehicle;
        this.status = TaskStatus.ASSIGNED;
        return true;
    }

    /**
     * Marca la tarea como en proceso de carga.
     */
    public void startLoading() {
        if (status == TaskStatus.ASSIGNED) {
            status = TaskStatus.LOADING;
        }
    }

    /**
     * Marca la tarea como en tránsito.
     */
    public void startTransit() {
        if (status == TaskStatus.LOADING) {
            status = TaskStatus.IN_TRANSIT;
        }
    }

    /**
     * Marca la tarea como entregada.
     *
     * @param completedAt Tiempo simulado de finalización
     */
    public void markDelivered(double completedAt) {
        if (status == TaskStatus.IN_TRANSIT) {
            this.status = TaskStatus.DELIVERED;
            this.completedAt = completedAt;
        }
    }

    /**
     * Cancela la tarea.
     */
    public void cancel() {
        if (status != TaskStatus.DELIVERED && status != TaskStatus.CANCELLED) {
            this.status = TaskStatus.CANCELLED;
        }
    }

    // Getters

    public String getTaskId() {
        return taskId;
    }

    public List<Product> getProducts() {
        return new ArrayList<>(products); // Retorna copia
    }

    public String getDestination() {
        return destination;
    }

    public Vehicle getAssignedVehicle() {
        return assignedVehicle;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public double getCreatedAt() {
        return createdAt;
    }

    public double getCompletedAt() {
        return completedAt;
    }

    /**
     * Obtiene el tiempo total de la tarea (si está completada).
     *
     * @return tiempo en horas, o -1 si no está completada
     */
    public double getTotalTime() {
        if (completedAt > 0) {
            return completedAt - createdAt;
        }
        return -1;
    }

    /**
     * Obtiene un resumen de categorías en la entrega.
     *
     * @return mapa de categoría a cantidad
     */
    public java.util.Map<Category, Integer> getCategorySummary() {
        java.util.Map<Category, Integer> summary = new java.util.HashMap<>();
        for (Product product : products) {
            summary.merge(product.getCategory(), 1, Integer::sum);
        }
        return summary;
    }

    @Override
    public String toString() {
        return String.format("DeliveryTask{id=%s, destination=%s, status=%s, products=%d, weight=%.2f kg}",
                taskId, destination, status, products.size(), getTotalWeight());
    }
}
