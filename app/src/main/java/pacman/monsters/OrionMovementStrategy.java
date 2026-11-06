package pacman.monsters;

import ch.aplu.jgamegrid.Location;
import pacman.PacManGameGrid;

import java.util.*;

/**
 * Implements the movement strategy for the Orion monster. Orion patrols a pre-defined
 * sequence of gold piece locations on the map using a Breadth-First Search (BFS) pathfinding algorithm.
 */
public class OrionMovementStrategy implements MovementStrategy {
    private final List<Location> patrolRoute;
    private int routeIndex = 0;

    /**
     * Constructs an OrionMovementStrategy with a given patrol route.
     *
     * @param patrolRoute the ordered list of gold locations Orion must visit
     * @throws IllegalArgumentException if the provided patrol route is null or empty
     */
    public OrionMovementStrategy(List<Location> patrolRoute) {
        if (patrolRoute == null || patrolRoute.isEmpty()) {
            throw new IllegalArgumentException("Orion needs at least one gold location to patrol.");
        }
        this.patrolRoute = patrolRoute;
    }

    /**
     * Determines the next location for the Orion monster to move toward, advancing
     * through its patrol route and utilizing BFS pathfinding to navigate safely.
     *
     * @param monster the monster instance using this strategy
     * @param grid    the current game grid layout
     * @return the next Location the monster should move to
     */
    @Override
    public Location determineMovement(Monster monster, PacManGameGrid grid) {
        Location current = monster.getLocation();
        Location target = patrolRoute.get(routeIndex);

        // Advance patrol sequence if we have reached the current target
        if (current.getX() == target.getX() && current.getY() == target.getY()) {
            routeIndex = (routeIndex + 1) % patrolRoute.size();
            target = patrolRoute.get(routeIndex);
        }

        // Pathfind safely to the target to completely avoid wall traps
        Location nextStep = getNextStepBFS(current, target, grid);
        if (nextStep != null && (nextStep.getX() != current.getX() || nextStep.getY() != current.getY())) {
            return nextStep;
        }

        return monster.getNextRandomLocation();
    }

    /**
     * Performs a Breadth-First Search (BFS) to find the shortest path from the start location
     * to the target location, avoiding walls. Uses string keys to prevent JGameGrid Location hashing errors.
     *
     * @param start  the starting location of the pathfind
     * @param target the destination location
     * @param grid   the game grid layout used to evaluate walkable cells
     * @return the immediate next Location to step onto, or null if no path is found
     */
    private Location getNextStepBFS(Location start, Location target, PacManGameGrid grid) {
        if (start.getX() == target.getX() && start.getY() == target.getY()) return start;

        Queue<Location> queue = new LinkedList<>();
        // Maps a String key "x,y" to the parent Location
        Map<String, Location> parentMap = new HashMap<>();
        Set<String> visited = new HashSet<>();

        queue.add(start);
        visited.add(start.getX() + "," + start.getY());

        boolean found = false;
        Location actualTarget = null;

        while (!queue.isEmpty()) {
            Location curr = queue.poll();
            if (curr.getX() == target.getX() && curr.getY() == target.getY()) {
                found = true;
                actualTarget = curr;
                break;
            }

            // Orion only uses 4 orthogonal directions for pathfinding
            for (Location.CompassDirection dir : new Location.CompassDirection[]{
                    Location.CompassDirection.NORTH, Location.CompassDirection.EAST,
                    Location.CompassDirection.SOUTH, Location.CompassDirection.WEST}) {

                Location neighbor = curr.getNeighbourLocation(dir);
                String key = neighbor.getX() + "," + neighbor.getY();

                if (grid.getCell(neighbor) > 0 && !visited.contains(key)) {
                    visited.add(key);
                    parentMap.put(key, curr);
                    queue.add(neighbor);
                }
            }
        }

        if (!found || actualTarget == null) return null;

        // Trace back the shortest path to find the immediate next step from start
        Location step = actualTarget;
        while (step != null) {
            String stepKey = step.getX() + "," + step.getY();
            Location parent = parentMap.get(stepKey);

            if (parent == null) break;

            if (parent.getX() == start.getX() && parent.getY() == start.getY()) {
                return step;
            }
            step = parent;
        }

        return null;
    }
}