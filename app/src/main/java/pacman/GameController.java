package pacman;

import ch.aplu.jgamegrid.Actor;
import ch.aplu.jgamegrid.Location;
import pacman.monsters.Monster;
import pacman.monsters.MonsterFactory;
import pacman.monsters.EffectManager;
import pacman.Configurations.ConfigurationLoader;
import pacman.Configurations.GameConfiguration;
import pacman.Configurations.ItemConfiguration;
import pacman.Item.Item;
import pacman.Item.ItemFactory;
import pacman.Item.ItemType;
import pacman.utility.GameCallback;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Coordinates the main Pac-Man game state and runtime behaviour.
 *
 * <p>The controller creates the maze, Pac-Man, monsters, and items. It also
 * processes item consumption, checks collision and end-game conditions,
 * manages effects, and provides game data to the {@link Game} class.</p>
 */
public class GameController {
    private int nbHorzCells = 20;
    private int nbVertCells = 11;
    protected PacManGameGrid grid;

    private List<Item> activeItems = new ArrayList<>();
    private List<ActorLocation> itemActorLocations = new ArrayList<>();

    private boolean hasPacmanBeenHit = false;
    private boolean hasPacmanEatAllPills = false;

    private Properties properties;
    private int seed = 30006;
    private int currentTick = 0;
    private ArrayList<Location> propertyPillLocations = new ArrayList<>();
    private ArrayList<Location> propertyGoldLocations = new ArrayList<>();

    protected PacActor pacActor;

    private GameCallback gameCallback;
    private List<Monster> monsters = new ArrayList<>();
    private List<ActorLocation> monstersLocation = new ArrayList<>();

    private Location pacManLocation;
    private Location.CompassDirection pacManDirection;

    private boolean usesConfigurableMap;
    private List<ItemConfiguration> itemConfigurations = new ArrayList<>();

    private EffectManager effectManager;

    /**
     * Creates a game controller and initialises the game configuration.
     *
     * <p>If a configurable map is supplied through {@code Map.layout}, the
     * map, items, Pac-Man, and monsters are created from the configuration.
     * Otherwise, the controller loads the legacy map and object locations from
     * the supplied properties.</p>
     *
     * @param nbHorzCells default number of horizontal maze cells
     * @param nbVertCells default number of vertical maze cells
     * @param properties properties used to configure the game
     * @param gameCallback callback used to report game events
     */
    public GameController(int nbHorzCells, int nbVertCells, Properties properties, GameCallback gameCallback) {
        this.properties = properties;
        this.gameCallback = gameCallback;

        String mapLayout = properties.getProperty("Map.layout");
        // Determine whether the new configurable map format is being used.
        this.usesConfigurableMap = (mapLayout != null && !mapLayout.trim().isEmpty());

        if (usesConfigurableMap) {
            /*
             * Load the map and game objects from the configuration file.
             */
            GameConfiguration config = new ConfigurationLoader().load(properties);
            this.nbHorzCells = config.getWidth();
            this.nbVertCells = config.getHeight();
            this.grid = new PacManGameGrid(config.getMazeLayout(), config.getWidth(), config.getHeight());
            this.pacManLocation = config.getPacManLocation();
            this.pacManDirection = config.getPacManDirection();
            this.itemConfigurations = new ArrayList<>(config.getItemConfigurations());

            pacActor = new PacActor(this);
            pacActor.setPropertyMoves(properties.getProperty("PacMan.move"));
            pacActor.setAuto(Boolean.parseBoolean(properties.getProperty("PacMan.isAuto")));

            this.monstersLocation = new MonsterFactory().createMonsters(this, config);
        } else {
            /*
             * Load the legacy map and object locations from properties.
             */
            this.nbHorzCells = nbHorzCells;
            this.nbVertCells = nbVertCells;
            this.grid = new PacManGameGrid(nbHorzCells, nbVertCells);

            String[] pacManLocations = properties.getProperty("PacMan.location").split(",");
            this.pacManLocation = new Location(
                    Integer.parseInt(pacManLocations[0]),
                    Integer.parseInt(pacManLocations[1]));
            // Pac-Man faces East by default in the legacy configuration.
            this.pacManDirection = Location.EAST;

            pacActor = new PacActor(this);
            pacActor.setPropertyMoves(properties.getProperty("PacMan.move"));
            pacActor.setAuto(Boolean.parseBoolean(properties.getProperty("PacMan.isAuto")));
            loadPillAndItemsLocations();

            this.monstersLocation = new MonsterFactory().createMonstersFromProperties(this, properties, propertyGoldLocations);
        }
        /*
         * Extract Monster objects from their ActorLocation wrappers.
         */
        for (ActorLocation monsterLocation: this.monstersLocation) {
            Actor actor = monsterLocation.getActor();
            if (actor instanceof Monster) {
                Monster monster = (Monster) actor;
                this.monsters.add(monster);
            }
        }

        // Create the effect manager after all monsters have been loaded.
        this.effectManager = new EffectManager(monsters, gameCallback);

        String seedProperty = properties.getProperty("seed");
        if (seedProperty != null && !seedProperty.isEmpty()) {
            seed = Integer.parseInt(seedProperty);
            pacActor.setSeed(seed);
            for (Monster monster: monsters) {
                monster.setSeed(seed);
            }
        }
        // Create the items used by the current map.
        setupPillAndItemsLocations();
    }

