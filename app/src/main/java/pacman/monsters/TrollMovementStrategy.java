package pacman.monsters;

import ch.aplu.jgamegrid.Location;
import pacman.PacManGameGrid;

import java.util.Random;

/**
 * Implements the movement strategy for the Troll monster.
 * Trolls move randomly by turning left or right, attempting forward movement,
 * and handling wall collisions by turning back or reversing directions.
 */
public class TrollMovementStrategy implements MovementStrategy {
    private final Random randomiser = new Random();

    /**
     * Sets the seed for the random number generator to ensure deterministic movement during testing.
     *
     * @param seed the seed value for the randomizer
     */
    public void setSeed(int seed) {
        randomiser.setSeed(seed);
    }

    /**
     * Determines the next location for the Troll monster based on its randomized
     * turning and collision-handling rules.
     *
     * @param monster the monster instance using this strategy
     * @param grid    the current game grid layout
     * @return the next Location the monster should move to
     */
    @Override
    public Location determineMovement(Monster monster, PacManGameGrid grid) {
        double oldDirection = monster.getDirection();
        int sign = randomiser.nextDouble() < 0.5 ? 1 : -1;

        monster.setDirection(oldDirection);
        monster.turn(sign * 90);
        Location next = monster.getNextMoveLocation();
        if (monster.canMove(next)) {
            return next;
        }

        monster.setDirection(oldDirection);
        next = monster.getNextMoveLocation();
        if (monster.canMove(next)) {
            return next;
        }

        monster.setDirection(oldDirection);
        monster.turn(-sign * 90);
        next = monster.getNextMoveLocation();
        if (monster.canMove(next)) {
            return next;
        }

        monster.setDirection(oldDirection);
        monster.turn(180);
        return monster.getNextMoveLocation();
    }
}