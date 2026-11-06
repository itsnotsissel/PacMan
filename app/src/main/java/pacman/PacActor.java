package pacman;

import ch.aplu.jgamegrid.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class PacActor extends Actor {
    private static final int nbSprites = 4;
    private int idSprite = 0;
    private int nbPills = 0;
    private int score = 0;
    private ArrayList<Location> visitedList = new ArrayList<>();
    private List<String> propertyMoves = new ArrayList<>();
    private int propertyMoveIndex = 0;
    private GameController gameController;
    private final int listLength = 10;
    private int seed;
    private Random randomiser = new Random();
    private PacmanController pacmanController = new PacmanController(this);
    private boolean isAuto = false;

    public PacActor(GameController gameController) {
        super(true, "sprites/pacpix.gif", nbSprites);  // Rotatable
        this.gameController = gameController;
    }

    public void setAuto(boolean auto) {
        isAuto = auto;
    }

    public PacmanController getPacmanController() {
        return pacmanController;
    }

    public void setSeed(int seed) {
        this.seed = seed;
        randomiser.setSeed(seed);
    }

    public void setPropertyMoves(String propertyMoveString) {
        if (propertyMoveString != null) {
            this.propertyMoves = Arrays.asList(propertyMoveString.split(","));
        }
    }

    public boolean isAuto() {
        return isAuto;
    }

    public void act() {
        show(idSprite);
        idSprite++;
        if (idSprite == nbSprites)
            idSprite = 0;

        if (isAuto) {
            moveInAutoMode();
        }
        this.gameController.getGameCallback().pacManLocationChanged(gameController.getCurrentTick(), getLocation(), score, nbPills);
    }

    private Location closestPillLocation() {
        int currentDistance = 1000;
        Location currentLocation = null;

        List<Location> pillAndItemLocations = gameController.getPillAndItemLocations();
        for (Location location : pillAndItemLocations) {
            int distanceToPill = location.getDistanceTo(getLocation());
            if (distanceToPill < currentDistance) {
                currentLocation = location;
                currentDistance = distanceToPill;
            }
        }

        return currentLocation;
    }

    private void followPropertyMoves() {
        String currentMove = propertyMoves.get(propertyMoveIndex);
        switch (currentMove) {
            case "R":
                turn(90);
                break;
            case "L":
                turn(-90);
                break;
            case "M":
                Location next = getNextMoveLocation();
                if (canMove(next)) {
                    setLocation(next);
                    eatPill(next);
                }
                break;
        }
        propertyMoveIndex++;
    }

    private void moveInAutoMode() {
        if (propertyMoves.size() > propertyMoveIndex) {
            followPropertyMoves();
            return;
        }
        Location closestPill = closestPillLocation();
        double oldDirection = getDirection();

        Location.CompassDirection compassDir =
                getLocation().get4CompassDirectionTo(closestPill);
        Location next = getLocation().getNeighbourLocation(compassDir);
        setDirection(compassDir);
        if (!isVisited(next) && canMove(next)) {
            setLocation(next);
        } else {
            // normal movement
            int sign = randomiser.nextDouble() < 0.5 ? 1 : -1;
            setDirection(oldDirection);
            turn(sign * 90);  // Try to turn left/right
            next = getNextMoveLocation();
            if (canMove(next)) {
                setLocation(next);
            } else {
                setDirection(oldDirection);
                next = getNextMoveLocation();
                if (canMove(next)) // Try to move forward
                {
                    setLocation(next);
                } else {
                    setDirection(oldDirection);
                    turn(-sign * 90);  // Try to turn right/left
                    next = getNextMoveLocation();
                    if (canMove(next)) {
                        setLocation(next);
                    } else {
                        setDirection(oldDirection);
                        turn(180);  // Turn backward
                        next = getNextMoveLocation();
                        setLocation(next);
                    }
                }
            }
        }
        eatPill(next);
        addVisitedList(next);
    }

    private void addVisitedList(Location location) {
        visitedList.add(location);
        if (visitedList.size() == listLength)
            visitedList.remove(0);
    }

    private boolean isVisited(Location location) {
        for (Location loc : visitedList)
            if (loc.equals(location))
                return true;
        return false;
    }

    public boolean canMove(Location location) {
        Color c = getBackground().getColor(location);
        if (c.equals(Color.gray) || location.getX() >= gameController.getNumHorzCells()
                || location.getX() < 0 || location.getY() >= gameController.getNumVertCells() || location.getY() < 0)
            return false;
        else
            return true;
    }

    public int getNbPills() {
        return nbPills;
    }

    public int getScore() {
        return score;
    }

    public void eatPill(Location location) {
        gameController.processItem(location);
    }

    public void addScore(int value) {
        this.score += value;
    }

    public void addPill() {
        this.nbPills++;
    }
}