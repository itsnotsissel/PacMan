// PacManGameGrid.java
package pacman;

import ch.aplu.jgamegrid.*;
import java.util.List; // ADDED

public class PacManGameGrid
{
  // Every pill/gold/ice item outside column 4 (PacMan's default start
  // column, x=4 for the default PacMan.location=4,9) has been cleared to
  // empty space, so the default maze only has items in PacMan's own
  // column - keeps auto-mode/manual test runs deterministic and easy to
  // eyeball without a scripted move list. One of the column-4 pills
  // (row 5) is an ice cube and another (row 7) is gold. The gold is
  // deliberately NOT at PacMan's exact start cell (row 9) - eatPill
  // only fires on arrival, so an item PacMan starts standing on is
  // never actually picked up.
  private static final String DEFAULT_MAZE =
    "xxxxxxxxxxxxxxxxxxxx" + // 0
    "x   .x        x    x" + // 1
    "x xx.x xxxxxx x xx x" + // 2
    "x x .            x x" + // 3
    "x x xx xx  xx xx x x" + // 4
    "x   i  x    x      x" + // 5
    "x x xx xxxxxx xx x x" + // 6
    "x x g            x x" + // 7
    "x xx.x xxxxxx x xx x" + // 8
    "x   .x        x    x" + // 9
    "xxxxxxxxxxxxxxxxxxxx";// 10

  private int nbHorzCells;
  private int nbVertCells;
  private int[][] mazeArray;

  public PacManGameGrid(int nbHorzCells, int nbVertCells)
  {
    this.nbHorzCells = nbHorzCells;
    this.nbVertCells = nbVertCells;
    mazeArray = new int[nbVertCells][nbHorzCells];

    // Copy structure into integer array
    for (int i = 0; i < nbVertCells; i++)
    {
      for (int k = 0; k < nbHorzCells; k++) {
        int value = toInt(DEFAULT_MAZE.charAt(nbHorzCells * i + k));
        mazeArray[i][k] = value;
      }
    }
  }

  // ADDED: Feature 3 — build the maze from a parsed Map.layout instead.
  // Only walls ('x' -> 0) vs open (-> 2) matter here; pill/gold/ice
  // placement in config mode is tracked separately as ItemConfiguration
  // objects (see GameController/Game), not as special grid-cell codes -
  // ConfigurationLoader already blanks 'g'/'i'/'.' to ' ' in cleanRows for
  // exactly this reason.
  public PacManGameGrid(List<String> mazeLayout, int nbHorzCells, int nbVertCells)
  {
    this.nbHorzCells = nbHorzCells;
    this.nbVertCells = nbVertCells;
    mazeArray = new int[nbVertCells][nbHorzCells];

    for (int y = 0; y < nbVertCells; y++) {
        String row = mazeLayout.get(y);
        for (int x = 0; x < nbHorzCells; x++) {
            mazeArray[y][x] = (row.charAt(x) == 'x') ? 0 : 2;
        }
    }
  }


  public int getCell(Location location)
  {
    if (mazeArray.length <= location.y || location.y <= 0) {
      return -1;
    }
    if (mazeArray[location.y].length <= location.x || location.x <= 0) {
      return -1;
    }

    return mazeArray[location.y][location.x];
  }
  private int toInt(char c)
  {
    if (c == 'x')
      return 0;
    if (c == '.')
      return 1;
    if (c == ' ')
      return 2;
    if (c == 'g')
      return 3;
    if (c == 'i')
      return 4;
    return -1;
  }
}
