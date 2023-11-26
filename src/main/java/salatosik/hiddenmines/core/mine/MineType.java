package salatosik.hiddenmines.core.mine;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

@Getter
public enum MineType {
    HOOK("hiddenminespluginhookmine", Material.TNT),
    GROUND("hiddenminesplugingroundmine", Material.TNT);

    private final NamespacedKey namespacedKey;
    private final Material material;

    MineType(String key, Material material) {
        this.namespacedKey = NamespacedKey.fromString(key);
        this.material = material;
    }
}
