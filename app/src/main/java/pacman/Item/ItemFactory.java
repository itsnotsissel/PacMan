package pacman.Item;

import ch.aplu.jgamegrid.Location;

public class ItemFactory {
    private ItemFactory() {
        // Prevent instantiation
    }

    /**
    Creates each item by getting the type of the item: Pill, GoldPiece, IceCube,
    and cell location in the map
     */
    public static Item createItem(ItemType type, Location location) {
        switch (type) {
            case PILL:
                return new Pill(location);
            case GOLDPIECE:
                return new GoldPiece();
            case ICECUBE:
                return new IceCube();
            default:
                throw new IllegalArgumentException("Unknown item type: " + type);
        }
    }
}