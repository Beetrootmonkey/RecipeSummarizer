/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recipesummarizer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static recipesummarizer.Item.Type.FLUID;
import static recipesummarizer.Item.Type.ITEM;

/**
 *
 * @author nadel
 */
public class RecipeSummarizer {

    static Map<Item, Double> leaves = new HashMap<>();

    static List<Ingredient> ingredients = new ArrayList<>();

    static Map<Item, List<Ingredient>> recipes = new HashMap<>();
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        boolean onlyPositiveEntries = args.length > 1 ? "-onlypositive".equals(args[1]) : false;
        
        readFile(args[0]);

        ingredients.forEach(i -> registerRecipeForNode(i));

        ingredients.forEach(i -> completePartialRecipes(i));

        ingredients.forEach(i -> registerNodeIfLeaf(i));

        leaves.keySet().stream().filter(key -> onlyPositiveEntries ? leaves.get(key) > 0 : true).sorted((a, b) -> a.name.compareTo(b.name))
                .forEach(key -> System.out.println(String.format("%s: %,.0f %s", key.name, leaves.get(key), key.type.getUnit())));
    }

    static void completePartialRecipes(Ingredient ingredient) {
        if (ingredient.ingredients.isEmpty()) {
            if (recipes.containsKey(ingredient.item)) {
                ingredient.ingredients = recipes.get(ingredient.item).stream()
                        .map(i -> new Ingredient(i.item, i.amount * ingredient.amount))
                        .collect(Collectors.toList());
            }
        }
        ingredient.ingredients.forEach(i -> completePartialRecipes(i));
    }

    static void registerRecipeForNode(Ingredient ingredient) {
        if (!ingredient.ingredients.isEmpty()) {
            double divider = 1 / ingredient.amount;
            recipes.put(ingredient.item, ingredient.ingredients.stream()
                    .map(i -> new Ingredient(i.item, i.amount * divider))
                    .collect(Collectors.toList()));
            ingredient.ingredients.forEach(i -> registerRecipeForNode(i));
        }
    }

    static Ingredient getIngredientFromString(String text) {
        String[] parts = text.split(" ");
        String number = parts[0];
        List<String> fromParts = new ArrayList<>();
        for (int j = 1; j < parts.length; j++) {
            fromParts.add(parts[j]);
        }
        String name = fromParts.stream().reduce("", (a, b) -> a + (a.length() > 0 ? " " : "") + b);
        boolean isFluid = number.endsWith("mb");
        long amount = 0;
        try {
            amount = Long.parseLong(number.substring(0, number.length() - (isFluid ? 2 : 0)));
        } catch (NumberFormatException e) {
            return null;
        }
        Item item = new Item(isFluid ? FLUID : ITEM, name);
        return new Ingredient(item, amount);
    }

    static void readFile(String fileName) throws IOException {
        Path path = Paths.get(fileName);
        byte[] bytes = Files.readAllBytes(path);
        List<String> allLines = Files.readAllLines(path, StandardCharsets.UTF_8);

        int currentPadding = 0;
        Map<Integer, Ingredient> currentParent = new HashMap<>();
        for (int i = 0; i < allLines.size(); i++) {
            String l = allLines.get(i);
            Pattern pattern1 = Pattern.compile("(-*\\w+ *)+");
            Matcher matcher1 = pattern1.matcher(l);
            if (matcher1.find()) {
                String match = matcher1.group();
                String[] nameParts = match.split(" //");
                String name = nameParts[0];
                String whiteSpace = matcher1.replaceAll("").replaceAll("\t", "    ");
                int padding = (int) (whiteSpace.length() * 0.25);
                Ingredient ingredient = getIngredientFromString(name);
                if (ingredient == null) {
                    continue;
                }

                Ingredient parent = currentParent.get(padding - 1);
                if (parent == null) {
                    ingredients.add(ingredient);
                } else {
                    parent.addChild(ingredient);
                }
                currentParent.put(padding, ingredient);
            }
        }
    }

    static void registerNodeIfLeaf(Ingredient ingredient) {
        if (ingredient.ingredients.isEmpty()) {
            double currentAmount = 0L;
            if (leaves.containsKey(ingredient.item)) {
                currentAmount = leaves.get(ingredient.item);
            }
            leaves.put(ingredient.item, currentAmount + ingredient.amount);
        } else {
            ingredient.ingredients.forEach(i -> registerNodeIfLeaf(i));
        }
    }

}
