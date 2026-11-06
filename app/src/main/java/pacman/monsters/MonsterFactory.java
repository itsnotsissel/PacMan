package pacman.monsters;

import ch.aplu.jgamegrid.Location;
import pacman.ActorLocation;
import pacman.Configurations.GameConfiguration;
import pacman.Configurations.MonsterConfiguration;
import pacman.Configurations.ItemConfiguration;
import pacman.Item.ItemType;
import pacman.GameController;

import java.util.Properties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates and configures monster actors.
 *
 * <p>The factory supports both configurable-map properties and the legacy
 * property format. It converts monster configurations into
 * {@link ActorLocation} objects that can be placed on the game grid.</p>
 */
public class MonsterFactory {

    /**
     * Creates monsters from a complete game configuration.
     *
     * @param gameController controller responsible for the current game
     * @param config game configuration containing monster definitions
     * @return actor-location configurations for the created monsters
     */
    public List<ActorLocation> createMonsters(GameController gameController, GameConfiguration config) {
        List<Location> goldLocations = extractGoldLocations(config);
        return createMonsters(gameController, config.getMonsterConfigurations(), goldLocations);
    }


    /**
     * Creates monsters from monster configurations.
     *
     * @param gameController controller responsible for the current game
     * @param monsterConfigs monster configurations to create
     * @param goldLocations locations of gold pieces in the maze
     * @return actor-location configurations for the created monsters
     */
    public List<ActorLocation> createMonsters(GameController gameController,
                                              List<MonsterConfiguration> monsterConfigs,
                                              List<Location> goldLocations) {
        List<ActorLocation> actorLocations = new ArrayList<>();
        for (MonsterConfiguration monsterConfig : monsterConfigs) {
            Monster monster = createMonster(gameController, monsterConfig, goldLocations);
            monster.setId(monsterConfig.getId());
            monster.setScriptedMoves(monsterConfig.getScriptedMonsters());
            actorLocations.add(new ActorLocation(monster, monsterConfig.getLocation(), monsterConfig.getDirection()));
        }
        return actorLocations;
    }

    /**
     * Creates monsters from the legacy properties format.
     *
     * @param gameController controller responsible for the current game
     * @param properties legacy game properties
     * @param goldLocations locations of gold pieces in the maze
     * @return actor-location configurations for the created monsters
     */
    public List<ActorLocation> createMonstersFromProperties(GameController gameController,
                                                            Properties properties,
                                                            List<Location> goldLocations) {
        List<MonsterConfiguration> monsterConfigs = new ArrayList<>();
        addLegacyMonsterIfPresent(monsterConfigs, properties, MonsterType.Troll, "Troll.location");
        addLegacyMonsterIfPresent(monsterConfigs, properties, MonsterType.Wizard, "Wizard.location");
        addLegacyMonsterIfPresent(monsterConfigs, properties, MonsterType.Orion, "Orion.location");
        return createMonsters(gameController, monsterConfigs, goldLocations);
    }

    /**
     * Adds a legacy monster configuration when its location is present.
     *
     * @param monsterConfigs list to which the configuration is added
     * @param properties legacy game properties
     * @param type type of monster to configure
     * @param locationKey property key containing the monster location
     */
    private void addLegacyMonsterIfPresent(List<MonsterConfiguration> monsterConfigs,
                                           Properties properties,
                                           MonsterType type,
                                           String locationKey) {
        String locationValue = properties.getProperty(locationKey);
        if (locationValue == null || locationValue.trim().isEmpty()) {
            return;
        }
        String[] parts = locationValue.split(",");
        int x = Integer.parseInt(parts[0].trim());
        int y = Integer.parseInt(parts[1].trim());

        // CHANGED: Fixed <= to < to allow valid zero-index spawning
        if (x < 0 || y < 0) {
            return;
        }
        monsterConfigs.add(new MonsterConfiguration(
                type,
                readScriptedMoves(properties, type.name(), 0),
                0,
                new Location(x, y),
                Location.NORTH));
    }

    /**
     * Reads and validates scripted movements for a monster.
     *
     * @param properties game properties
     * @param type monster type name
     * @param id monster identifier
     * @return validated scripted movement commands
     * @throws IllegalArgumentException if a movement command is invalid
     */
    private List<String> readScriptedMoves(Properties properties, String type, int id) {
        String value = properties.getProperty(type + "." + id + ".move", "");
        if (value.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<String> moves = new ArrayList<>();
        for (String token : value.split(",")) {
            String move = token.trim().toUpperCase();
            if (!move.equals("R") && !move.equals("L") && !move.equals("M") && !move.equals("S")) {
                throw new IllegalArgumentException("Invalid scripted move: " + move);
            }
            moves.add(move);
        }
        return moves;
    }

    /**
     * Creates a concrete monster based on its configured type.
     *
     * @param gameController controller responsible for the current game
     * @param monsterConfig configuration for the monster
     * @param goldLocations locations of gold pieces in the maze
     * @return a newly created monster
     * @throws IllegalArgumentException if the monster type is unknown
     */
    private Monster createMonster(GameController gameController, MonsterConfiguration monsterConfig, List<Location> goldLocations) {
        switch (monsterConfig.getMonsterType()) {
            case Troll:
                return new Troll(gameController);
            case Wizard:
                return new Wizard(gameController);
            case Orion:
                return new Orion(gameController, goldLocations);
            default:
                throw new IllegalArgumentException("Unknown monster type: " + monsterConfig.getMonsterType());
        }
    }

    /**
     * Extracts gold-piece locations from a game configuration.
     *
     * @param config game configuration containing item definitions
     * @return locations of all configured gold pieces
     */
    private List<Location> extractGoldLocations(GameConfiguration config) {
        List<Location> goldLocations = new ArrayList<>();
        for (ItemConfiguration itemConfig : config.getItemConfigurations()) {
            if (itemConfig.getType() == ItemType.GOLDPIECE) {
                goldLocations.add(itemConfig.getLocation());
            }
        }
        return goldLocations;
    }
}