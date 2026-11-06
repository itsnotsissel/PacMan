package pacman.monsters;

import ch.aplu.jgamegrid.Location;
import pacman.PacManGameGrid;

public interface MonsterEffect {
    // scriptedMove is null when the monster has no script left this tick.
    Location modifyMovement(MovementStrategy baseStrategy, Monster monster, PacManGameGrid grid, String scriptedMove);
    void tick();
    boolean isExpired();
}