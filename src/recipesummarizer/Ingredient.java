/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recipesummarizer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nadel
 */
public class Ingredient {

    List<Ingredient> ingredients;
    double amount;
    Item item;

    public Ingredient(Item item, double amount) {
        this.item = item;
        this.amount = amount;
        this.ingredients = new ArrayList<>();
    }

    public void addChild(Ingredient ingredient) {
        this.ingredients.add(ingredient);
    }

    @Override
    public String toString() {
        return item.name + " x " + amount;
    }
}
