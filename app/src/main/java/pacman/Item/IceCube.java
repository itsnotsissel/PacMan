package pacman.Item;

import ch.aplu.jgamegrid.Actor;
import pacman.GameController;
import pacman.PacActor;

/**
 * Represents an ice cube that Pac-Man can consume.
 *
 * <p>Consuming an ice cube does not increase Pac-Man's score. The ice cube is
 * hidden from the game board, a callback is triggered, and the freeze effect
 * is activated at the ice cube's location.</p>
 */
public class IceCube extends Actor implements Item {
    private static final int SCORE = 0;

    public IceCube() {
        super("sprites/ice.png");
    }

    @Override
    public ItemType getType() {
        return ItemType.ICECUBE;
    }

    @Override
    public int getScore() {
        return SCORE;
    }

    /**
     * Consumes the ice cube when Pac-Man collides with it.
     *
     * <p>The ice cube is hidden, the collection event is reported through the
     * game callback, and the freeze effect is triggered.</p>
     *
     * @param pacman Pac-Man consuming the ice cube
     * @param gc the game controller managing the current game
     */
    @Override
    public void consume(PacActor pacman, GameController gc) {
        // Remove the ice cube from the game board.
        this.hide();
        // Notify the game that Pac-Man collected an ice cube.
        gc.getGameCallback().pacManEatPillsAndItems(gc.getCurrentTick(), this.getLocation(), "ice");
        // Trigger the freeze effect at the ice cube's location.
        gc.triggerFreeze(this.getLocation());
    }
}