package salatosik.hiddenmines.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class ShapedRecipeSection {

    @AllArgsConstructor
    @Getter
    public static class Ingredient {
        private char key;
        private Material material;
    }

    private List<Ingredient> ingredients;

    public ShapedRecipe toShapedRecipe(ItemStack result, NamespacedKey namespacedKey) {
        ShapedRecipe shapedRecipe = new ShapedRecipe(namespacedKey, result);

        List<String> keysList = ingredients.stream().map((i) -> String.valueOf(i.getKey())).toList();
        String[] keysArray = new String[keysList.size()];

        for(int i = 0; i < keysList.size(); i++) {
            keysArray[i] = keysList.get(i);
        }

        shapedRecipe.shape(keysArray);

        for(Ingredient ingredient: ingredients) {
            shapedRecipe.setIngredient(ingredient.getKey(), ingredient.getMaterial());
        }

        return shapedRecipe;
    }
}
