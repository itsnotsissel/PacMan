package pacman.Configurations;

import ch.aplu.jgamegrid.Location;
import pacman.Item.ItemType;

public class ItemConfiguration {
    public final ItemType type;
    public final Location location;

    public ItemConfiguration(ItemType type, Location location) {
        this.type = type;
        this.location = location;
    }

    public ItemType getType() {
        return type;
    }

    public Location getLocation() {
        return location;
    }

}