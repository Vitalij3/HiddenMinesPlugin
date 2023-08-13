package me.salatosik.hiddenminesplugin.utils.configuration;

import org.bukkit.Material;

import java.util.List;

public enum Values {
    GENERAL_ALLOWED_GROUNDS(
            List.of(
                    Material.SAND,
                    Material.RED_SAND,
                    Material.SOUL_SAND,
                    Material.DIRT,
                    Material.COARSE_DIRT,
                    Material.GRAVEL
            )
    ),

    GENERAL_ALLOWED_PICKUP_INSTRUMENT(
            List.of(
                    Material.WOODEN_SHOVEL,
                    Material.STONE_SHOVEL,
                    Material.IRON_SHOVEL,
                    Material.GOLDEN_SHOVEL,
                    Material.DIAMOND_SHOVEL,
                    Material.NETHERITE_SHOVEL
            )
    );

    public final List<Material> generalAllowedList;

    Values(List<Material> generalAllowedList) {
        this.generalAllowedList = generalAllowedList;
    }

    public boolean verify(List<Material> otherAllowedList) {
        for(Material otherMaterial: otherAllowedList) {
            if(!generalAllowedList.contains(otherMaterial)) {
                return true;
            }
        }
        return false;
    }
}
