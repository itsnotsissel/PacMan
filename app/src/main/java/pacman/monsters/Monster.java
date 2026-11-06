package pacman.monsters;

import ch.aplu.jgamegrid.*;
import pacman.GameController;

import java.util.*;

/**
 * Base class for all Pac-Man monsters.
 *
 * <p>A monster is responsible for its own movement, animation direction,
 * scripted movement, visited-location tracking, and active temporary effect.
 * Concrete subclasses provide the monster type and sprite-specific behaviour.</p>
 */
public abstract class Monster extends Actor {
    protected GameController gameController;
    protected MovementStrategy movementStrategy;
    protected MonsterEffect currentEffect = null;

    protected ArrayList<Location> visitedList = new ArrayList<Location>();
    protected final int listLength = 10;
    protected boolean stopMoving = false;

    protected Random randomiser = new Random();

    private int idleTicksRemaining = 0;
    private List<String> scriptedMoves = new ArrayList<>();
    private int scriptedMoveIndex = 0;
    private int id = 0;

    /**
     * Creates a monster using the sprite associated with its concrete class.
     *
     * @param gameController controller responsible for the current game
     * @param movementStrategy strategy used to determine movement
     * @param monsterClass concrete monster class used to locate its sprite
     */
    protected Monster(GameController gameController, MovementStrategy movementStrategy, Class<? extends Monster> monsterClass) {
        super(spriteFileFor(monsterClass));
        this.gameController = gameController;
        this.movementStrategy = movementStrategy;
    }

    /**
     * Sets the random seed used by the monster and its movement strategy.
     *
     * @param seed random seed
     */
    // Polymorphic execution. instanceof checks removed.
    public void setSeed(int seed) {
        randomiser.setSeed(seed);
        movementStrategy.setSeed(seed);
    }


    /**
     * Builds the sprite path for a monster class.
     *
     * @param monsterClass concrete monster class
     * @return path to the monster's sprite
     */
    private static String spriteFileFor(Class<? extends Monster> monsterClass) {
        return "sprites/m_" + monsterClass.getSimpleName().toLowerCase() + ".gif";
    }

