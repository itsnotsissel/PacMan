package pacman.Item;

import ch.aplu.jgamegrid.Actor;
import pacman.GameController;
import pacman.PacActor;

/**
 * Represents a gold piece that Pac-Man can collect.
 *
 * <p>A gold piece awards Pac-Man five points, increases the pill count,
 * hides itself from the game board, notifies the game callback, and triggers
 * the fury effect.</p>
 */
public class GoldPiece extends Actor implements Item {
    private static final int SCORE = 5;

    public GoldPiece() {
        super("sprites/gold.png");
    }

    /**
     * Returns the type of this item.
     *
     * @return the gold piece item type
     */
    @Override
    public ItemType getType() {
        return ItemType.GOLDPIECE;
    }

    @Override
    public int getScore() {
        return SCORE;
    }

    /**
     * Consumes the gold piece when Pac-Man collides with it.
     *
     * <p>When consumed, the gold piece increases Pac-Man's score and pill
     * count, hides itself, notifies the game callback, and triggers the fury
     * effect at its current location.</p>
     *
     * @param pacman Pac-Man consuming the gold piece
     * @param gc the game controller managing the current game
     */
    @Override
    public void consume(PacActor pacman, GameController gc) {
        // Award the gold piece's score to Pac-Man.
        pacman.addScore(this.getScore());
        // Preserve the existing game behaviour for collected items.
        pacman.addPill();
        // Remove the gold piece from view.
        this.hide();
        // Notify the game that Pac-Man collected a gold piece.
        gc.getGameCallback().pacManEatPillsAndItems(gc.getCurrentTick(), this.getLocation(), "gold");
        // Trigger the fury effect at the gold piece's location.
        gc.triggerFury(this.getLocation());
    }
}