package pacman.Item;

import ch.aplu.jgamegrid.Location;
import pacman.GameController;
import pacman.PacActor;

/**
 * Represents a pill that Pac-Man can consume.
 *
 * <p>Consuming a pill increases Pac-Man's score and pill count. The
 * collection event is then reported through the game callback.</p>
 */
public class Pill implements Item {
    private static final int SCORE = 1;
    private final Location location;

    /**
     * Creates a pill at the specified location.
     *
     * @param location the pill's location on the game board
     */
    public Pill(Location location) {
        this.location = location;
    }

    /**
     * Returns the type of this item.
     *
     * @return the pill item type
     */
    @Override
    public ItemType getType() {
        return ItemType.PILL;
    }

    @Override
    public int getScore() {
        return SCORE;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    /**
     * Consumes the pill when Pac-Man reaches its location.
     *
     * <p>The visual clearing of the pill is handled by
     * {@code GameController}, which fills the cell with light grey after the
     * pill has been consumed.</p>
     *
     * @param pacman Pac-Man consuming the pill
     * @param gc the game controller managing the current game
     */
    @Override
    public void consume(PacActor pacman, GameController gc) {
        // Award the pill's points to Pac-Man.
        pacman.addScore(this.getScore());
        // Increase Pac-Man's pill count.
        pacman.addPill();
        // The visual clearing of the pill is already handled by GameController filling the cell with lightGray
        // Notify the game that Pac-Man collected a pill.
        gc.getGameCallback().pacManEatPillsAndItems(gc.getCurrentTick(), this.getLocation(), "pills");
    }
}