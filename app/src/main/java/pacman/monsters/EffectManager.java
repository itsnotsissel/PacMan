package pacman.monsters;

import ch.aplu.jgamegrid.Location;
import pacman.utility.GameCallback;

import java.util.List;

/**
 * Manages temporary effects applied to monsters.
 *
 * <p>The manager is responsible for triggering fury and freeze effects,
 * notifying the game callback, and determining whether any monster is
 * currently frozen.</p>
 */
public class EffectManager {
    private final List<Monster> monsters;
    private final GameCallback gameCallback;

    /**
     * Creates an effect manager for the supplied monsters.
     *
     * @param monsters monsters that may receive effects
     * @param gameCallback callback used to report effect events
     */
    public EffectManager(List<Monster> monsters, GameCallback gameCallback) {
        this.monsters = monsters;
        this.gameCallback = gameCallback;
    }

    /**
     * Triggers the fury effect at a specified location.
     *
     * <p>The effect is logged even when monsters are currently frozen. However,
     * no monster becomes furious while a freeze effect is active.</p>
     *
     * @param tick current game tick
     * @param triggerLocation location where the fury effect was triggered
     */
    public void triggerFury(int tick, Location triggerLocation) {
        // The effect must be logged regardless of whether monsters are immune
        gameCallback.effectTriggered(tick, "Fury", "gold", triggerLocation);

        // no monster gets furious while frozen is active.
        if (isFrozen()) {
            return;
        }

        for (Monster monster : monsters) {
            monster.applyEffect(new FuriousEffect());
        }
    }

    /**
     * Triggers the freeze effect for every monster.
     *
     * <p>Each monster creates its own freeze effect through its movement
     * strategy. This allows specialised monsters, such as Wizards, to return
     * a specialised effect implementation.</p>
     *
     * @param tick current game tick
     * @param triggerLocation location where the freeze effect was triggered
     */
    public void triggerFreeze(int tick, Location triggerLocation) {
        // Moved logging to the top for consistency
        gameCallback.effectTriggered(tick, "Freeze", "ice", triggerLocation);

        for (Monster monster : monsters) {
            // Polymorphic: Wizard's own strategy hands back a
            // WizardIceEffect here, every other monster gets a plain
            // FrozenEffect, with no type-check anywhere in this class.
            monster.applyEffect(monster.getFreezeEffect());
        }
    }

    /**
     * Determines whether at least one monster is currently frozen.
     *
     * @return {@code true} if a monster has an active ice effect; otherwise,
     *         {@code false}
     */
    public boolean isFrozen() {
        for (Monster monster : monsters) {
            // Checking against IceEffect (the shared abstract class for
            // FrozenEffect/WizardIceEffect) rather than either concrete
            // class individually, matches the team's IceEffect
            // structure instead of introducing a separate marker method.
            if (monster.getCurrentEffect() instanceof IceEffect) {
                return true;
            }
        }
        return false;
    }
}