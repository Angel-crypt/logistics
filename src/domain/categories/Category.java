package domain.categories;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Enum Category representa diferentes categorías de productos,
 * cada una con un peso mínimo y máximo, y una lista de nombres de productos asociados.
 * Incluye métodos para obtener nombres y pesos de manera aleatoria.
 */
public enum Category {

    /**
     * Categoría de productos electrónicos con pesos entre 0.2 y 40 kg.
     */
    ELECTRONICS(0.2, 40.0, Arrays.asList(
            "Laptop", "Smartphone", "Tablet", "Auriculares", "Cámara Digital",
            "Monitor", "Teclado", "Mouse", "Impresora", "Router Wi-Fi")),

    /**
     * Electrodomésticos con pesos variables entre 0.5 y 150 kg.
     */
    APPLIANCES(0.5, 150.0, Arrays.asList(
            "Refrigerador", "Lavadora", "Microondas", "Aspiradora", "Horno Eléctrico",
            "Licuadora", "Cafetera", "Plancha de ropa", "Ventilador de pie", "Aire Acondicionado")),

    /**
     * Muebles del hogar u oficina, típicamente pesados.
     */
    FURNITURE(3.0, 200.0, Arrays.asList(
            "Sofá de 3 plazas", "Mesa de comedor", "Silla de madera", "Cama matrimonial", "Armario grande",
            "Estantería", "Escritorio de oficina", "Lámpara de pie", "Cómoda de dormitorio", "Banco de entrada")),

    /**
     * Consolas de videojuegos de distintas marcas.
     */
    CONSOLES(2.0, 6.0, Arrays.asList(
            "PlayStation 5", "Xbox Series X", "Nintendo Switch", "Steam Deck", "PlayStation 4",
            "Xbox Series S", "Nintendo Switch OLED", "PlayStation 5 Digital", "Retro consola mini", "PlayStation Portal")),

    /**
     * Videojuegos físicos con pesos muy ligeros.
     */
    VIDEOGAMES(0.05, 0.2, Arrays.asList(
            "Juego FIFA", "Juego Zelda", "Juego Mario Kart", "Juego God of War", "Juego Call of Duty",
            "Juego Elden Ring", "Juego Minecraft", "Juego Horizon", "Juego Gran Turismo", "Juego Final Fantasy")),

    /**
     * Accesorios gaming como controladores, teclados y sillas.
     */
    GAMING_ACCESSORIES(0.1, 1.5, Arrays.asList(
            "Control Xbox", "Auriculares Gaming", "Teclado Mecánico", "Mouse Gaming", "Silla Gamer",
            "Alfombrilla RGB", "Micrófono USB", "Soporte de auriculares", "Control PS5 DualSense", "Volante de carreras")),

    /**
     * Hardware gamer de alto rendimiento.
     */
    GAMING_HARDWARE(0.5, 10.0, Arrays.asList(
            "Tarjeta Gráfica RTX", "Monitor Gaming", "Fuente de poder modular", "SSD NVMe",
            "Placa base", "Memoria RAM DDR5", "Refrigeración líquida", "Gabinete ATX", "Ventilador RGB", "Procesador Intel i9")),

    /**
     * Artículos de oficina como sillas o impresoras.
     */
    OFFICE(0.1, 5.0, Arrays.asList(
            "Silla de Oficina", "Impresora láser", "Archivador metálico", "Carpeta A4", "Calculadora de escritorio",
            "Organizador de escritorio", "Lámpara LED", "Pizarra blanca", "Teléfono fijo", "Portadocumentos")),

    /**
     * Juguetes de diferentes tipos.
     */
    TOYS(0.1, 5.0, Arrays.asList(
            "Lego Classic", "Muñeca articulada", "Drone infantil", "Pelota antiestrés", "Rompecabezas 1000 piezas",
            "Auto RC", "Juego de mesa Monopoly", "Peluche grande", "Set de plastilina", "Robot educativo")),

    /**
     * Artículos deportivos y de fitness.
     */
    SPORTS(0.2, 50.0, Arrays.asList(
            "Bicicleta de montaña", "Mancuernas", "Balón de fútbol", "Raqueta de tenis", "Cinta de correr",
            "Colchoneta de yoga", "Guantes de boxeo", "Pesas rusas", "Patineta", "Pelota de baloncesto"));

    /** Peso mínimo permitido para esta categoría */
    private final double minWeight;
    /** Peso máximo permitido para esta categoría */
    private final double maxWeight;
    /** Lista de nombres de productos que pertenecen a esta categoría */
    private final List<String> productNames;

    /** Generador aleatorio utilizado internamente para pesos y nombres */
    private static final Random random = new Random();

    /**
     * Constructor del enum Category.
     *
     * @param minWeight peso mínimo de los productos en esta categoría
     * @param maxWeight peso máximo de los productos en esta categoría
     * @param productNames lista de nombres de productos asociados
     */
    Category(double minWeight, double maxWeight, List<String> productNames) {
        this.minWeight = minWeight;
        this.maxWeight = maxWeight;
        this.productNames = productNames;
    }

    /**
     * Obtiene el peso mínimo de la categoría.
     *
     * @return peso mínimo
     */
    public double getMinWeight() {
        return minWeight;
    }

    /**
     * Obtiene el peso máximo de la categoría.
     *
     * @return peso máximo
     */
    public double getMaxWeight() {
        return maxWeight;
    }

    /**
     * Devuelve un nombre de producto al azar desde la lista de la categoría.
     *
     * @return nombre de un producto aleatorio
     */
    public String getRandomName() {
        return productNames.get(random.nextInt(productNames.size()));
    }

    /**
     * Genera un peso aleatorio dentro del rango permitido (minWeight a maxWeight),
     * y lo limita a dos decimales.
     *
     * @return peso aleatorio con dos decimales
     */
    public double getRandomWeight() {
        double rawWeight = minWeight + (maxWeight - minWeight) * random.nextDouble();
        return Math.round(rawWeight * 100.0) / 100.0; // Limita a 2 decimales
    }
}
