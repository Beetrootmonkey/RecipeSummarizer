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
import java.util.Arrays;
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

    static boolean onlyPositiveEntries = false;
    static boolean showIntermediates = false;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        onlyPositiveEntries = Arrays.asList(args).contains("-onlypositive");
        showIntermediates = Arrays.asList(args).contains("-showintermediates");

        readFile(args[0]);

        ingredients.forEach(i -> registerRecipeForNode(i));

        ingredients.forEach(i -> completePartialRecipes(i));

        ingredients.forEach(i -> registerNode(i));

        leaves.keySet().stream().filter(key -> onlyPositiveEntries ? leaves.get(key) > 0 : true).sorted((a, b) -> a.name.compareTo(b.name))
                .forEach(key -> System.out.println(String.format("%s: %,.0f%s", key.name, leaves.get(key), key.type.getUnit())));
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
            String[] lineParts = l.split("//");
            String line = lineParts[0];
            Pattern pattern1 = Pattern.compile("([-\\w()]+ *)+");
            Matcher matcher1 = pattern1.matcher(line);
            if (matcher1.find()) {
                String matchingString = matcher1.group();
                int index = l.indexOf(matchingString);
                String whiteSpace = matcher1.replaceAll("").substring(0, index).replaceAll("\t", "    ");
                String name = matchingString.trim();
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

    static void registerNode(Ingredient ingredient) {

        boolean isLeaf = ingredient.ingredients.isEmpty();
        boolean isBranchButShowRegardless = !isLeaf && showIntermediates;

        if (isLeaf || isBranchButShowRegardless) {
            double currentAmount = 0L;
            Item item = isBranchButShowRegardless
                    ? new Item(ingredient.item.type, "(" + ingredient.item.name + ")")
                    : ingredient.item;
            if (leaves.containsKey(item)) {
                currentAmount = leaves.get(item);
            }
            leaves.put(item, currentAmount + ingredient.amount);
        }

        ingredient.ingredients.forEach(i -> registerNode(i));
    }

}
