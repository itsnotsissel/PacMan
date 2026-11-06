package pacman.monsters;

import ch.aplu.jgamegrid.Location;
import pacman.PacManGameGrid;

/**
 * Represents the unique ice effect applied to the Wizard monster.
 * Due to its magical abilities, the Wizard is immune to a complete freeze
 * and instead moves at a reduced speed by alternating between moving and remaining stationary.
 */
public class WizardIceEffect extends IceEffect {
    private static final int FREEZE_DURATION_TICKS = 3;
    private boolean moveInThisTick = true;

    /**
     * Constructs a new WizardIceEffect with a predefined freeze duration.
     */
    public WizardIceEffect() {
        super(FREEZE_DURATION_TICKS);
    }

    /**
     * Modifies the movement behavior of the Wizard during the freeze effect,
     * alternating each tick between making an unscripted movement and staying in place.
     *
     * @param baseStrategy the monster's base movement strategy
     * @param monster      the monster instance experiencing the effect
     * @param grid         the current game grid layout
     * @param scriptedMove the scripted move for this tick (which is burned during freeze)
     * @return the resulting Location after applying the reduced-speed ice effect
     */
    @Override
    public Location modifyMovement(MovementStrategy baseStrategy, Monster monster, PacManGameGrid grid, String scriptedMove) {
        if (moveInThisTick) {
            moveInThisTick = false;
            // Freeze burns the script without applying it.
            // The Wizard must ALWAYS use its unscripted random walk during freeze to break out of "S" holds.
            return baseStrategy.determineMovement(monster, grid);
        } else {
            moveInThisTick = true;
            return monster.getLocation();
        }
    }
}