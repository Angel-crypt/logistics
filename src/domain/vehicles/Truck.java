package domain.vehicles;

import interfaces.Loadable;
import utils.TimeUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Camión para transporte terrestre de productos.
 * Capaz de transportar cargas pesadas y grandes volúmenes.
 * Velocidad de transporte: ~60-80 km/h simulados
 */
public class Truck extends Vehicle {

    /** Velocidad promedio del camión en km/h simulados */
    private static final double AVERAGE_SPEED_KMH = 70.0;

    /** Distancias predefinidas para diferentes destinos (en km) */
    private static final Map<String, Double> DISTANCES = new HashMap<>();

    static {
        // Inicializar distancias comunes
        DISTANCES.put("Ciudad A", 150.0);
        DISTANCES.put("Ciudad B", 300.0);
        DISTANCES.put("Ciudad C", 450.0);
        DISTANCES.put("Ciudad D", 200.0);
        DISTANCES.put("Puerto", 100.0);
        DISTANCES.put("Aeropuerto", 50.0);
        DISTANCES.put("Almacén Central", 250.0);
        DISTANCES.put("Sucursal Norte", 180.0);
        DISTANCES.put("Sucursal Sur", 220.0);
        DISTANCES.put("Centro de Distribución", 120.0);
    }

    /**
     * Constructor del camión.
     *
     * @param maxCapacity Capacidad máxima de carga en kilogramos (por defecto: 20000 kg para camión grande)
     * @param initialLocation Ubicación inicial del camión
     */
    public Truck(double maxCapacity, String initialLocation) {
        super(maxCapacity, initialLocation);
        System.out.println("[TRUCK] Camión inicializado:");
        System.out.println("   • Capacidad máxima: " + maxCapacity + " kg");
        System.out.println("   • Ubicación inicial: " + initialLocation);
        System.out.println("   • Tipo: Transporte terrestre");
    }

    /**
     * Constructor con capacidad por defecto (20000 kg).
     *
     * @param initialLocation Ubicación inicial del camión
     */
    public Truck(String initialLocation) {
        this(20000.0, initialLocation);
    }

    /**
     * Calcula el tiempo de transporte basado en la distancia al destino.
     * Para destinos no registrados, estima basándose en el nombre.
     *
     * @param destination Destino del transporte
     * @return tiempo de transporte en horas simuladas
     */
    @Override
    protected double calculateTransportTime(String destination) {
        Double distance = DISTANCES.get(destination);
        
        if (distance == null) {
            // Estimar distancia basándose en el hash del nombre (para destinos desconocidos)
            distance = estimateDistance(destination);
        }

        // Tiempo = distancia / velocidad (en horas)
        double travelTime = distance / AVERAGE_SPEED_KMH;
        
        // Agregar tiempo adicional basado en el peso de la carga (más peso = más lento)
        double weightFactor = 1.0 + (currentLoad / maxCapacity) * 0.2; // Hasta 20% más lento con carga completa
        
        return travelTime * weightFactor;
    }

    /**
     * Estima la distancia para destinos no conocidos.
     *
     * @param destination Nombre del destino
     * @return distancia estimada en km
     */
    private double estimateDistance(String destination) {
        // Usar el hash del string para generar una distancia estimada consistente
        int hash = destination.hashCode();
        double estimatedDistance = 100 + (Math.abs(hash) % 400); // Entre 100-500 km
        return estimatedDistance;
    }

    /**
     * Retorna el tipo de vehículo.
     *
     * @return "Camión"
     */
    @Override
    protected String getVehicleType() {
        return "Camión";
    }

    /**
     * Los camiones pueden transportar cualquier tipo de producto,
     * incluyendo items muy pesados como muebles y electrodomésticos.
     *
     * @param item Item a verificar
     * @return true siempre (los camiones no tienen restricciones)
     */
    @Override
    protected boolean canTransportItem(Loadable item) {
        return true; // Los camiones pueden transportar todo
    }

    /**
     * Los camiones tienen un tiempo de descarga ligeramente más largo
     * debido a que pueden cargar items muy pesados.
     *
     * @return tiempo de descarga en horas simuladas
     */
    @Override
    protected double calculateUnloadTime() {
        // Tiempo base mayor para camiones + tiempo proporcional al peso
        return 0.2 + (currentLoad / 1000.0) * 0.08; // Mínimo 0.2h, +0.08h por cada 1000kg
    }

    /**
     * Agrega una nueva ruta con su distancia.
     *
     * @param destination Nombre del destino
     * @param distanceKm Distancia en kilómetros
     */
    public static void addRoute(String destination, double distanceKm) {
        DISTANCES.put(destination, distanceKm);
    }

    /**
     * Obtiene la distancia a un destino específico.
     *
     * @param destination Nombre del destino
     * @return distancia en km, o null si no está registrada
     */
    public static Double getDistance(String destination) {
        return DISTANCES.get(destination);
    }
}
