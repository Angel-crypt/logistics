package domain.vehicles;

import domain.categories.Category;
import interfaces.Loadable;
import utils.TimeUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Avión para transporte aéreo de productos.
 * Más rápido que el transporte terrestre pero con restricciones de peso por item.
 * Velocidad de transporte: ~800-900 km/h simulados
 * Restricción: No puede transportar items individuales mayores a 150 kg
 */
public class Airplane extends Vehicle {

    /** Velocidad promedio del avión en km/h simulados */
    private static final double AVERAGE_SPEED_KMH = 850.0;

    /** Peso máximo permitido por item individual (en kg) */
    private static final double MAX_ITEM_WEIGHT = 150.0;

    /** Distancias predefinidas para diferentes destinos (en km) */
    private static final Map<String, Double> DISTANCES = new HashMap<>();

    static {
        // Inicializar distancias comunes (típicamente mayores para aviones)
        DISTANCES.put("Puerto", 300.0);
        DISTANCES.put("Aeropuerto", 0.0); // El avión ya está en el aeropuerto
        DISTANCES.put("Almacén Central", 600.0);
        DISTANCES.put("Sucursal Norte", 1500.0);
        DISTANCES.put("Sucursal Sur", 1800.0);
        DISTANCES.put("Centro de Distribución", 400.0);
        
        // Rutas aéreas desde Guadalajara (GDL)
        DISTANCES.put("SLP", 380.0);  // San Luis Potosí desde GDL (vuelo directo)
        DISTANCES.put("ZAC", 250.0);  // Zacatecas desde GDL (vuelo directo)
        DISTANCES.put("AGS", 260.0);  // Aguascalientes desde GDL (vuelo directo)
    }

    /**
     * Constructor del avión.
     *
     * @param maxCapacity Capacidad máxima de carga en kilogramos (por defecto: 30000 kg para avión de carga)
     * @param initialLocation Ubicación inicial del avión (típicamente un aeropuerto)
     */
    public Airplane(double maxCapacity, String initialLocation) {
        super(maxCapacity, initialLocation);
        System.out.println("[AIRPLANE] Avión inicializado:");
        System.out.println("   - Capacidad máxima: " + maxCapacity + " kg");
        System.out.println("   - Ubicación inicial: " + initialLocation);
        System.out.println("   - Tipo: Transporte aéreo");
        System.out.println("   - Restricción: Items individuales máximo " + MAX_ITEM_WEIGHT + " kg");
    }

    /**
     * Constructor con capacidad por defecto (30000 kg).
     *
     * @param initialLocation Ubicación inicial del avión
     */
    public Airplane(String initialLocation) {
        this(30000.0, initialLocation);
    }

    /**
     * Calcula el tiempo de transporte basado en la distancia al destino.
     * Los aviones son mucho más rápidos que los camiones.
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

        if (distance == 0.0) {
            return 0.1; // Tiempo mínimo si ya está en el destino
        }

        // Tiempo = distancia / velocidad (en horas)
        double travelTime = distance / AVERAGE_SPEED_KMH;
        
        // Agregar tiempo de despegue y aterrizaje (fijo)
        double groundTime = 0.5; // 30 minutos simulados
        
        // El peso afecta menos al avión en vuelo, pero sí afecta el tiempo de carga
        double weightFactor = 1.0 + (currentLoad / maxCapacity) * 0.05; // Solo 5% más lento con carga completa
        
        return (travelTime * weightFactor) + groundTime;
    }

    /**
     * Estima la distancia para destinos no conocidos.
     * Los aviones típicamente viajan distancias mayores.
     *
     * @param destination Nombre del destino
     * @return distancia estimada en km
     */
    private double estimateDistance(String destination) {
        // Usar el hash del string para generar una distancia estimada consistente
        // Los aviones típicamente viajan distancias mayores (500-4000 km)
        int hash = destination.hashCode();
        double estimatedDistance = 500 + (Math.abs(hash) % 3500); // Entre 500-4000 km
        return estimatedDistance;
    }

    /**
     * Retorna el tipo de vehículo.
     *
     * @return "Avión"
     */
    @Override
    protected String getVehicleType() {
        return "Avión";
    }

    /**
     * Los aviones tienen restricciones de peso por item.
     * No pueden transportar items individuales mayores a MAX_ITEM_WEIGHT.
     * Además, ciertas categorías muy pesadas (como FURNITURE grande) pueden no ser adecuadas.
     *
     * @param item Item a verificar
     * @return true si el item puede ser transportado, false en caso contrario
     */
    @Override
    protected boolean canTransportItem(Loadable item) {
        double itemWeight = item.getLoadWeight();
        
        // Verificar límite de peso por item
        if (itemWeight > MAX_ITEM_WEIGHT) {
            return false;
        }

        // Los muebles muy pesados no son ideales para transporte aéreo
        // (aunque técnicamente podrían caber si pesan menos de 150kg)
        Category category = item.getCategory();
        if (category == Category.FURNITURE && itemWeight > 100.0) {
            // Permitir muebles menores a 100kg
            return true;
        }

        // Permitir todos los demás items que cumplan con el peso
        return true;
    }

    /**
     * Los aviones tienen un tiempo de descarga más rápido
     * debido a procesos más eficientes en aeropuertos.
     *
     * @return tiempo de descarga en horas simuladas
     */
    @Override
    protected double calculateUnloadTime() {
        // Tiempo base menor para aviones + tiempo proporcional al peso
        // Los procesos en aeropuertos suelen ser más eficientes
        return 0.15 + (currentLoad / 1000.0) * 0.03; // Mínimo 0.15h, +0.03h por cada 1000kg
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

    /**
     * Obtiene el peso máximo permitido por item individual.
     *
     * @return peso máximo en kilogramos
     */
    public static double getMaxItemWeight() {
        return MAX_ITEM_WEIGHT;
    }
}
