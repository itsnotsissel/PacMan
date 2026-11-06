package pacman.Configurations;
import ch.aplu.jgamegrid.Location;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * Represents the complete configuration required to initialise a Pac-Man game.
 *
 * <p>This class stores the maze dimensions, maze layout, Pac-Man's starting
 * position and direction, as well as the configured monsters and items.</p>
 *
 * <p>Defensive copies of the supplied lists are created by the constructor.
 * The getter methods return unmodifiable views so that the configuration
 * cannot be changed through the returned lists.</p>
 */
public class GameConfiguration {
    private final int width;
    private final int height;
    private final List<String> mazeLayout;
    private final Location pacManLocation;
    private final Location.CompassDirection pacManDirection;
    //Configurations for all monsters in the maze.
    private final List<MonsterConfiguration> monsterConfigurations;
    //Configurations for all items in the maze.
    private final List<ItemConfiguration> itemConfigurations;

    /**
     * Creates a game configuration.
     *
     * @param width width of the maze in cells
     * @param height height of the maze in cells
     * @param mazeLayout maze layout represented as a list of rows
     * @param pacManLocation Pac-Man's starting location
     * @param pacManDirection Pac-Man's initial direction
     * @param monsterConfigurations configurations for the monsters in the maze
     * @param itemConfigurations configurations for the items in the maze
     */
    public GameConfiguration(int width, int height, List<String> mazeLayout, Location pacManLocation,
                             Location.CompassDirection pacManDirection,
                             List<MonsterConfiguration> monsterConfigurations,
                             List<ItemConfiguration> itemConfigurations) {
        this.width = width;
        this.height = height;
        this.mazeLayout = new ArrayList<>(mazeLayout);
        this.pacManLocation = pacManLocation;
        this.pacManDirection = pacManDirection;
        this.monsterConfigurations = new ArrayList<>(monsterConfigurations);
        this.itemConfigurations = new ArrayList<>(itemConfigurations);
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    public List<String> getMazeLayout() {
        return Collections.unmodifiableList(mazeLayout);
    }
    public Location getPacManLocation() {
        return pacManLocation;
    }

    public Location.CompassDirection getPacManDirection() {
        return pacManDirection;
    }

    public List<MonsterConfiguration> getMonsterConfigurations() {
        return Collections.unmodifiableList(monsterConfigurations);
    }

    public List<ItemConfiguration> getItemConfigurations() {
        return Collections.unmodifiableList(itemConfigurations);
    }

}
