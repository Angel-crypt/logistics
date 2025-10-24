package domain.products;

import domain.categories.Category;
import interfaces.Loadable;

public abstract class Product implements Loadable {
    private final String name;
    private final double weight;
    private final Category category;

    protected Product(Category category){
        this.category = category;
        this.name = category.getRandomName();
        this.weight = category.getRandomWeight();
    }

    public String getName() {
        return name;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public double getLoadWeight(){
        return weight;
    }

    public String getSizeClassification(){
        if (weight <= 40.0) return "Small";
        else if (weight <= 150)  return "Medium";
        else return "Large";
    }
}
