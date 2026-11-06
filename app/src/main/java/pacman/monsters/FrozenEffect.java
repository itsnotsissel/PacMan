package pacman.monsters;

import ch.aplu.jgamegrid.Location;
import pacman.PacManGameGrid;

public class FrozenEffect extends IceEffect {
    private static final int FREEZE_DURATION_TICKS = 3;

    public FrozenEffect() {
        super(FREEZE_DURATION_TICKS);
    }

    @Override
    public Location modifyMovement(MovementStrategy baseStrategy, Monster monster, PacManGameGrid grid, String scriptedMove) {
        return monster.getLocation();
    }
}