package test;

import domain.categories.Category;

public class Test {
    private static void testCategory(){
        Category electronic = Category.ELECTRONICS;

        String randomProductName = electronic.getRandomName();
        double randomProductWeight = electronic.getRandomWeight();

        System.out.println("Producto aleatorio: "+randomProductName+" tiene un peso de "+randomProductWeight);
    }

    public static void main(String[] args){
        testCategory();
    }
}
