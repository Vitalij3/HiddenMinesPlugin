package salatosik.hiddenmines.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

@Getter
@AllArgsConstructor
public class LocalizationSection {
    private final String name;
    private final List<String> lore;

    public void applyForItemStack(ItemStack itemStack) {
        itemStack.lore(lore.stream().map(Component::text).toList());
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(Component.text(name));
        itemStack.setItemMeta(itemMeta);
    }
}
