package pacman;

import ch.aplu.jgamegrid.Actor;
import ch.aplu.jgamegrid.Location;

/**
 * Stores an actor together with its starting location and direction.
 *
 * <p>This class is used by {@link GameController} and {@link Game} to
 * initialise actors on the game grid.</p>
 */
public class ActorLocation {
    private Actor actor;
    private Location location;
    private Location.CompassDirection direction;

    /**
     * Creates an actor-location configuration.
     *
     * @param actor actor to place on the game grid
     * @param location actor's starting location
     * @param direction actor's initial direction
     */
    public ActorLocation(Actor actor, Location location, Location.CompassDirection direction) {
        this.actor = actor;
        this.location = location;
        this.direction = direction;
    }

    public Actor getActor() {
        return actor;
    }

    public Location getLocation() {
        return location;
    }

    public Location.CompassDirection getDirection() {
        return direction;
    }
}
