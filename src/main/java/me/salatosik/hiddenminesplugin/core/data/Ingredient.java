package me.salatosik.hiddenminesplugin.core.data;

import org.bukkit.Material;

public class Ingredient {
    public final char key;
    public final Material item;

    public Ingredient(char key, Material item) {
        this.key = key;
        this.item = item;
    }
}
