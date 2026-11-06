package pacman.Configurations;
import ch.aplu.jgamegrid.Location;
import pacman.monsters.Monster;
import pacman.monsters.MonsterType;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * Stores the configuration required to create a monster in the game.
 *
 * <p>A monster configuration contains the monster's type, identifier,
 * starting location, initial direction, and any scripted movements assigned
 * to it.</p>
 *
 * <p>The scripted movement list is copied when the configuration is created
 * and exposed as an unmodifiable list to prevent external modification.</p>
 */
public class MonsterConfiguration {
    private final MonsterType monsterType;
    private final List<String> ScriptedMonsters;
    private final int id;
    private final Location location;
    private final Location.CompassDirection Direction;

    /**
     * Creates a monster configuration.
     *
     * @param monsterType the type of monster
     * @param ScriptedMonsters the scripted movements assigned to the monster
     * @param id the monster's unique identifier
     * @param location the monster's starting location
     * @param Direction the monster's initial direction
     */
    public MonsterConfiguration(MonsterType monsterType, List<String> ScriptedMonsters,
                                int id, Location location,
                                Location.CompassDirection Direction) {
        this.monsterType = monsterType;
        this.ScriptedMonsters = new ArrayList<>(ScriptedMonsters);
        this.id = id;
        this.location = location;
        this.Direction = Direction;
    }

    public MonsterType getMonsterType() {
        return monsterType;
    }

    public List<String> getScriptedMonsters() {
        return Collections.unmodifiableList(ScriptedMonsters);
    }
    public int getId() {
        return id;
    }
    public Location getLocation() {
        return location;
    }
    public Location.CompassDirection getDirection() {
        return Direction;
    }




}
