package salatosik.hiddenmines.core.mine;

import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.World;
import salatosik.hiddenmines.core.database.model.MineOfDatabase;

@AllArgsConstructor
@Getter
@ToString
public class Mine {
    private Location location;
    private MineType mineType;

    public MineOfDatabase toMineOfDatabase() {
        return new MineOfDatabase(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getEnvironment().name(), mineType.name());
    }

    public boolean locationEqualsAccuracy(Location location) {
        return this.location.getWorld().equals(location.getWorld()) && this.location.getBlockX() == location.getBlockX() && this.location.getBlockY() == location.getBlockY() && this.location.getBlockZ() == location.getBlockZ();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Mine mine = (Mine) o;
        return locationEqualsAccuracy(mine.getLocation()) && mineType == mine.mineType;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(location, mineType);
    }
}
