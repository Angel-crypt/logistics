package test;

import domain.categories.Category;

public class testCategory {
    public static void run() {
        Category electronic = Category.ELECTRONICS;

        String randomProductName = electronic.getRandomName();
        double randomProductWeight = electronic.getRandomWeight();

        System.out.println("Producto aleatorio: "+randomProductName+" tiene un peso de "+randomProductWeight);
    }
}
