package pacman.Configurations;
import java.util.*;

import pacman.monsters.MonsterType;
import ch.aplu.jgamegrid.Location;

import pacman.Item.ItemType;

/**
 * Loads and validates Pac-Man game configurations from a set of properties.
 *
 * <p>The map layout is read from the {@code Map.layout} property. Each
 * character in the layout represents either a game object or a type of map
 * tile. For example:</p>
 *
 * <ul>
 *     <li>{@code P} - Pac-Man's starting position</li>
 *     <li>{@code T} - Troll monster</li>
 *     <li>{@code O} - Orion monster</li>
 *     <li>{@code W} - Wizard monster</li>
 *     <li>{@code .} - Pill</li>
 *     <li>{@code G} or {@code g} - Gold piece</li>
 *     <li>{@code I} or {@code i} - Ice cube</li>
 *     <li>{@code X} or {@code x} - Wall</li>
 *     <li>Any other character - Empty walkable space</li>
 * </ul>
 *
 * <p>The loader also validates map dimensions, row lengths, object locations,
 * and scripted monster movements.</p>
 */
public class ConfigurationLoader {

    /**
     * Loads a complete game configuration from the supplied properties.
     *
     * @param properties properties containing the map, dimensions, and
     *                   optional monster movement scripts
     * @return a validated game configuration
     * @throws NullPointerException if {@code properties} is {@code null}
     * @throws IllegalArgumentException if the layout is invalid, dimensions do
     *                                  not match, required objects are
     *                                  missing, or an object is placed on a wall
     */
    public GameConfiguration load(Properties properties) {
        if (properties == null) {
            throw new NullPointerException("Properties cannot be null");
        }

        // Parse the layout and determine its dimensions.
        List<String> originalRows = parseLayout(properties.getProperty("Map.layout"));
        int height = originalRows.size();
        int width = originalRows.get(0).length();

        // Validate the configured dimensions and ensure all rows are equal.
        validateDimensions(properties, width, height);
        validateRows(originalRows, width);

        List<String> cleanRows = new ArrayList<>();
        List<MonsterConfiguration> monsters = new ArrayList<>();
        List<ItemConfiguration> items = new ArrayList<>();

        Location pacManLocation = null;

        // Each monster type receives its own sequential identifier.
        int trollId = 0;
        int orionId = 0;
        int wizardId = 0;
        /**
         * Convert the layout into a clean map containing only walkable spaces
         * and walls. Game objects are stored separately in configuration lists.
         */
        for (int y = 0; y < height; y++) {
            String row = originalRows.get(y);
            StringBuilder cleanRow = new StringBuilder();

            for (int x = 0; x < width; x++) {
                char symbol = row.charAt(x);
                Location location = new Location(x, y);

                switch (symbol) {
                    case 'P':
                        // Only one Pac-Man starting position is permitted.
                        if (pacManLocation != null) {
                            throw new IllegalArgumentException(
                                    "The map contains more than one PacMan.");
                        }
                        pacManLocation = location;
                        cleanRow.append(' ');
                        break;

                    case 'T':
                        monsters.add(new MonsterConfiguration(
                                MonsterType.Troll,
                                readScriptedMoves(
                                        properties,
                                        "Troll",
                                        trollId),
                                trollId,
                                location,
                                //Spawns facing North to match legacy default and test scripts
                                Location.CompassDirection.NORTH
                        ));
                        trollId++;
                        cleanRow.append(' ');
                        break;

                    case 'O':
                        monsters.add(new MonsterConfiguration(
                                MonsterType.Orion,
                                readScriptedMoves(
                                        properties,
                                        "Orion",
                                        orionId),
                                orionId,
                                location,
                                //Spawns facing North to match legacy default and test scripts
                                Location.CompassDirection.NORTH
                        ));
                        orionId++;
                        cleanRow.append(' ');
                        break;

                    case 'W':
                        monsters.add(new MonsterConfiguration(
                                MonsterType.Wizard,
                                readScriptedMoves(
                                        properties,
                                        "Wizard",
                                        wizardId),
                                wizardId,
                                location,
                                // Spawns facing North to match legacy default and test scripts
                                Location.CompassDirection.NORTH
                        ));
                        wizardId++;
                        cleanRow.append(' ');
                        break;

                    case '.':
                        items.add(new ItemConfiguration(
                                ItemType.PILL,
                                location));
                        cleanRow.append(' ');
                        break;

                    case 'g':
                    case 'G':
                        items.add(new ItemConfiguration(
                                ItemType.GOLDPIECE,
                                location));
                        cleanRow.append(' ');
                        break;

                    case 'i':
                    case 'I':
                        items.add(new ItemConfiguration(
                                ItemType.ICECUBE,
                                location));
                        cleanRow.append(' ');
                        break;

                    case 'x':
                    case 'X':
                        // Walls remain in the cleaned layout.
                        cleanRow.append('x');
                        break;

                    default:
                        // Any unrecognised character represents empty space.
                        cleanRow.append(' ');
                        break;
                }
            }

            cleanRows.add(cleanRow.toString());
        }

        // A valid map must contain exactly one Pac-Man position.
        if (pacManLocation == null) {
            throw new IllegalArgumentException(
                    "The map does not contain a PacMan symbol 'P'.");
        }

        // Ensure monsters and items are within the map and not on walls.
        validateLocations(
                monsters,
                items,
                cleanRows,
                width,
                height);
        // Pac-Man starts facing East by default.
        return new GameConfiguration(
                width,
                height,
                cleanRows,
                pacManLocation,
                Location.CompassDirection.EAST, // PacMan continues to default spawn facing East
                monsters,
                items);
    }


