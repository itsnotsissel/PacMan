
package pacman.monsters;

import pacman.GameController;

public class Wizard extends Monster {
    public Wizard(GameController gameController) {
        super(gameController, new WizardMovementStrategy(), Wizard.class);
    }

    @Override
    public MonsterType getType() {
        return MonsterType.Wizard;
    }
}
