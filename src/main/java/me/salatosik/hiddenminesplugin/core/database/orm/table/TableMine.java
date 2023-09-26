package me.salatosik.hiddenminesplugin.core.database.orm.table;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import me.salatosik.hiddenminesplugin.core.data.MineData;
import me.salatosik.hiddenminesplugin.core.database.models.mine.Mine;
import org.bukkit.World;

@DatabaseTable(tableName = "mines")
public class TableMine {
    @DatabaseField(canBeNull = false)
    private int x = 0;

    @DatabaseField(canBeNull = false)
    private int y = 0;

    @DatabaseField(canBeNull = false)
    private int z = 0;

    @DatabaseField(canBeNull = false, columnName = "mineType")
    private String mineTypeString = "";

    @DatabaseField(canBeNull = false, columnName = "worldType")
    private String worldTypeString = "";

    @DatabaseField(generatedId = true)
    private long id;

    public TableMine(int x, int y, int z, MineData mineData, World.Environment worldType) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.mineTypeString = mineData.name();
        this.worldTypeString = worldType.name();
    }

    public TableMine(int x, int y, int z, String mineTypeString, String worldTypeString) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.mineTypeString = mineTypeString;
        this.worldTypeString = worldTypeString;
    }

    public TableMine() {}

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public World.Environment getWorldType() {
        for(World.Environment environment: World.Environment.values()) {
            if(environment.name().equals(worldTypeString)) {
                return environment;
            }
        }

        return null;
    }

    public MineData getMineType() {
        for(MineData mineData: MineData.values()) {
            if(mineData.name().equals(mineTypeString)) {
                return mineData;
            }
        }

        return null;
    }

    public long getId() {
        return id;
    }

    public Mine toMine() {
        return new Mine(x, y, z, getMineType(), getWorldType());
    }

    public boolean equalsMine(Mine mine) {
        return mine.getX() == x & mine.getY() == y & mine.getZ() == z & mine.getMineType() == getMineType() & mine.getWorldType() == getWorldType();
    }
}
