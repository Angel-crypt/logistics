package domain.vehicles;

import interfaces.Loadable;
import interfaces.Transportable;
import utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase base abstracta para vehículos de transporte.
 * Proporciona funcionalidad común para cargar, descargar y transportar productos.
 */
public abstract class Vehicle implements Transportable {
    
    /** Capacidad máxima de carga en kilogramos */
    protected final double maxCapacity;
    
    /** Carga actual del vehículo */
    protected final List<Loadable> cargo;
    
    /** Peso actual de la carga en kilogramos */
    protected double currentLoad;
    
    /** Ubicación actual del vehículo */
    protected String currentLocation;
    
    /** Destino actual del vehículo */
    protected String destination;
    
    /** Indica si el vehículo está en tránsito */
    protected boolean inTransit;

    /**
     * Constructor del vehículo.
     *
     * @param maxCapacity Capacidad máxima de carga en kilogramos
     * @param initialLocation Ubicación inicial del vehículo
     */
    protected Vehicle(double maxCapacity, String initialLocation) {
        this.maxCapacity = maxCapacity;
        this.cargo = new ArrayList<>();
        this.currentLoad = 0.0;
        this.currentLocation = initialLocation;
        this.destination = null;
        this.inTransit = false;
    }

    /**
     * Carga una lista de items en el vehículo.
     * Solo carga items que no excedan la capacidad máxima.
     *
     * @param items Lista de items a cargar
     */
    @Override
    public void load(List<Loadable> items) {
        if (inTransit) {
            System.out.println("[WARNING] " + getVehicleType() + " no puede cargar mientras está en tránsito");
            return;
        }

        if (items == null || items.isEmpty()) {
            System.out.println("[WARNING] No hay items para cargar");
            return;
        }

        System.out.println("\n[LOAD] Cargando " + items.size() + " items en " + getVehicleType() + "...");
        
        int loadedCount = 0;
        double loadedWeight = 0.0;

        for (Loadable item : items) {
            double itemWeight = item.getLoadWeight();
            
            // Verificar si el item puede ser transportado por este vehículo
            if (!canTransportItem(item)) {
                System.out.println("   [X] Rechazado: " + item.getCategory() + " (no permitido para este tipo de vehículo)");
                continue;
            }

            if (currentLoad + itemWeight <= maxCapacity) {
                cargo.add(item);
                currentLoad += itemWeight;
                loadedWeight += itemWeight;
                loadedCount++;
                
                System.out.println("   [+] Cargado: " + item.getCategory() + 
                        " (" + String.format("%.2f", itemWeight) + " kg)");
            } else {
                System.out.println("   [X] Rechazado: excede capacidad. Peso: " + 
                        String.format("%.2f", itemWeight) + " kg, Capacidad disponible: " + 
                        String.format("%.2f", maxCapacity - currentLoad) + " kg");
            }
        }

        System.out.println("[OK] Carga completada:");
        System.out.println("   - Items cargados: " + loadedCount + "/" + items.size());
        System.out.println("   - Peso cargado: " + String.format("%.2f", loadedWeight) + " kg");
        System.out.println("   - Carga total: " + String.format("%.2f", currentLoad) + "/" + maxCapacity + " kg");
    }

