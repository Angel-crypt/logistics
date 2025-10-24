package domain.products;

import domain.categories.Category;

import java.util.ArrayList;
import java.util.List;

public class ProductFactory {
    public static Product createdRandmon(Category category){
        return new Product(category) {};
    }

    public static List<Product> createBatch(Category category, int count){
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < count; i++){
            products.add(createdRandmon(category));
        }
        return products;
    }
}