    /**
     * Parses the map layout into individual rows.
     *
     * <p>Rows are separated by semicolons. A leading backslash and one
     * continuation space are removed from each row when present.</p>
     *
     * @param layout semicolon-separated map layout
     * @return the parsed map rows
     * @throws IllegalArgumentException if the layout is null or empty
     */
    private List<String> parseLayout(String layout) {
        if(layout == null || layout.isEmpty()) {
            throw new IllegalArgumentException("Layout cannot be null or empty");

        }

        String[] rows = layout.split(";", -1);
        List<String> result = new ArrayList<>();

        for (String row : rows) {
            result.add(removeContinuationWhitespace(row));
        }
        return result;
    }

    /**
     * Removes formatting characters introduced when a layout continues
     * across multiple lines in a properties file.
     *
     * @param row map row to clean
     * @return the cleaned map row
     */
    private String removeContinuationWhitespace(String row) {
        if(row.startsWith("\\")) {
            row = row.substring(1);
        }
        if(row.startsWith(" ")) {
            row = row.substring(1);
        }
        return row;
    }

    /**
     * Ensures that every map row has the same width as the first row.
     *
     * @param rows map rows
     * @param width expected width of each row
     * @throws IllegalArgumentException if a row has an unexpected length
     */
    private void validateRows(
            List<String> rows,
            int width) {

        for (int index = 0; index < rows.size(); index++) {
            if (rows.get(index).length() != width) {
                throw new IllegalArgumentException(
                        "Map row " + index
                                + " has length "
                                + rows.get(index).length()
                                + ", expected "
                                + width);
            }
        }
    }

    /**
     * Validates the map dimensions against the configured dimensions and the
     * permitted size range.
     *
     * @param properties configuration properties
     * @param width actual map width
     * @param height actual map height
     * @throws IllegalArgumentException if configured dimensions do not match
     *                                  the layout or are outside the range
     *                                  from 10 to 50
     */
    private void validateDimensions(
            Properties properties,
            int width,
            int height) {

        String configuredWidth =
                properties.getProperty("Map.width");

        String configuredHeight =
                properties.getProperty("Map.height");

        if (configuredWidth != null
                && Integer.parseInt(configuredWidth) != width) {
            throw new IllegalArgumentException(
                    "Map.width does not match Map.layout.");
        }

        if (configuredHeight != null
                && Integer.parseInt(configuredHeight) != height) {
            throw new IllegalArgumentException(
                    "Map.height does not match Map.layout.");
        }

        if (width < 10 || width > 50
                || height < 10 || height > 50) {
            throw new IllegalArgumentException(
                    "Map dimensions must be between 10 and 50.");
        }
    }

    /**
     * Reads and validates scripted movements for a monster.
     *
     * <p>Movement commands are read from a property such as
     * {@code Troll.0.move}. Supported commands are:</p>
     *
     * <ul>
     *     <li>{@code R} - move or turn right</li>
     *     <li>{@code L} - move or turn left</li>
     *     <li>{@code M} - move forward</li>
     *     <li>{@code S} - stay still</li>
     * </ul>
     *
     * @param properties configuration properties
     * @param type monster type name
     * @param id monster identifier
     * @return a list of validated movement commands
     * @throws IllegalArgumentException if a movement command is invalid
     */
    private List<String> readScriptedMoves(
            Properties properties,
            String type,
            int id) {

        String value = properties.getProperty(
                type + "." + id + ".move",
                "");

        // No script means the monster has no scripted movements.
        if (value.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<String> moves = new ArrayList<>();

        for (String token : value.split(",")) {
            String move = token.trim().toUpperCase();

            if (!move.equals("R")
                    && !move.equals("L")
                    && !move.equals("M")
                    && !move.equals("S")) {
                throw new IllegalArgumentException(
                        "Invalid scripted move: " + move);
            }

            moves.add(move);
        }

        return moves;
    }

    /**
     * Validates the locations of all monsters and items.
     *
     * @param monsters monsters to validate
     * @param items items to validate
     * @param rows cleaned map rows
     * @param width map width
     * @param height map height
     * @throws IllegalArgumentException if an object is outside the map or
     *                                  positioned on a wall
     */
    private void validateLocations(
            List<MonsterConfiguration> monsters,
            List<ItemConfiguration> items,
            List<String> rows,
            int width,
            int height) {

        for (MonsterConfiguration monster : monsters) {
            validateLocation(
                    monster.getLocation(),
                    rows,
                    width,
                    height,
                    "Monster " + monster.getId());
        }

        for (ItemConfiguration item : items) {
            validateLocation(
                    item.getLocation(),
                    rows,
                    width,
                    height,
                    "Item " + item.getType());
        }
    }


/**
 * Validates a single game object's location.
 *
 * @param location location to validate
 * @param rows cleaned map rows
 * @param width map width
 * @param height map height
 * @param objectName descriptive name used in error messages
 * @throws IllegalArgumentException if the location is outside the map or
 *                                  is placed on a wall
 */
    private void validateLocation(
            Location location,
            List<String> rows,
            int width,
            int height,
            String objectName) {

        if (location.getX() < 0
                || location.getX() >= width
                || location.getY() < 0
                || location.getY() >= height) {
            throw new IllegalArgumentException(
                    objectName + " is outside the map.");
        }

        if (rows.get(location.getY())
                .charAt(location.getX()) == 'x') {
            throw new IllegalArgumentException(
                    objectName + " is placed on a wall.");
        }
    }
}