    /**
     * Descarga todos los items del vehículo.
     */
    @Override
    public void unload() {
        if (inTransit) {
            System.out.println("[WARNING] " + getVehicleType() + " no puede descargar mientras está en tránsito");
            return;
        }

        if (cargo.isEmpty()) {
            System.out.println("[WARNING] " + getVehicleType() + " no tiene carga para descargar");
            return;
        }

        System.out.println("\n[UNLOAD] Descargando " + cargo.size() + " items de " + getVehicleType() + "...");
        
        double unloadedWeight = currentLoad;
        int itemCount = cargo.size();

        // Simular tiempo de descarga
        try {
            double unloadTime = calculateUnloadTime();
            TimeUtils.sleepSimulated(unloadTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        cargo.clear();
        currentLoad = 0.0;

        System.out.println("[OK] Descarga completada:");
        System.out.println("   - Items descargados: " + itemCount);
        System.out.println("   - Peso descargado: " + String.format("%.2f", unloadedWeight) + " kg");
    }

    /**
     * Transporta la carga desde la ubicación actual hasta el destino especificado.
     *
     * @param destination Destino del transporte
     */
    @Override
    public void transport(String destination) {
        if (cargo.isEmpty()) {
            System.out.println("[WARNING] " + getVehicleType() + " no tiene carga para transportar");
            return;
        }

        if (inTransit) {
            System.out.println("[WARNING] " + getVehicleType() + " ya está en tránsito hacia " + this.destination);
            return;
        }

        if (destination == null || destination.trim().isEmpty()) {
            System.out.println("[WARNING] Destino no válido");
            return;
        }

        this.destination = destination;
        this.inTransit = true;

        System.out.println("\n[TRANSPORT] " + getVehicleType() + " iniciando transporte:");
        System.out.println("   - Desde: " + currentLocation);
        System.out.println("   - Hacia: " + destination);
        System.out.println("   - Carga: " + cargo.size() + " items (" + 
                String.format("%.2f", currentLoad) + " kg)");

        try {
            // Simular tiempo de transporte
            double transportTime = calculateTransportTime(destination);
            System.out.println("   - Tiempo estimado: " + String.format("%.2f", transportTime) + " horas simuladas");
            TimeUtils.sleepSimulated(transportTime);

            // Llegada al destino
            currentLocation = destination;
            this.inTransit = false;
            this.destination = null;

            System.out.println("[OK] " + getVehicleType() + " ha llegado a " + destination);
            System.out.println("   - Carga entregada: " + cargo.size() + " items");

        } catch (InterruptedException e) {
            System.out.println("[WARNING] Transporte interrumpido");
            Thread.currentThread().interrupt();
            this.inTransit = false;
        }
    }

    /**
     * Determina si un item específico puede ser transportado por este vehículo.
     * Las subclases pueden sobrescribir este método para implementar restricciones.
     *
     * @param item Item a verificar
     * @return true si el item puede ser transportado, false en caso contrario
     */
    protected boolean canTransportItem(Loadable item) {
        return true; // Por defecto, todos los items pueden ser transportados
    }

    /**
     * Calcula el tiempo de descarga basado en la carga actual.
     * Las subclases pueden sobrescribir este método.
     *
     * @return tiempo de descarga en horas simuladas
     */
    protected double calculateUnloadTime() {
        // Tiempo base + tiempo proporcional al peso
        return 0.1 + (currentLoad / 1000.0) * 0.05; // Mínimo 0.1h, +0.05h por cada 1000kg
    }

    /**
     * Calcula el tiempo de transporte basado en la distancia/destino.
     * Las subclases deben implementar este método.
     *
     * @param destination Destino del transporte
     * @return tiempo de transporte en horas simuladas
     */
    protected abstract double calculateTransportTime(String destination);

    /**
     * Retorna el tipo de vehículo (para mensajes).
     * Las subclases deben implementar este método.
     *
     * @return nombre del tipo de vehículo
     */
    protected abstract String getVehicleType();

    // Getters

    public double getMaxCapacity() {
        return maxCapacity;
    }

    public double getCurrentLoad() {
        return currentLoad;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public String getDestination() {
        return destination;
    }

    public boolean isInTransit() {
        return inTransit;
    }

    public List<Loadable> getCargo() {
        return new ArrayList<>(cargo); // Retorna copia para evitar modificaciones externas
    }

    public double getAvailableCapacity() {
        return maxCapacity - currentLoad;
    }

    public boolean isEmpty() {
        return cargo.isEmpty();
    }

    public int getCargoCount() {
        return cargo.size();
    }

    /**
     * Establece la ubicación actual del vehículo.
     * Útil para reinicializar la posición después de entregas.
     *
     * @param location Nueva ubicación
     */
    public void setCurrentLocation(String location) {
        if (location != null) {
            this.currentLocation = location;
            // Si se establece una nueva ubicación, el vehículo ya no está en tránsito
            if (inTransit) {
                this.inTransit = false;
                this.destination = null;
            }
        }
    }
}
