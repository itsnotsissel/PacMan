
package pacman.monsters;

import ch.aplu.jgamegrid.Location;
import pacman.GameController;

import java.util.List;

public class Orion extends Monster {
    public Orion(GameController gameController, List<Location> goldLocations) {
        super(gameController, new OrionMovementStrategy(goldLocations), Orion.class);
    }

    @Override
    public MonsterType getType() {
        return MonsterType.Orion;
    }
}
