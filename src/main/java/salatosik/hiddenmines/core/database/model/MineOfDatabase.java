package salatosik.hiddenmines.core.database.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import salatosik.hiddenmines.HiddenMines;
import salatosik.hiddenmines.core.mine.Mine;
import salatosik.hiddenmines.core.mine.MineType;

@DatabaseTable(tableName = "mines")
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class MineOfDatabase {

    @DatabaseField
    private int x, y, z;

    @DatabaseField
    private String worldName, mineTypeName;

    @Setter(AccessLevel.PRIVATE)
    @DatabaseField(generatedId = true)
    private long id;

    public MineOfDatabase(int x, int y, int z, String worldName, String mineTypeName) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldName = worldName;
        this.mineTypeName = mineTypeName;
    }

    public Mine toMine() {
        World world = null;

        for(World w: Bukkit.getWorlds()) {
            if(w.getEnvironment().name().equals(worldName)) {
                world = w;
                break;
            }
        }

        if(world == null) {
            throw new RuntimeException("World %s is not defined!".formatted(worldName));
        }

        Location location = new Location(world, x, y, z);
        MineType mineType;

        try {
            mineType = MineType.valueOf(mineTypeName);
        } catch(IllegalArgumentException e) {
            throw new RuntimeException("Mine type with name: \"%s\", is not defined!".formatted(mineTypeName));
        }

        return new Mine(location, mineType);
    }
}
