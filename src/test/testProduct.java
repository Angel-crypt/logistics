package test;

import domain.categories.Category;
import domain.products.Product;
import domain.products.ProductFactory;

import java.util.List;

public class testProduct {
    public static void run(){
        Product p = ProductFactory.createdRandmon(Category.ELECTRONICS);
        System.out.println("Producto: " + p.getName() + ", Peso: " + p.getWeight() + ", Categoría: " + p.getCategory() + ", Tamaño: " + p.getSizeClassification());

        List<Product> batch = ProductFactory.createBatch(Category.APPLIANCES, 5);
        System.out.println("=".repeat(5)+"Lote de productos"+"=".repeat(5));
        batch.forEach(pr -> System.out.println(pr.getName() + " - " + pr.getWeight()));
        System.out.println("=".repeat(20));
    }
}
