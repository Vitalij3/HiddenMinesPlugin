package salatosik.hiddenmines.configuration;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PluginConfiguration {
    private final String databaseFilename;

    private final ShapedRecipeSection shapedRecipeSectionGround;
    private final ShapedRecipeSection shapedRecipeSectionHook;

    private final float groundMineExplosionPower;
    private final float hookMineExplosionPower;

    private final LocalizationSection groundMineLocalizationSection;
    private final LocalizationSection hookMineLocalizationSection;

    private final boolean groundMineBreakBlocks;
    private final boolean hookMineBreakBlocks;

    public PluginConfiguration(FileConfiguration cfg) {
        this.databaseFilename = cfg.getString("database.filename");
        this.shapedRecipeSectionGround = getShapedRecipeSection("mine.ground.recipe", cfg);
        this.shapedRecipeSectionHook = getShapedRecipeSection("mine.hook.recipe", cfg);
        this.groundMineExplosionPower = (float) cfg.getDouble("mine.ground.explosionPower");
        this.hookMineExplosionPower = (float) cfg.getDouble("mine.hook.explosionPower");
        this.groundMineLocalizationSection = getLocalizationSection("mine.ground.localization", cfg);
        this.hookMineLocalizationSection = getLocalizationSection("mine.hook.localization", cfg);
        this.groundMineBreakBlocks = cfg.getBoolean("mine.ground.breakBlocks", false);
        this.hookMineBreakBlocks = cfg.getBoolean("mine.hook.breakBlocks", false);
    }

    private static ConfigurationSection getConfigurationSection(String key, FileConfiguration fileConfiguration) {
        ConfigurationSection configurationSection = fileConfiguration.getConfigurationSection(key);

        if(configurationSection == null) {
            throw new RuntimeException("Section with path: \"%s\" is not defined!".formatted(key));
        }

        return configurationSection;
    }

    private static ShapedRecipeSection getShapedRecipeSection(String key, FileConfiguration fileConfiguration) {
        ConfigurationSection recipeSection = getConfigurationSection(key, fileConfiguration);

        List<String> groundMineMaterialsString = recipeSection.getStringList("ingredients");

        if(groundMineMaterialsString.isEmpty()) {
            groundMineMaterialsString.add(Material.STONE_PRESSURE_PLATE.name());
            groundMineMaterialsString.add(Material.TNT.name());
        }

        List<Character> groundMineKeysChar = recipeSection.getCharacterList("keys");

        if(groundMineKeysChar.isEmpty()) {
            groundMineKeysChar.add('X');
            groundMineKeysChar.add('Y');
        }

        if(groundMineKeysChar.size() != groundMineMaterialsString.size()) {
            throw new RuntimeException("Recipe key and material count has not equal!");
        }

        List<ShapedRecipeSection.Ingredient> ingredients = new ArrayList<>();

        for(int i = 0; i < groundMineKeysChar.size(); i++) {
            try {
                ingredients.add(new ShapedRecipeSection.Ingredient(groundMineKeysChar.get(i), Material.valueOf(groundMineMaterialsString.get(i))));
            } catch(IllegalArgumentException e) {
                throw new RuntimeException("Recipe material \"%s\" is not defined!".formatted(groundMineMaterialsString.get(i)));
            }
        }

        return new ShapedRecipeSection(ingredients);
    }

    private static LocalizationSection getLocalizationSection(String key, FileConfiguration fileConfiguration) {
        ConfigurationSection localizationSection = getConfigurationSection(key, fileConfiguration);

        String name = localizationSection.getString("name", "none");
        List<String> lore = localizationSection.getStringList("lore");

        return new LocalizationSection(name, lore);
    }
}
