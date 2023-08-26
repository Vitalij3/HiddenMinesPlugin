package me.salatosik.hiddenminesplugin.event.listener;

import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.core.database.models.Mine;
import me.salatosik.hiddenminesplugin.core.MineType;
import me.salatosik.hiddenminesplugin.utils.configuration.Configuration;
import me.salatosik.hiddenminesplugin.utils.configuration.mine.ground.Ground;
import me.salatosik.hiddenminesplugin.utils.configuration.mine.hook.Hook;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

// TODO реалізувати адаптацію наземної міни під якийсь тип
public class MineCosmeticListener extends BaseMineListener {
    public MineCosmeticListener(JavaPlugin plugin, Database database, Configuration configuration) {
        super(plugin, database, configuration);
    }

    @Override
    public void onMineAdd(Mine mine) {
        super.onMineAdd(mine);
        spawnMineArmorStand(mine);
    }

    @Override
    public void onMineRemove(Mine mine) {
        super.onMineRemove(mine);
        removeMineArmorStand(mine);
    }

    @Override
    public void onListenerAdded(List<Mine> mines) {
        super.onListenerAdded(mines);
        mines.forEach(this::spawnMineArmorStand);
    }

    @Override
    public void onMineRemoveList(List<Mine> removedMines) {
        super.onMineRemoveList(removedMines);
        removedMines.forEach(this::removeMineArmorStand);
    }

    public static final String MINE_ARMOR_STAND_CUSTOM_NAME = MineCosmeticListener.class.getName();

    private void formatMineArmorStandLocation(Location location, MineType mineType) {
        switch(mineType) {
            case HOOK:
                location.setX(location.getX() + 0.50d);
                location.setZ(location.getZ() + 0.50d);
                location.setY(location.getY() - 0.35d);
                break;

            case GROUND:
                location.setX(location.getX() + 0.50d);
                location.setZ(location.getZ() + 0.50d);
                break;
        }
    }

    private void spawnMineArmorStand(Mine mine) {
        World mineWorld = findWorldByEnvironmentName(mine.worldType);
        if(mineWorld == null) return;

        Location mineLocation = mine.toLocation(mineWorld);
        formatMineArmorStandLocation(mineLocation, mine.mineType);

        if(itIsMineArmorStand(mineLocation)) return;

        ArmorStand mineArmorStand = (ArmorStand) mineWorld.spawnEntity(mineLocation, EntityType.ARMOR_STAND);

        mineArmorStand.setCanMove(false);
        mineArmorStand.setSmall(true);
        mineArmorStand.setVisible(false);
        mineArmorStand.setCustomName(MINE_ARMOR_STAND_CUSTOM_NAME);

        switch(mine.mineType) {
            case GROUND:
                Ground groundConfig = configuration.getMineConfiguration().getGround();
                if(groundConfig.getCosmetic()) {
                    Material cosmeticMaterial = Material.TNT;
                    if(groundConfig.getAdaptiveCosmetic()) cosmeticMaterial = mineLocation.getBlock().getType();
                    mineArmorStand.setItem(EquipmentSlot.HEAD, new ItemStack(cosmeticMaterial, 1));
                }
                break;

            case HOOK:
                Hook hookConfig = configuration.getMineConfiguration().getHook();
                if(hookConfig.getCosmetic()) mineArmorStand.setItem(EquipmentSlot.HEAD, new ItemStack(Material.TNT, 1));
                break;
        }
    }

    private void removeMineArmorStand(Mine mine) {
        World mineWorld = findWorldByEnvironmentName(mine.worldType);
        if(mineWorld == null) return;

        Location mineLocation = mine.toLocation(mineWorld);
        formatMineArmorStandLocation(mineLocation, mine.mineType);

        removeItIsMineArmorStand(mineLocation);
    }

    private Entity findMineArmorStand(Location location) {
        for(Entity entity: location.getWorld().getEntities()) {
            Location entityLocation = entity.getLocation();

            if(entityLocation.equals(location)) {
                String entityCustomName = entity.getCustomName();
                if(entityCustomName == null) return null;
                return entity;
            }
        }

        return null;
    }

    private boolean itIsMineArmorStand(Location location) {
        return findMineArmorStand(location) != null;
    }

    private void removeItIsMineArmorStand(Location location) {
        Entity entity = findMineArmorStand(location);
        if(entity != null) entity.remove();
    }

    private World findWorldByEnvironmentName(World.Environment environment) {
        for(World world: plugin.getServer().getWorlds()) {
            if(world.getEnvironment().name().equals(environment.name())) {
                return world;
            }
        }

        return null;
    }
}
