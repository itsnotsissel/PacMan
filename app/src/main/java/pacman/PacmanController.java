package pacman;

import ch.aplu.jgamegrid.GGKeyRepeatListener;
import ch.aplu.jgamegrid.Location;

import java.awt.event.KeyEvent;

/**
 * Handles keyboard input for manually controlled Pac-Man movement.
 *
 * <p>Keyboard input is ignored when Pac-Man is in automatic mode or has
 * already been removed from the game.</p>
 */
class PacmanController implements GGKeyRepeatListener {
    private PacActor pacActor;
    public PacmanController(PacActor pacActor) {
        this.pacActor = pacActor;
    }

    /**
     * Processes a repeated keyboard input event.
     *
     * <p>The requested direction is applied before checking whether the
     * destination cell is walkable. If movement is possible, Pac-Man moves
     * and the game controller processes any item at the new location.</p>
     *
     * @param keyCode key code received from the keyboard
     */
    public void keyRepeated(int keyCode)
    {
        if (pacActor.isAuto()) {
            return;
        }
        if (pacActor.isRemoved())  // Already removed
            return;
        Location next = null;
        switch (keyCode)
        {
            case KeyEvent.VK_LEFT:
                next = pacActor.getLocation().getNeighbourLocation(Location.WEST);
                pacActor.setDirection(Location.WEST);
                break;
            case KeyEvent.VK_UP:
                next = pacActor.getLocation().getNeighbourLocation(Location.NORTH);
                pacActor.setDirection(Location.NORTH);
                break;
            case KeyEvent.VK_RIGHT:
                next = pacActor.getLocation().getNeighbourLocation(Location.EAST);
                pacActor.setDirection(Location.EAST);
                break;
            case KeyEvent.VK_DOWN:
                next = pacActor.getLocation().getNeighbourLocation(Location.SOUTH);
                pacActor.setDirection(Location.SOUTH);
                break;
        }
        if (next != null && pacActor.canMove(next))
        {
            pacActor.setLocation(next);
            pacActor.eatPill(next);
        }
    }

}