    public PacActor getPacActor() {
        return pacActor;
    }

    public PacManGameGrid getGrid() {
        return grid;
    }

    public boolean isUsingConfigurableMap() {
        return usesConfigurableMap;
    }

    public void incrementTick() {
        currentTick++;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public void stopMonsters() {
        for (Monster monster: monsters) {
            monster.setStopMoving(true);
        }
    }

    public boolean isNormal(Location location) {
        int a = grid.getCell(location);
        return a > 0;
    }

    public GameCallback getGameCallback() {
        return gameCallback;
    }
    /**
     * Determines whether Pac-Man has collected all required items.
     *
     * @return {@code true} if all required items have been collected;
     *         otherwise, {@code false}
     */
    public boolean isHasPacmanBeenHit() {
        return hasPacmanBeenHit;
    }

    public boolean isHasPacmanEatAllPills() {
        return hasPacmanEatAllPills;
    }

    public boolean hasEndGame() {
        return hasPacmanBeenHit || hasPacmanEatAllPills;
    }

    public void checkEndGame() {
        int maxPillsAndItems = countPillsAndItems();
        for (Monster monster: monsters) {
            hasPacmanBeenHit = hasPacmanBeenHit || monster.getLocation().equals(pacActor.getLocation());
        }
        hasPacmanEatAllPills = pacActor.getNbPills() >= maxPillsAndItems;
    }

    /**
     * Counts the number of pills and gold pieces required to complete the
     * game.
     *
     * @return number of required score-awarding items
     */
    private int countPillsAndItems() {
        if (usesConfigurableMap) {
            int count = 0;
            for (ItemConfiguration item : itemConfigurations) {
                if (item.getType() == ItemType.PILL || item.getType() == ItemType.GOLDPIECE) {
                    count++;
                }
            }
            return count;
        }

        int pillsAndItemsCount = 0;
        for (int y = 0; y < nbVertCells; y++) {
            for (int x = 0; x < nbHorzCells; x++) {
                Location location = new Location(x, y);
                int a = grid.getCell(location);
                boolean isPill = false;
                boolean isGold = false;

                if (!propertyPillLocations.isEmpty()) {
                    if (propertyPillLocations.contains(location)) isPill = true;
                } else {
                    if (a == 1) isPill = true;
                }

                if (!propertyGoldLocations.isEmpty()) {
                    if (propertyGoldLocations.contains(location)) isGold = true;
                } else {
                    if (a == 3) isGold = true;
                }

                if (isPill || isGold) {
                    pillsAndItemsCount++;
                }
            }
        }
        return pillsAndItemsCount;
    }
    /**
     * Loads pill and gold-piece locations from the game properties.
     *
     * <p>Locations must be specified as comma-separated coordinates, with
     * multiple locations separated by semicolons.</p>
     */
    private void loadPillAndItemsLocations() {
        String pillsLocationString = properties.getProperty("Pills.location");
        if (pillsLocationString != null && !pillsLocationString.trim().isEmpty()) {
            String[] singlePillLocationStrings = pillsLocationString.split(";");
            for (String singlePillLocationString: singlePillLocationStrings) {
                String[] locationStrings = singlePillLocationString.split(",");
                propertyPillLocations.add(new Location(Integer.parseInt(locationStrings[0].trim()), Integer.parseInt(locationStrings[1].trim())));
            }
        }

        String goldLocationString = properties.getProperty("Gold.location");
        if (goldLocationString != null && !goldLocationString.trim().isEmpty()) {
            String[] singleGoldLocationStrings = goldLocationString.split(";");
            for (String singleGoldLocationString: singleGoldLocationStrings) {
                String[] locationStrings = singleGoldLocationString.split(",");
                propertyGoldLocations.add(new Location(Integer.parseInt(locationStrings[0].trim()), Integer.parseInt(locationStrings[1].trim())));
            }
        }
    }

    public void setupPillAndItemsLocations() {
        activeItems.clear();
        itemActorLocations.clear();

        if (usesConfigurableMap) {
            for (ItemConfiguration config : itemConfigurations) {
                Item newItem = ItemFactory.createItem(config.getType(), config.getLocation());
                activeItems.add(newItem);
                if (newItem instanceof Actor) {
                    itemActorLocations.add(new ActorLocation((Actor) newItem, config.getLocation(), Location.CompassDirection.NORTH));
                }
            }
            return;
        }

        for (int y = 0; y < nbVertCells; y++) {
            for (int x = 0; x < nbHorzCells; x++) {
                Location location = new Location(x, y);
                int a = grid.getCell(location);

                boolean hasPill = (!propertyPillLocations.isEmpty()) ? propertyPillLocations.contains(location) : (a == 1);
                boolean hasGold = (!propertyGoldLocations.isEmpty()) ? propertyGoldLocations.contains(location) : (a == 3);
                boolean hasIce = (a == 4);

                if (hasPill) {
                    Item pill = ItemFactory.createItem(ItemType.PILL, location);
                    activeItems.add(pill);
                } else if (hasGold) {
                    Item gold = ItemFactory.createItem(ItemType.GOLDPIECE, location);
                    activeItems.add(gold);
                    itemActorLocations.add(new ActorLocation((Actor) gold, location, Location.CompassDirection.NORTH));
                } else if (hasIce) {
                    Item ice = ItemFactory.createItem(ItemType.ICECUBE, location);
                    activeItems.add(ice);
                    itemActorLocations.add(new ActorLocation((Actor) ice, location, Location.CompassDirection.NORTH));
                }
            }
        }
    }

    public List<ActorLocation> getActorLocations() {
        List<ActorLocation> actorLocations = new ArrayList<>();
        actorLocations.add(new ActorLocation(pacActor, pacManLocation, pacManDirection));
        actorLocations.addAll(monstersLocation);
        actorLocations.addAll(itemActorLocations);
        return actorLocations;
    }

    public List<Location> getPillAndItemLocations() {
        List<Location> locations = new ArrayList<>();
        for (Item item : activeItems) {
            locations.add(item.getLocation());
        }
        return locations;
    }

    public List<Item> getActiveItems() {
        return activeItems;
    }

    public void processItem(Location location) {
        Item eatenItem = null;
        for (Item item : activeItems) {
            if (item.getLocation().equals(location)) {
                // If it is an actor, verify it's visible. Pills don't need this check.
                if (!(item instanceof Actor) || ((Actor) item).isVisible()) {
                    eatenItem = item;
                    break;
                }
            }
        }

        if (eatenItem != null) {
            eatenItem.consume(pacActor, this);
            activeItems.remove(eatenItem);
            pacActor.getBackground().fillCell(location, Color.lightGray);
        }
    }

    public int getNumHorzCells(){
        return this.nbHorzCells;
    }

    public int getNumVertCells(){
        return this.nbVertCells;
    }

    /**
     * Triggers the fury effect if the feature is enabled.
     *
     * @param location location where the fury effect begins
     */
    public void triggerFury(Location location) {
        if (Boolean.parseBoolean(properties.getProperty("feature2.active", "false"))) {
            effectManager.triggerFury(currentTick, location);
        }
    }

    /**
     * Triggers the freeze effect if the feature is enabled.
     *
     * @param location location where the freeze effect begins
     */
    public void triggerFreeze(Location location) {
        if (Boolean.parseBoolean(properties.getProperty("feature2.active", "false"))) {
            effectManager.triggerFreeze(currentTick, location);
        }
    }
}