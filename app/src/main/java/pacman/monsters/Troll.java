package pacman.monsters;

import pacman.GameController;

public class Troll extends Monster {
    public Troll(GameController gameController) {
        super(gameController, new TrollMovementStrategy(), Troll.class);
    }

    @Override
    public MonsterType getType() {
        return MonsterType.Troll;
    }
}
