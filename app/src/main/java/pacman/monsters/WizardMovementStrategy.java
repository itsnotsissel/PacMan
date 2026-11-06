package pacman.monsters;

import ch.aplu.jgamegrid.Location;
import pacman.PacManGameGrid;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Implements the wall-through movement strategy for the Wizard monster.
 * The Wizard can navigate 8 surrounding neighbor cells and has the unique ability
 * to walk through 1-cell-thick walls under specific conditions.
 */
public class WizardMovementStrategy implements MovementStrategy {
    private final Random randomiser = new Random();

    /**
     * Sets the seed for the random number generator to ensure deterministic behavior during testing.
     *
     * @param seed the seed value for the randomizer
     */
    public void setSeed(int seed) {
        randomiser.setSeed(seed);
    }

    /**
     * Determines the next location for the Wizard monster by evaluating its 8 neighbor cells
     * and applying wall-phasing logic when encountering a 1-cell-thick wall.
     *
     * @param monster the monster instance using this strategy
     * @param grid    the current game grid layout
     * @return the next Location the monster should move to
     */
    @Override
    public Location determineMovement(Monster monster, PacManGameGrid grid) {
        Location current = monster.getLocation();

        // Wizard select one of its neighbour locations (8 cells around the Wizard).
        List<Location.CompassDirection> directions = Arrays.asList(Location.CompassDirection.values());
        Collections.shuffle(directions, randomiser);

        for (Location.CompassDirection direction : directions) {
            Location neighbour = current.getNeighbourLocation(direction);

            // If the selected location is not a maze wall, Wizard will move to that location.
            if (grid.getCell(neighbour) > 0) {
                return neighbour;
            }

            // If the selected location is a maze wall, it will check if the adjacent location in the same direction... is a wall or not
            Location beyondWall = neighbour.getNeighbourLocation(direction);

            // "If the adjacent location is not a wall, the Wizard will walk through the wall
            if (grid.getCell(beyondWall) > 0) {
                return beyondWall;
            }

            // If the adjacent location is a wall, the Wizard will randomly select another neighbour
        }
        return current;
    }

    /**
     * Evaluates whether the Wizard can phase through a wall given a specific compass direction
     * during a scripted movement step.
     *
     * @param monster   the monster instance attempting to phase
     * @param grid      the current game grid layout
     * @param direction the compass direction of the movement attempt
     * @return the landing Location past the wall if valid, or null if wall-phasing is not possible
     */
    @Override
    public Location tryWallPhase(Monster monster, PacManGameGrid grid, Location.CompassDirection direction) {
        Location current = monster.getLocation();
        Location neighbour = current.getNeighbourLocation(direction);

        // Only phase if the immediate neighbor is a solid wall
        if (grid.getCell(neighbour) > 0) {
            return null;
        }

        // Look one cell further in the same direction
        Location beyondWall = neighbour.getNeighbourLocation(direction);

        // Only allow the jump if the landing cell is walkable (not out of bounds or another wall)
        return grid.getCell(beyondWall) > 0 ? beyondWall : null;
    }

    /**
     * Polymorphic override to hand back the Wizard's specific immunity effect (WizardIceEffect)
     * when an ice cube is consumed.
     *
     * @return a MonsterEffect representing the Wizard's reduced-speed freeze behavior
     */
    @Override
    public MonsterEffect createFreezeEffect() {
        return new WizardIceEffect();
    }
}