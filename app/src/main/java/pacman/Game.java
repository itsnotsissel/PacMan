package pacman;

import ch.aplu.jgamegrid.*;
import pacman.utility.GameCallback;
import pacman.Configurations.ConfigurationLoader;
import pacman.Item.Item;
import pacman.Item.ItemType;

import java.awt.*;
import java.util.List;
import java.util.Properties;

/**
 * Main game window and game-grid coordinator.
 *
 * <p>The {@code Game} class creates the game grid, initialises the
 * {@link GameController}, draws the maze, places actors, starts the game loop,
 * and displays the final game result.</p>
 */
public class Game extends GameGrid
{
    private final static int DEFAULT_HORZ_CELLS = 20;
    private final static int DEFAULT_VERT_CELLS = 11;
    private final static int DEFAULT_SIMULATION_PERIOD_MS = 300;
    private final int nbHorzCells;
    private final int nbVertCells;
    private GameController gameController;
    private GameCallback gameCallback;

    /**
     * Creates and initialises the Pac-Man game.
     *
     * @param gameCallback callback used to report game events
     * @param properties game configuration properties
     */
    public Game(GameCallback gameCallback, Properties properties)
    {
        super(resolveWidth(properties), resolveHeight(properties), 20, false);
        this.nbHorzCells = resolveWidth(properties);
        this.nbVertCells = resolveHeight(properties);
        this.gameCallback = gameCallback;

        // Create the controller that owns the main game state.
        gameController = new GameController(nbHorzCells, nbVertCells, properties, gameCallback);
        setSimulationPeriod(resolveSimulationPeriod(properties));
        setTitle("[PacMan in the Multiverse]");

        GGBackground bg = getBg();
        // Draw the maze background before placing actors.
        drawGrid(bg);

        addKeyRepeatListener(gameController.getPacActor().getPacmanController());
        setKeyRepeatPeriod(150);

        // Place Pac-Man, monsters, and actor-based items on the grid.
        setupActorLocations();
    }

    /**
     * Resolves the maze width from the configurable map, if available.
     *
     * @param properties game configuration properties
     * @return configured map width or the default width
     */
    private static int resolveWidth(Properties properties) {
        String mapLayout = properties.getProperty("Map.layout");
        if (mapLayout != null && !mapLayout.trim().isEmpty()) {
            return new ConfigurationLoader().load(properties).getWidth();
        }
        return DEFAULT_HORZ_CELLS;
    }

    /**
     * Resolves the maze height from the configurable map, if available.
     *
     * @param properties game configuration properties
     * @return configured map height or the default height
     */
    private static int resolveHeight(Properties properties) {
        String mapLayout = properties.getProperty("Map.layout");
        if (mapLayout != null && !mapLayout.trim().isEmpty()) {
            return new ConfigurationLoader().load(properties).getHeight();
        }
        return DEFAULT_VERT_CELLS;
    }

    public GameCallback getGameCallback() {
        return gameCallback;
    }

    /**
     * Performs one simulation step.
     *
     * <p>The controller tick is incremented after the parent game-grid
     * behaviour has been processed.</p>
     */
    @Override
    public void act() {
        super.act();
        gameController.incrementTick();
        updateTitle();
    }

    private void updateTitle() {
        setTitle("[PacMan in the Multiverse] Tick: " + gameController.getCurrentTick()
                + " Score: " + gameController.getPacActor().getScore());
    }

    /**
     * Resolves the simulation period from the supplied properties.
     *
     * @param properties game configuration properties
     * @return simulation period in milliseconds
     */
    private static int resolveSimulationPeriod(Properties properties) {
        String period = properties.getProperty("Simulation.period");
        if (period == null || period.isEmpty()) {
            return DEFAULT_SIMULATION_PERIOD_MS;
        }
        return Integer.parseInt(period.trim());
    }

    public String runApp() {
        doRun();
        show();

        // Continue checking game conditions until Pac-Man wins or loses.
        do {
            gameController.checkEndGame();
            delay(10);
        } while(!gameController.hasEndGame());
        delay(120);

        Location loc = gameController.getPacActor().getLocation();
        gameController.stopMonsters();
        gameController.getPacActor().removeSelf();

        String title = "";
        if (gameController.isHasPacmanBeenHit()) {
            getBg().setPaintColor(Color.red);
            title = "GAME OVER";
            addActor(new Actor("sprites/explosion3.gif"), loc);
        } else if (gameController.isHasPacmanEatAllPills()) {
            getBg().setPaintColor(Color.yellow);
            title = "YOU WIN";
        }
        setTitle(title);
        gameCallback.endOfGame(title);

        doPause();

        return gameCallback.getAllLog();
    }

    private void setupActorLocations() {
        List<ActorLocation> actorLocations = gameController.getActorLocations();
        for (ActorLocation actorLocation: actorLocations) {
            addActor(actorLocation.getActor(), actorLocation.getLocation(), actorLocation.getDirection());
        }
    }


    private void drawGrid(GGBackground bg)
    {
        bg.clear(Color.gray);
        bg.setPaintColor(Color.white);
        for (int y = 0; y < nbVertCells; y++)
        {
            for (int x = 0; x < nbHorzCells; x++)
            {
                bg.setPaintColor(Color.white);
                Location location = new Location(x, y);
                if (gameController.isNormal(location))
                    bg.fillCell(location, Color.lightGray);
            }
        }

        // Draw pills directly onto the background using the original implementation
        for (Item item : gameController.getActiveItems()) {
            if (item.getType() == ItemType.PILL) {
                putPill(bg, item.getLocation());
            }
        }
    }

    private void putPill(GGBackground bg, Location location){
        bg.setPaintColor(Color.white);
        bg.fillCircle(toPoint(location), 5);
    }
}