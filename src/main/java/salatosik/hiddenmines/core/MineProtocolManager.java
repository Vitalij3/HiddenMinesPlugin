package salatosik.hiddenmines.core;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import salatosik.hiddenmines.core.mine.Mine;
import com.comphenix.protocol.ProtocolManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MineProtocolManager {
    private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    private static final double HEIGHT_OF_GROUND_MINE = -0.1;
    private static final double HEIGHT_OF_HOOK_MINE = -0.3;

    public static void showMine(Mine mine, Player player) {
        final int entityId = mine.hashCode();
        final PacketContainer spawnEntityPacket = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);

        spawnEntityPacket.getIntegers().write(0, entityId); // entity id
        spawnEntityPacket.getUUIDs().write(0, UUID.randomUUID()); // uuid
        spawnEntityPacket.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND); // entity type

        spawnEntityPacket.getDoubles().write(0, mine.getLocation().getBlockX() + 0.50); // X coordinate
        spawnEntityPacket.getDoubles().write(2, mine.getLocation().getBlockZ() + 0.50); // Z coordinate

        // Y coordinate
        switch(mine.getMineType()) {
            case GROUND -> spawnEntityPacket.getDoubles().write(1, mine.getLocation().getBlockY() + HEIGHT_OF_GROUND_MINE);
            case HOOK -> spawnEntityPacket.getDoubles().write(1, mine.getLocation().getBlockY() + HEIGHT_OF_HOOK_MINE);
        }

        final PacketContainer entityMetadataPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        entityMetadataPacket.getIntegers().write(0, entityId);
        final List<WrappedDataValue> wrappedDataValuesOfEntity = new ArrayList<>();
        final WrappedDataWatcher.Serializer byteSerializer = WrappedDataWatcher.Registry.get(Byte.class);
        final WrappedDataWatcher.Serializer booleanSerializer = WrappedDataWatcher.Registry.get(Boolean.class);

        wrappedDataValuesOfEntity.add(new WrappedDataValue(0, byteSerializer, (byte) 0x20)); // is invisible
        wrappedDataValuesOfEntity.add(new WrappedDataValue(5, booleanSerializer, true)); // has no gravity

        byte byteMask = 0x01;
        byteMask |= 0x04;

        wrappedDataValuesOfEntity.add(new WrappedDataValue(15, byteSerializer, byteMask)); // is small
        wrappedDataValuesOfEntity.add(new WrappedDataValue(15, byteSerializer, byteMask)); // has arms

        entityMetadataPacket.getDataValueCollectionModifier().write(0, wrappedDataValuesOfEntity);

        final PacketContainer entityEqPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        entityEqPacket.getIntegers().write(0, entityId);
        entityEqPacket.getSlotStackPairLists().write(0, List.of(new Pair<>(EnumWrappers.ItemSlot.HEAD, new ItemStack(mine.getMineType().getMaterial()))));

        protocolManager.sendServerPacket(player, spawnEntityPacket);
        protocolManager.sendServerPacket(player, entityMetadataPacket);
        protocolManager.sendServerPacket(player, entityEqPacket);
    }

    public static void showMine(Mine mine) {
        mine.getLocation().getWorld().getPlayers().forEach((player) -> showMine(mine, player));
    }

    public static void hideMine(Mine mine, Player player) {
        final PacketContainer destroyEntityPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        destroyEntityPacket.getModifier().write(0, new IntArrayList(new int[] { mine.hashCode() }));
        protocolManager.sendServerPacket(player, destroyEntityPacket);
    }

    public static void hideMine(Mine mine) {
        mine.getLocation().getWorld().getPlayers().forEach((player) -> hideMine(mine, player));
    }
}
