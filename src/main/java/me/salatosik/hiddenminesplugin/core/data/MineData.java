package me.salatosik.hiddenminesplugin.core.data;

import me.salatosik.hiddenminesplugin.core.data.model.Ingredient;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public enum MineData implements BaseData {
    HOOK(
            ChatColor.DARK_RED + "Hook mine",
            new String[]{ "X", "Y" },
            new Ingredient[]{
                    new Ingredient('Y', Material.STONE_BUTTON),
                    new Ingredient('X', Material.TNT)
            }
    ),

    GROUND(
            ChatColor.DARK_RED + "Ground mine",
            new String[]{ "Y", "X" },
            new Ingredient[]{
                    new Ingredient('Y', Material.STONE_PRESSURE_PLATE),
                    new Ingredient('X', Material.TNT)
            }
    );

    public static final Material GENERAL_OUTPUT_ITEM = Material.TNT;

    private final String nameSpacedKey;
    private final String[] recipe;
    private final String persistentData;
    private final String displayName;
    private final Ingredient[] ingredients;

    MineData(String displayName, String[] recipe, Ingredient[] ingredients) {
        final String universalName = this.getClass().getName() + "-" + this.name();
        this.persistentData = universalName;
        this.nameSpacedKey = universalName;
        this.recipe = recipe;
        this.displayName = displayName;
        this.ingredients = ingredients;
    }

    @Override
    public String[] getRecipe() {
        return recipe;
    }

    @Override
    public Ingredient[] getIngredients() {
        return ingredients;
    }

    private NamespacedKey namespacedKeyInstance = null;

    @Override
    public NamespacedKey getNamespacedKeyInstance(JavaPlugin javaPlugin) {
        if(namespacedKeyInstance == null) {
            namespacedKeyInstance = new NamespacedKey(javaPlugin, getNamespacedKeyString());
            return namespacedKeyInstance;
        }

        return namespacedKeyInstance;
    }

    @Override
    public String getPersistentDataString() {
        return persistentData;
    }

    @Override
    public Material getOutputType() {
        return GENERAL_OUTPUT_ITEM;
    }

    @Override
    public String getNamespacedKeyString() {
        return nameSpacedKey;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }
}
