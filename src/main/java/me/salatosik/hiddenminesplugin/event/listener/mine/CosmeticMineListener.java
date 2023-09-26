package me.salatosik.hiddenminesplugin.event.listener.mine;

import me.salatosik.hiddenminesplugin.core.data.MineData;
import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.core.database.models.mine.Mine;
import me.salatosik.hiddenminesplugin.utils.configuration.Configuration;
import me.salatosik.hiddenminesplugin.utils.configuration.mine.ground.GroundCfg;
import me.salatosik.hiddenminesplugin.utils.configuration.mine.hook.HookCfg;
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

public class CosmeticMineListener extends BaseMineListener {
    public CosmeticMineListener(JavaPlugin plugin, Database database, Configuration configuration) {
        super(plugin, database, configuration);
    }

    @Override
    public void onItemAdd(Mine mine) {
        super.onItemAdd(mine);
        spawnMineArmorStand(mine);
    }

    @Override
    public void onItemRemove(Mine mine) {
        super.onItemRemove(mine);
        removeMineArmorStand(mine);
    }

    @Override
    public void onListenerAdded(List<Mine> mines) {
        super.onListenerAdded(mines);

        plugin.getServer().getWorlds().forEach((world) -> {
            world.getEntities().forEach((entity) -> {
                String entityCustomName = entity.getCustomName();
                if(entityCustomName == null) return;
                if(!entityCustomName.equals(MINE_ARMOR_STAND_CUSTOM_NAME)) return;

                Location entityLocation = entity.getLocation();

                mines.forEach((mine) -> {
                    Location mineLocation = mine.toLocation(world);

                    if(entityLocation.getBlockX() == mineLocation.getBlockX()
                            & entityLocation.getBlockY() == mineLocation.getBlockY()
                            & entityLocation.getBlockZ() == mineLocation.getBlockZ()) {

                        entity.remove();
                    }
                });
            });
        });

        HookCfg hookCfg = configuration.getMineConfiguration().getHook();
        GroundCfg groundCfgCfg = configuration.getMineConfiguration().getGround();
        if(hookCfg.isAllow() || groundCfgCfg.isAllow()) mines.forEach(this::spawnMineArmorStand);
    }

    @Override
    public void onItemRemoveList(List<Mine> removedMines) {
        super.onItemRemoveList(removedMines);
        removedMines.forEach(this::removeMineArmorStand);
    }

    public static final String MINE_ARMOR_STAND_CUSTOM_NAME = CosmeticMineListener.class.getName();

    private void formatMineArmorStandLocation(Location location, MineData mineType) {
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
        World mineWorld = findWorldByEnvironmentName(mine.getWorldType());
        if(mineWorld == null) return;

        Location mineLocation = mine.toLocation(mineWorld);
        formatMineArmorStandLocation(mineLocation, mine.getMineType());

        if(itIsMineArmorStand(mineLocation)) return;

        ArmorStand mineArmorStand = (ArmorStand) mineWorld.spawnEntity(mineLocation, EntityType.ARMOR_STAND);

        mineArmorStand.setCanMove(false);
        mineArmorStand.setSmall(true);
        mineArmorStand.setVisible(false);
        mineArmorStand.setCustomName(MINE_ARMOR_STAND_CUSTOM_NAME);

        switch(mine.getMineType()) {
            case GROUND:
                GroundCfg groundCfg = configuration.getMineConfiguration().getGround();
                if(groundCfg.getCosmetic()) {
                    Material cosmeticMaterial = Material.TNT;
                    if(groundCfg.getAdaptiveCosmetic()) cosmeticMaterial = mineLocation.getBlock().getType();
                    mineArmorStand.setItem(EquipmentSlot.HEAD, new ItemStack(cosmeticMaterial, 1));
                }
                break;

            case HOOK:
                HookCfg hookCfgConfig = configuration.getMineConfiguration().getHook();
                if(hookCfgConfig.getCosmetic()) mineArmorStand.setItem(EquipmentSlot.HEAD, new ItemStack(Material.TNT, 1));
                break;
        }
    }

    private void removeMineArmorStand(Mine mine) {
        World mineWorld = findWorldByEnvironmentName(mine.getWorldType());
        if(mineWorld == null) return;

        Location mineLocation = mine.toLocation(mineWorld);
        formatMineArmorStandLocation(mineLocation, mine.getMineType());

        removeItIsMineArmorStand(mineLocation);
    }

    private Entity findMineArmorStand(Location location) {
        for(Entity entity: location.getWorld().getEntities()) {
            Location entityLocation = entity.getLocation();

            if(entityLocation.equals(location)) {
                String entityCustomName = entity.getCustomName();
                if(entityCustomName == null) return null;
                if(!entityCustomName.equals(MINE_ARMOR_STAND_CUSTOM_NAME)) return null;
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