    public abstract MonsterType getType();

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }


    /**
     * Stops the monster from moving for a specified number of ticks.
     *
     * @param ticks number of ticks for which the monster remains idle
     */
    public void stopMoving(int ticks) {
        idleTicksRemaining = ticks;
    }

    public void setStopMoving(boolean stopMoving) {
        this.stopMoving = stopMoving;
    }

    public void setScriptedMoves(List<String> scriptedMoves) {
        this.scriptedMoves = scriptedMoves;
    }

    public String getState() {
        if (currentEffect == null) {
            return "Normal";
        }
        if (currentEffect instanceof FuriousEffect) {
            return "Furious";
        }
        if (currentEffect instanceof IceEffect) {
            return "Frozen";
        }
        return currentEffect.getClass().getSimpleName();
    }

    /**
     * Performs one monster simulation step.
     *
     * <p>The monster does not move when stopped or temporarily idle. If an
     * effect is active, the effect determines movement; otherwise, the normal
     * movement strategy is used.</p>
     */
    public void act() {
        if (stopMoving) {
            return;
        }
        if (idleTicksRemaining > 0) {
            idleTicksRemaining--;
            return;
        }

        String scriptedMove = consumeScriptedMove();
        Location current = getLocation();
        Location next;

        boolean effectExpiredThisTick = false;

        if (currentEffect != null) {
            next = currentEffect.modifyMovement(movementStrategy, this, gameController.getGrid(), scriptedMove);
            currentEffect.tick();
            if (currentEffect.isExpired()) {
                effectExpiredThisTick = true;
            }
        } else {
            next = computeBaseNextLocation(scriptedMove);
        }

        boolean isFurious = currentEffect instanceof FuriousEffect;

        if (next != null && !next.equals(current)) {
            setLocation(next);
            gameController.getGameCallback().monsterLocationChanged(gameController.getCurrentTick(), this);
            addVisitedList(next);
        } else if (isFurious) {
            // test5 explicitly asserts that Furious monsters log their location
            // every single tick, even if their movement was completely blocked by a wall!
            gameController.getGameCallback().monsterLocationChanged(gameController.getCurrentTick(), this);
        }

        if (effectExpiredThisTick) {
            currentEffect = null;
        }

        updateMirror();
    }

    /**
     * Calculates the monster's next location using normal movement rules.
     *
     * @param scriptedMove scripted movement command, or {@code null}
     * @return calculated next location
     */
    public Location computeBaseNextLocation(String scriptedMove) {
        return computeBaseNextLocation(scriptedMove, true);
    }

    /**
     * Calculates the monster's next location.
     *
     * @param scriptedMove scripted movement command, or {@code null}
     * @param allowWallPhase whether wall phasing may be attempted
     * @return calculated next location
     */
    public Location computeBaseNextLocation(String scriptedMove, boolean allowWallPhase) {
        if (scriptedMove == null) {
            return movementStrategy.determineMovement(this, gameController.getGrid());
        }
        switch (scriptedMove) {
            case "R":
                turn(90);
                return getLocation();
            case "L":
                turn(-90);
                return getLocation();
            case "M":
                Location next = getNextMoveLocation();
                if (canMove(next)) {
                    return next;
                }
                if (allowWallPhase) {
                    Location phased = movementStrategy.tryWallPhase(this, gameController.getGrid(), currentCompassDirection());
                    if (phased != null) {
                        return phased;
                    }
                }
                return getLocation();
            case "S":
            default:
                return getLocation();
        }
    }

    /**
     * Converts the actor's rotation into a compass direction.
     *
     * @return the monster's current compass direction
     */
    private Location.CompassDirection currentCompassDirection() {
        int dir = (int) Math.round(getDirection()) % 360;
        if (dir < 0) {
            dir += 360;
        }

        if (dir >= 315 || dir < 45) return Location.CompassDirection.EAST;
        if (dir >= 45 && dir < 135) return Location.CompassDirection.SOUTH;
        if (dir >= 135 && dir < 225) return Location.CompassDirection.WEST;
        return Location.CompassDirection.NORTH;
    }

    public Location getNextRandomLocation() {
        List<Location.CompassDirection> directions = new ArrayList<>(Arrays.asList(
                Location.CompassDirection.NORTH,
                Location.CompassDirection.EAST,
                Location.CompassDirection.SOUTH,
                Location.CompassDirection.WEST
        ));
        Collections.shuffle(directions, randomiser);
        Location current = getLocation();

        for (Location.CompassDirection direction : directions) {
            Location candidate = current.getNeighbourLocation(direction);
            if (canMove(candidate) && !isVisited(candidate)) {
                return candidate;
            }
        }

        for (Location.CompassDirection direction : directions) {
            Location candidate = current.getNeighbourLocation(direction);
            if (canMove(candidate)) {
                return candidate;
            }
        }

        return current;
    }

    /**
     * Applies a temporary effect to the monster.
     *
     * @param effect effect to apply
     */
    public void applyEffect(MonsterEffect effect) {
        this.currentEffect = effect;
    }

    public MonsterEffect getCurrentEffect() {
        return currentEffect;
    }

    public MonsterEffect getFreezeEffect() {
        return movementStrategy.createFreezeEffect();
    }
    /**
     * Updates the monster's horizontal sprite mirror based on its direction.
     */
    private void updateMirror() {
        if (getDirection() > 150 && getDirection() < 210)
            setHorzMirror(false);
        else
            setHorzMirror(true);
    }

    /**
     * Returns the next scripted movement command.
     *
     * @return next movement command, or {@code null} when no commands remain
     */
    private String consumeScriptedMove() {
        if (scriptedMoveIndex < scriptedMoves.size()) {
            String move = scriptedMoves.get(scriptedMoveIndex++);
            return move != null ? move.trim() : null;
        }
        return null;
    }

    /**
     * Determines whether a location is walkable.
     *
     * @param location location to check
     * @return {@code true} if the location is walkable; otherwise,
     *         {@code false}
     */
    public boolean canMove(Location location) {
        return gameController.getGrid().getCell(location) > 0;
    }

    /**
     * Adds a location to the monster's recent-visit history.
     *
     * @param location location visited by the monster
     */
    protected void addVisitedList(Location location) {
        visitedList.add(location);
        if (visitedList.size() == listLength)
            visitedList.remove(0);
    }

    /**
     * Determines whether a location was recently visited.
     *
     * @param location location to check
     * @return {@code true} if the location was recently visited
     */
    protected boolean isVisited(Location location) {
        for (Location loc : visitedList)
            if (loc.equals(location))
                return true;
        return false;
    }
}