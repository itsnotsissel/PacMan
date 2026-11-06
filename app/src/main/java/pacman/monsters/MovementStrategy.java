package pacman.monsters;

import ch.aplu.jgamegrid.Location;
import pacman.PacManGameGrid;

/**
 * Defines the movement behaviour used by a monster.
 *
 * <p>Concrete strategies determine where a monster should move on the game
 * grid. Optional default methods provide random-seed support, freeze-effect
 * creation, and wall-phasing behaviour.</p>
 */
public interface MovementStrategy {
    /**
     * Determines the monster's next location.
     *
     * @param monster monster whose movement is being calculated
     * @param grid game grid used for movement decisions
     * @return the monster's next location
     */
    Location determineMovement(Monster monster, PacManGameGrid grid);

    /**
     * Sets the random seed used by the movement strategy.
     *
     * <p>Strategies that do not use randomness may leave this method
     * unchanged.</p>
     *
     * @param seed random seed
     */
    // Default method allows Monster to pass seeds without 'instanceof' casting
    default void setSeed(int seed) {
        // By default, do nothing. Troll and Wizard override this.
    }

    default MonsterEffect createFreezeEffect() {
        return new FrozenEffect();
    }

    /**
     * Attempts to move a monster through a wall.
     *
     * <p>Wall phasing is disabled by default. Strategies that support this
     * behaviour can override the method.</p>
     *
     * @param monster monster attempting to phase through a wall
     * @param grid game grid
     * @param direction direction in which the monster is moving
     * @return a phased location, or {@code null} if wall phasing is unavailable
     */
    default Location tryWallPhase(Monster monster, PacManGameGrid grid, Location.CompassDirection direction) {
        return null;
    }
}