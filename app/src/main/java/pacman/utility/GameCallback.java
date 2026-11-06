package pacman.utility;

import ch.aplu.jgamegrid.Location;
import pacman.monsters.Monster;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Please do not change this class. This class is used for testing and your code needs to print the correct output to pass the test
 */
public class GameCallback {
    private String logFilePath = "Log.txt";
    private FileWriter fileWriter = null;
    private StringBuilder logBuilder = new StringBuilder();

    public GameCallback() {
        try {
            fileWriter = new FileWriter(new File(logFilePath));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void writeString(String str) {
        logBuilder.append(str).append("\n");
        try {
            fileWriter.write(str);
            fileWriter.write("\n");
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Full event log accumulated over the run, for JUnit tests to assert against.
     */
    public String getAllLog() {
        return logBuilder.toString();
    }

    public void endOfGame(String gameResult) {
        writeString(gameResult);
    }

    public void pacManLocationChanged(int tick, Location pacmanLocation, int score, int nbPills) {
        String pacmanLocationString = String.format("[Tick %d][PacMan] Location: %d-%d. Score: %d. Pills: %d", tick,
                pacmanLocation.getX(), pacmanLocation.getY(), score, nbPills);
        writeString(pacmanLocationString);
    }

    public void monsterLocationChanged(int tick, Monster monster) {
        String monsterLocationString = String.format("[Tick %d][%s-%d] Location: %d-%d. State: %s", tick, monster.getType(), monster.getId(),
                monster.getLocation().getX(), monster.getLocation().getY(), monster.getState());
        writeString(monsterLocationString);
    }

    public void pacManEatPillsAndItems(int tick, Location pacmanLocation, String type) {
        String pillOrItemLocationString = String.format("[Tick %d][PacMan] Location: %d-%d. Eat Pill/Item: %s", tick,
                pacmanLocation.getX(), pacmanLocation.getY(), type);
        writeString(pillOrItemLocationString);
    }

    /**
     * Feature 2 (item effects): one line fired exactly when a fury/freeze
     * effect starts, separate from the pacManEatPillsAndItems pickup line,
     * so effect onset is directly greppable rather than inferred from
     * monster State changes on the following lines.
     */
    public void effectTriggered(int tick, String effectType, String cause, Location triggerLocation) {
        String effectString = String.format("[Tick %d][Effect] %s triggered by PacMan eating %s at %d-%d", tick,
                effectType, cause, triggerLocation.getX(), triggerLocation.getY());
        writeString(effectString);
    }
}
