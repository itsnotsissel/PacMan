package pacman.Item;

import ch.aplu.jgamegrid.Location;
import pacman.GameController;
import pacman.PacActor;

/**
 * Defines the common behaviour of collectible Pac-Man items.
 *
 * <p>Items provide their type, score value, location, and the behaviour
 * performed when Pac-Man consumes them.</p>
 */
public interface Item {
    ItemType getType();
    int getScore();
    Location getLocation();

    /**
     * Consumes this item and applies its game effects.
     *
     * @param pacman Pac-Man consuming the item
     * @param gc the game controller managing the current game
     */
    void consume(PacActor pacman, GameController gc);
}