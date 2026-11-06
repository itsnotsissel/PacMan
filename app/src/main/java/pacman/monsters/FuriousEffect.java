package pacman.monsters;

import ch.aplu.jgamegrid.Location;
import pacman.PacManGameGrid;

/**
 * Represents the temporary fury effect applied to a monster.
 *
 * <p>While furious, a monster attempts to move two cells in the direction
 * selected by its normal movement strategy. The effect lasts for a fixed
 * number of game ticks.</p>
 */
public class FuriousEffect implements MonsterEffect {
    private static final int FURY_DURATION_TICKS = 5;
    private int ticksRemaining = FURY_DURATION_TICKS;


    /**
     * Calculates the monster's movement while it is furious.
     *
     * <p>The monster first calculates its normal next location and then
     * attempts to continue one additional cell in the same direction. If the
     * second cell is blocked, the monster moves only to the first cell.</p>
     *
     * @param baseStrategy monster's normal movement strategy
     * @param monster monster affected by fury
     * @param grid game grid used to check walkable cells
     * @param scriptedMove optional scripted movement command
     * @return the monster's next location
     */
    @Override
    public Location modifyMovement(MovementStrategy baseStrategy, Monster monster, PacManGameGrid grid, String scriptedMove) {
        Location current = monster.getLocation();

        // Pass 'false' to computeBaseNextLocation so wall-phasing does not apply during fury jumps
        Location firstStep = (scriptedMove != null)
                ? monster.computeBaseNextLocation(scriptedMove, false)
                : baseStrategy.determineMovement(monster, grid);

        if (firstStep == null || firstStep.equals(current)) {
            return firstStep;
        }

        int dx = firstStep.x - current.x;
        int dy = firstStep.y - current.y;
        Location secondStep = new Location(firstStep.x + dx, firstStep.y + dy);

        return grid.getCell(secondStep) > 0 ? secondStep : firstStep;
    }

    /**
     * Decreases the remaining duration of the fury effect.
     */
    @Override
    public void tick() {
        ticksRemaining--;
    }

    /**
     * Determines whether the fury effect has finished.
     *
     * @return {@code true} when no fury ticks remain
     */
    @Override
    public boolean isExpired() {
        return ticksRemaining <= 0;
    }
}