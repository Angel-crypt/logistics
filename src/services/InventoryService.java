package services;

import domain.categories.Category;
import domain.products.Product;
import domain.warehouse.Warehouse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servicio de gestión de inventario.
 * Proporciona operaciones de alto nivel para gestionar productos en el almacén.
 */
public class InventoryService {

    /** Almacén asociado al servicio */
    private final Warehouse warehouse;

    /**
     * Constructor del servicio de inventario.
     *
     * @param warehouse Almacén a gestionar
     */
    public InventoryService(Warehouse warehouse) {
        this.warehouse = warehouse;
        System.out.println("[INVENTORY_SERVICE] Servicio de inventario inicializado");
    }

    /**
     * Obtiene productos del inventario por categoría.
     *
     * @param category Categoría de productos
     * @param count Cantidad de productos a obtener
     * @return Lista de productos
     */
    public List<Product> getProductsByCategory(Category category, int count) {
        return warehouse.getProductsByCategory(category, count);
    }

    /**
     * Obtiene todos los productos de una categoría.
     *
     * @param category Categoría de productos
     * @return Lista de productos
     */
    public List<Product> getAllProductsByCategory(Category category) {
        return warehouse.getAllProductsByCategory(category);
    }

    /**
     * Obtiene productos aleatorios del inventario para una entrega.
     *
     * @param totalWeight Peso total deseado en kilogramos
     * @param maxProducts Cantidad máxima de productos
     * @return Lista de productos seleccionados
     */
    public List<Product> selectProductsForDelivery(double totalWeight, int maxProducts) {
        List<Product> selectedProducts = new ArrayList<>();
        double currentWeight = 0.0;
        
        Map<Category, List<Product>> inventory = warehouse.getInventory();
        List<Category> categories = new ArrayList<>(inventory.keySet());

        while (selectedProducts.size() < maxProducts && currentWeight < totalWeight) {
            // Seleccionar categoría aleatoria
            Category randomCategory = categories.get((int)(Math.random() * categories.size()));
            List<Product> categoryProducts = inventory.get(randomCategory);

            if (categoryProducts != null && !categoryProducts.isEmpty()) {
                Product product = categoryProducts.get(0);
                
                if (currentWeight + product.getWeight() <= totalWeight) {
                    selectedProducts.add(product);
                    currentWeight += product.getWeight();
                } else {
                    break; // No cabe más productos
                }
            }
        }

        return selectedProducts;
    }

    /**
     * Verifica la disponibilidad de productos en el inventario.
     *
     * @param requiredProducts Productos requeridos
     * @return true si hay suficientes productos disponibles
     */
    public boolean checkAvailability(List<Product> requiredProducts) {
        return warehouse.hasEnoughProducts(requiredProducts);
    }

    /**
     * Obtiene un resumen del inventario por categoría.
     *
     * @return Mapa de categoría a cantidad de productos
     */
    public Map<Category, Integer> getInventorySummary() {
        Map<Category, List<Product>> inventory = warehouse.getInventory();
        Map<Category, Integer> summary = new java.util.HashMap<>();
        
        for (Map.Entry<Category, List<Product>> entry : inventory.entrySet()) {
            summary.put(entry.getKey(), entry.getValue().size());
        }
        
        return summary;
    }

    /**
     * Obtiene el peso total del inventario por categoría.
     *
     * @return Mapa de categoría a peso total
     */
    public Map<Category, Double> getWeightByCategory() {
        Map<Category, List<Product>> inventory = warehouse.getInventory();
        Map<Category, Double> weightSummary = new java.util.HashMap<>();
        
        for (Map.Entry<Category, List<Product>> entry : inventory.entrySet()) {
            double totalWeight = entry.getValue().stream()
                    .mapToDouble(Product::getWeight)
                    .sum();
            weightSummary.put(entry.getKey(), totalWeight);
        }
        
        return weightSummary;
    }

    /**
     * Obtiene el almacén asociado.
     *
     * @return almacén
     */
    public Warehouse getWarehouse() {
        return warehouse;
    }
}
