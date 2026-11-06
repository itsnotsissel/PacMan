import org.junit.Test;
import pacman.Game;
import pacman.utility.GameCallback;
import pacman.utility.PropertiesLoader;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 5 automated tests spanning the 3 spec section 1.2 features, following the
 * properties-in / logResult-out pattern (see Game.runApp()). Each test's
 * properties file drives PacMan with a fully scripted (non-random) move
 * script, so the resulting log is deterministic - assertion strings below
 * are copied from real logResult output, not hand-derived (see each
 * testN.properties file's own comments for the full input/expected-output/
 * why breakdown behind that test's assertions).
 */
public class PacManTest {

    private String runGame(String propertiesFile) {
        Properties properties = PropertiesLoader.loadPropertiesFile(propertiesFile);
        GameCallback gameCallback = new GameCallback();
        Game game = new Game(gameCallback, properties);
        return game.runApp();
    }

    /**
     * Asserts that the log line starting with tickActorPrefix (e.g.
     * "[Tick 59][Wizard-0]") contains expectedSubstring, without pinning
     * the rest of that line (in particular its Location: X-Y) - for
     * checks on a monster that's genuinely unscripted at that tick (its
     * own real, seeded-Random-driven walkApproach()/onFrozenTick()), so
     * *that* it moved/logged is deterministic but *where* isn't
     * something a test should hard-code (would break under a different
     * seed even though the underlying behaviour is correct).
     */
    private void assertTickLineContains(String logResult, String tickActorPrefix, String expectedSubstring) {
        int lineStart = logResult.indexOf(tickActorPrefix);
        assertTrue("Expected a log line starting with \"" + tickActorPrefix + "\"", lineStart >= 0);
        int lineEnd = logResult.indexOf('\n', lineStart);
        String line = lineEnd >= 0 ? logResult.substring(lineStart, lineEnd) : logResult.substring(lineStart);
        assertTrue("Expected \"" + line + "\" to contain \"" + expectedSubstring + "\"", line.contains(expectedSubstring));
    }

    /**
     * test1: baseline (spec section 1.1 only, all 3 feature flags off).
     * Regression anchor - must keep passing through every later refactor.
     * PacMan is fully scripted to eat every pickup in the default maze's
     * column 4 (a BFS-computed 37-move route from a non-item start cell,
     * see test1.properties's comment for why PacMan.location moved off
     * (4,9)) and win outright, while Troll is scripted to make its
     * one and only regression-relevant move (proving it acts from tick 1,
     * no startup idle) then park somewhere PacMan's route never reaches -
     * so the win is deterministic on every seed, not just the current one.
     */
    @Test(timeout = 90000)
    public void test1Baseline() {
        String logResult = runGame("properties/test1.properties");
        System.out.println("logResult = " + logResult);

        assertTrue(logResult.contains("[Tick 12][PacMan] Location: 4-7. Eat Pill/Item: gold"));
        assertTrue(logResult.contains("[Tick 12][PacMan] Location: 4-7. Score: 5. Pills: 1"));
        assertTrue(logResult.contains("[Tick 14][PacMan] Location: 4-8. Eat Pill/Item: pills"));
        assertTrue(logResult.contains("[Tick 15][PacMan] Location: 4-9. Eat Pill/Item: pills"));
        assertTrue(logResult.contains("[Tick 26][PacMan] Location: 4-5. Eat Pill/Item: ice"));
        // Ice doesn't increment the pill counter, only pills/gold do - stays
        // at 3 (unchanged from the pill pickup at tick 15), not 4.
        assertTrue(logResult.contains("[Tick 26][PacMan] Location: 4-5. Score: 7. Pills: 3"));
        assertTrue(logResult.contains("[Tick 34][PacMan] Location: 4-3. Eat Pill/Item: pills"));
        assertTrue(logResult.contains("[Tick 36][PacMan] Location: 4-2. Eat Pill/Item: pills"));
        // Final pickup (the 5th pill): nbPills reaches 6 (5 pills + 1
        // gold), meeting this maze's maxPillsAndItems exactly - the game
        // ends in "YOU WIN" right after this tick.
        assertTrue(logResult.contains("[Tick 37][PacMan] Location: 4-1. Eat Pill/Item: pills"));
        assertTrue(logResult.contains("[Tick 37][PacMan] Location: 4-1. Score: 10. Pills: 6"));

        // Troll moves from tick 1 - no startup idle.
        assertTrue(logResult.contains("[Tick 1][Troll-0]"));

        assertFalse(logResult.contains("[Effect]"));
        assertFalse(logResult.contains("State: Furious"));
        assertFalse(logResult.contains("State: Frozen"));
        assertFalse(logResult.contains("[Orion]"));
        assertFalse(logResult.contains("[Wizard]"));

        assertTrue(logResult.contains("YOU WIN"));
    }

    /**
     * test2: Feature 1 (new monster types: Orion + Wizard), isolated from
     * Feature 2 and Feature 3. 3 gold pieces (18,1)/(18,7)/(13,7) exercise
     * Orion's fixed patrol sequence end-to-end: visiting each in order,
     * still visiting one PacMan has already eaten, and starting the cycle
     * over once the sequence is exhausted. Troll.0.move holds Troll
     * still for the whole test (and Wizard.0.move does the same through
     * tick 150, after its own 2-tick wall-phase demo below), so Orion is
     * the only monster actually moving during that window - free to run
     * its own real walkApproach() logic (unscripted) for as long as it
     * takes to complete a full patrol lap, with no collision risk to
     * guard against. After that hold, Wizard.0.move resumes with a
     * deliberate, BFS-computed walk onto PacMan's own resting cell
     * (7,7), ending the game as a scripted "GAME OVER" on a known tick
     * rather than by chance (which is what used to happen once its
     * script ran dry and its own random walk wandered there).
     */
    @Test(timeout = 90000)
    public void test2Feature1NewMonsterTypes() {
        String logResult = runGame("properties/test2.properties");
        System.out.println("logResult = " + logResult);

        // PacMan eats the 3rd gold piece (13,7) early - before Orion ever
        // gets there - then retreats west to (7,7), out of Orion's entire
        // patrol loop (see test2.properties's comment)
        assertTrue(logResult.contains("[Tick 2][PacMan] Location: 13-7. Eat Pill/Item: gold"));

        // Orion visits every gold piece in its patrol, at some point.
        // Deliberately NOT pinned to a specific tick: even with every
        // other monster held still, Orion's own random fallback on the
        // wall-blocked (18,7)->(13,7) leg (Monster.getNextRandomLocation(),
        // no memory across ticks) can still take a variable number of
        // ticks to escape depending on the exact draws from its seeded
        // Random - deterministic per run, but not something a test
        // should depend on. We don't want to depend on the seed's
        // determinism here, so these checks are tick-independent.
        String gold1 = "[Orion-0] Location: 18-1. State: Normal";
        String gold2 = "[Orion-0] Location: 18-7. State: Normal";
        // Orion still visits the 3rd gold piece even though PacMan
        // already ate it - per spec: "Even though the gold pieces are
        // eaten by PacMan, Orion still will visit those locations"
        String gold3 = "[Orion-0] Location: 13-7. State: Normal";
        assertTrue(logResult.contains(gold1));
        assertTrue(logResult.contains(gold2));
        assertTrue(logResult.contains(gold3));

        // Sequence exhausted - Orion visits the 1st gold piece a 2nd
        // time, per spec: "When all gold pieces have been visited, Orion
        // will revisit the golds based on the same sequence". Checked as
        // 2+ distinct occurrences (first index != last index), not just
        // logResult.contains(gold1) again - that would trivially pass
        // off the very first visit and never actually prove a revisit
        // happened.
        assertTrue(logResult.indexOf(gold1) != logResult.lastIndexOf(gold1));


        // Wizard.0.move (test2.properties) faces the Wizard directly at a
        // known wall and tells it to move forward into it - "R" turns it
        // to face East from its relocated spawn (1,6), toward the
        // confirmed wall cell (2,6); "M" then attempts the move.
        // Monster.applyScriptedMove() consults the same
        // Monster.tryWallPhase() hook Wizard.walkApproach() uses (every
        // other monster type's default is "no wall-phasing ability"), so
        // this single scripted move produces a genuine phase-through -
        // landing at (3,6), the cell just beyond the wall -
        // deterministically, on a tick this test chose, rather than
        // waiting for Wizard's own random neighbour-picking to land on
        // it by chance.
        assertTrue(logResult.contains("[Tick 2][Wizard-0] Location: 3-6. State: Normal"));

        assertFalse(logResult.contains("[Effect]"));
        assertFalse(logResult.contains("State: Furious"));
        assertFalse(logResult.contains("State: Frozen"));

        // PacMan's own scripted route (PacMan.move) ends with it holding
        // still at (7,7) from tick 10 onward. Once Wizard's "S" hold
        // expires (tick 150 - a generous margin over Orion's typical
        // ~35-tick lap time, to absorb real run-to-run variance now that
        // no "seed" property pins Orion's random fallback), Wizard.0.move's
        // remaining tokens (R,M,L,M,M,M,M) walk it there via a BFS-computed
        // route (3,6)->(3,7)->(4,7)->(5,7)->(6,7)->(7,7), landing exactly on
        // PacMan's cell at tick 156 - a deliberate scripted kill, not one
        // left to Wizard's own random walk to stumble into eventually.
        // Wizard's own script consumption is unaffected by Orion's
        // randomness, so this tick is 100% reproducible run to run
        // (confirmed via repeated real runs), even though Orion's own
        // path never is.
        assertTrue(logResult.contains("[Tick 156][Wizard-0] Location: 7-7. State: Normal"));
        assertTrue(logResult.contains("GAME OVER"));
    }

    /**
     * test3: Feature 2 (item effects: fury/freeze), isolated from Feature 1
     * and Feature 3 - only Troll is present.
     * Troll.0.move scripts Troll's movement - same mechanism as
     * test2.properties's Orion.0.move. A scripted move is consumed every
     * tick even during an effect (idle is the only exception): during
     * fury, "M" moves 2 cells instead of 1, matching the fury rule, so a
     * script can drive real fury-window movement directly; during
     * freeze, the instruction is consumed but never applied - the
     * monster stays put regardless of what it says. Troll.0.move covers
     * the entire fury window (ticks 15-19) with 5 clean, unblocked
     * 2-cell jumps along a long open corridor (row 3), then - unlike an
     * earlier version of this test, which just parked Troll silently
     * for the rest of the run - walks it further (ticks 20-27, real
     * Normal-state movement) before finally parking it somewhere safe
     * ahead of the freeze window. PacMan's own fully-scripted 39-tick
     * route (covering all 7 pickup items in this maze) never coincides
     * with any of Troll's cells at the same tick (confirmed via a
     * Python cross-check), so the run deterministically ends in "YOU
     * WIN" on any seed. Also covers spec 3d.3: eating a 2nd gold piece
     * while frozen must not make the monster furious.
     */
    @Test(timeout = 90000)
    public void test3Feature2ItemEffects() {
        String logResult = runGame("properties/test3.properties");
        System.out.println("logResult = " + logResult);

        assertTrue(logResult.contains("[Tick 14][Effect] Fury triggered by PacMan eating gold at 4-7"));

        // Furious monsters move 2 cells per tick, not 1 - per spec:
        // "move towards that direction for 2 cells if they can." Troll
        // is under script control the whole fury window (Troll.0.move's
        // "M"s, after turning to face west via a single "L" at tick 1) -
        // Troll.location=14,3 sits on a 14-cell-wide open corridor (row
        // 3, x=3..16) with no internal walls, so BOTH the 1st and 2nd
        // furious jumps are clean, unblocked 2-cell moves (14,3->12,3,
        // then 12,3->10,3).
        assertTrue(logResult.contains("[Tick 15][Troll-0] Location: 12-3. State: Furious"));
        assertTrue(logResult.contains("[Tick 16][Troll-0] Location: 10-3. State: Furious"));

        // The corridor is long enough for the script to keep driving
        // clean 2-cell jumps for the rest of the fury window too.
        assertTrue(logResult.contains("[Tick 17][Troll-0] Location: 8-3. State: Furious"));
        assertTrue(logResult.contains("[Tick 18][Troll-0] Location: 6-3. State: Furious"));
        assertTrue(logResult.contains("[Tick 19][Troll-0] Location: 4-3. State: Furious"));

        assertTrue(logResult.contains("[Tick 22][Troll-0] Location: 5-3. State: Normal"));
        assertTrue(logResult.contains("[Tick 23][Troll-0] Location: 6-3. State: Normal"));
        assertTrue(logResult.contains("[Tick 24][Troll-0] Location: 7-3. State: Normal"));
        assertTrue(logResult.contains("[Tick 25][Troll-0] Location: 8-3. State: Normal"));
        assertTrue(logResult.contains("[Tick 26][Troll-0] Location: 9-3. State: Normal"));
        assertTrue(logResult.contains("[Tick 27][Troll-0] Location: 10-3. State: Normal"));

        assertTrue(logResult.contains("[Tick 28][Effect] Freeze triggered by PacMan eating ice at 4-5"));

        // Spec 3d.3: "While the monsters are frozen, PacMan can eat a
        // gold piece without making any monsters (including the
        // Wizard) furious." A 2nd gold piece (5,5) is eaten at tick 29,
        // the very first tick of the freeze window. The [Effect] Fury
        // line still logs (it just records that PacMan interacted with
        // gold), but Monster.angerMonster() no-ops for an already-Frozen
        // monster, so this must NOT interrupt the freeze silence below.
        assertTrue(logResult.contains("[Tick 29][PacMan] Location: 5-5. Eat Pill/Item: gold"));
        assertTrue(logResult.contains("[Tick 29][Effect] Fury triggered by PacMan eating gold at 5-5"));

        // 3 ticks of silence (29/30/31) while frozen.
        assertFalse(logResult.contains("[Tick 29][Troll-0]"));
        assertFalse(logResult.contains("[Tick 30][Troll-0]"));
        assertFalse(logResult.contains("[Tick 31][Troll-0]"));

        // Proves the 2nd gold pickup (while frozen) really didn't make
        // Troll furious, not just that it stayed silent during the
        // freeze itself.
        assertFalse(logResult.contains("[Tick 35][Troll-0]"));
        assertFalse(logResult.contains("[Tick 39][Troll-0]"));

        // PacMan.move (test3.properties) is fully scripted for its
        // entire 39-tick route and covers all 7 pickup items in this
        // maze (5 pills + 2 golds; the ice doesn't count towards the
        // win condition) - combined with Troll never moving again after
        // tick 31, the run deterministically ends in "YOU WIN" every
        // time, regardless of run (no "seed" property is set - PacMan
        // and every monster use genuinely unseeded randomness, but
        // nothing here actually depends on it since both actors are
        // fully scripted).
        assertTrue(logResult.contains("[Tick 39][PacMan] Location: 4-1. Eat Pill/Item: pills"));
        assertTrue(logResult.contains("YOU WIN"));
    }

    /**
     * test4: Feature 3 (configurable map), isolated from Feature 1 and
     * Feature 2 - no gold/ice placed at all. The map has 3 "T" markers
     * (up from the original 1), so MonsterCreator's Feature-3 map-marker
     * path is exercised for multiple instances of the same monster type:
     * Troll-0/1/2, indexed in row-major scan order - (7,7), (1,8),
     * (8,8). Troll-0.move opens with the original turn-then-move demo
     * (making the marker parsing directly checkable: a monster only
     * logs a location after it actually moves, never at spawn, so the
     * log line after a single scripted move is the most direct way to
     * confirm the marker was parsed at the right coordinate), then all
     * 3 Trolls are scripted to converge on PacMan's own resting cell
     * (1,7) once its scripted route finishes, ending the game as a
     * deliberate, fully scripted "GAME OVER" (see test4.properties's
     * comment for the full per-Troll route and why each is safe from
     * colliding with PacMan before the intended kill tick).
     */
    @Test(timeout = 90000)
    public void test4Feature3ConfigurableMap() {
        String logResult = runGame("properties/test4.properties");
        System.out.println("logResult = " + logResult);

        assertTrue(logResult.contains("[Tick 1][PacMan] Location: 2-1. Eat Pill/Item: pills"));
        assertTrue(logResult.contains("[Tick 7][PacMan] Location: 8-1. Score: 7. Pills: 7"));

        assertTrue(logResult.contains("[Tick 2][Troll-0] Location: 6-7. State: Normal"));

        // PacMan's own scripted route (PacMan.move) finishes and holds
        // still at (1,7) from tick 22 on. All 3 Trolls then converge on
        // it at tick 31, from 3 different directions and starting
        // points (Troll-0 down column 1 from its safe holding spot at
        // (1,4); Troll-1 a single step north out of its (1,8) dead-end
        // pocket; Troll-2 west along the now-empty row 7 from its (8,8)
        // dead-end pocket) - a deliberate 3-way scripted kill, not a
        // random one.
        assertTrue(logResult.contains("[Tick 31][Troll-0] Location: 1-7. State: Normal"));
        assertTrue(logResult.contains("[Tick 31][Troll-1] Location: 1-7. State: Normal"));
        assertTrue(logResult.contains("[Tick 31][Troll-2] Location: 1-7. State: Normal"));

        assertFalse(logResult.contains("[Effect]"));
        assertFalse(logResult.contains("State: Furious"));
        assertFalse(logResult.contains("State: Frozen"));
        assertFalse(logResult.contains("[Orion]"));
        assertFalse(logResult.contains("[Wizard]"));

        assertTrue(logResult.contains("GAME OVER"));
    }

    /**
     * test5: all 3 features combined, redesigned around 3 phases (see
     * test5.properties's comment for the full breakdown) rather than
     * scripting every monster including Orion: Orion.0.move is
     * deliberately left UNSET here, unlike an earlier version of this
     * test - it runs its own real, unscripted walkApproach() patrol for
     * the whole run, same as test2Feature1NewMonsterTypes. Every other
     * actor (PacMan, both Trolls, Wizard) is instead scripted precisely
     * enough to structurally guarantee it can never collide with either
     * PacMan or Orion (verified, as always this session, via a Python
     * BFS/cross-check before being committed, then confirmed against
     * this test's own real log output).
     *
     * Phase 1 (ticks 1-10): PacMan eats a gold (tick 5), triggering
     * fury; Troll-0 and Wizard both demonstrate the fury 2-cell/1-
     * cell/0-cell movement pattern during the resulting window (ticks
     * 6-10). Wizard also demonstrates its wall-phase ability, at tick 1
     * (a deliberate deviation from a literal "phase through a wall
     * during the fury window" - see test5.properties's comment for why
     * that specific combination isn't something the engine can
     * produce: Monster.moveTwoCellsIfPossible(), used by every furious
     * "M", never consults tryWallPhase() at all).
     *
     * Phase 2 (ticks 11-13): PacMan eats an ice cube (tick 10, the last
     * fury tick), triggering freeze. Both Trolls go fully silent (as
     * established since test3); Wizard's freeze-immunity alternation
     * still produces 1-2 genuinely random moves, asserted via
     * assertTickLineContains (State only); Orion - unscripted, but
     * still a monster - is frozen too (freezeMonsters() applies to
     * every monster; only Wizard overrides the resulting silence), so
     * it also goes fully silent for these 3 ticks, checked the same way
     * as the Trolls.
     *
     * Phase 3 (ticks 14-83): PacMan and Troll-0 finish retreating to
     * fixed resting cells and hold there; Wizard holds wherever its own
     * random freeze-drift left it; Orion runs its real patrol logic the
     * whole time, visiting every gold piece on the map (including the
     * one PacMan itself already ate) with zero collision risk, mirroring
     * test2Feature1NewMonsterTypes's own "everyone else held safely
     * away" containment strategy. Troll-1, held still through tick 70,
     * then walks a scripted BFS route from its own spawn to PacMan's
     * resting cell (1,1), landing on it at tick 83 for a deterministic
     * "GAME OVER" - the one actor in this test that isn't just parked
     * for containment, but a real scripted kill.
     */
    @Test(timeout = 90000)
    public void test5AllFeaturesCombined() {
        String logResult = runGame("properties/test5.properties");
        System.out.println("logResult = " + logResult);

        // Wizard.0.move's opening scripted "M" (test5.properties) faces
        // directly into the wall cell just north of its spawn (12,7)
        // and proves the wall-phase ability deterministically on tick 1
        // - a full tick before PacMan even reaches the gold below -
        // landing at (12,5), the cell just beyond the wall at (12,6).
        assertTrue(logResult.contains("[Tick 1][Wizard-0] Location: 12-5. State: Normal"));
        assertFalse(logResult.contains("[Tick 1][Wizard-0] Location: 12-7"));

        // First (and only, in this test) gold eaten at tick 5,
        // triggering fury for every monster, including Orion (real,
        // unscripted, but still a monster).
        assertTrue(logResult.contains("[Tick 5][PacMan] Location: 4-2. Eat Pill/Item: gold"));
        assertTrue(logResult.contains("[Tick 5][Effect] Fury triggered by PacMan eating gold at 4-2"));

        // Troll-0 (turned to face the map's east border wall at tick 1,
        // held until the trigger) has 5 open cells of road before that
        // wall - its scripted "M"s during the fury window (ticks 6-10)
        // land 2, 2, 1, 0, 0 cells each: all 3 possible fury-jump
        // outcomes from a single monster.
        assertTrue(logResult.contains("[Tick 6][Troll-0] Location: 9-4. State: Furious"));
        assertTrue(logResult.contains("[Tick 7][Troll-0] Location: 11-4. State: Furious"));
        assertTrue(logResult.contains("[Tick 8][Troll-0] Location: 12-4. State: Furious"));
        assertTrue(logResult.contains("[Tick 9][Troll-0] Location: 12-4. State: Furious"));
        assertTrue(logResult.contains("[Tick 10][Troll-0] Location: 12-4. State: Furious"));

        // Wizard (turned to face west after its tick-1 wall-phase demo,
        // held until the same trigger) is on row 5, blocked earlier by
        // the (5,5)-(7,5) wall block - its own fury "M"s land 2, 2, 0,
        // 0, 0 cells: the same mechanism, a different blocking point,
        // still furious the whole time.
        assertTrue(logResult.contains("[Tick 6][Wizard-0] Location: 10-5. State: Furious"));
        assertTrue(logResult.contains("[Tick 7][Wizard-0] Location: 8-5. State: Furious"));
        assertTrue(logResult.contains("[Tick 8][Wizard-0] Location: 8-5. State: Furious"));
        assertTrue(logResult.contains("[Tick 10][Wizard-0] Location: 8-5. State: Furious"));

        // Ice eaten at tick 10 (the fury window's own last tick),
        // triggering freeze for ticks 11-13.
        assertTrue(logResult.contains("[Tick 10][PacMan] Location: 2-3. Eat Pill/Item: ice"));
        assertTrue(logResult.contains("[Tick 10][Effect] Freeze triggered by PacMan eating ice at 2-3"));

        // Both Trolls go fully silent for the entire freeze window -
        // both scripts say "S" this early anyway (freeze burns it
        // regardless of content); Troll-1 doesn't start its own scripted
        // walk toward PacMan until tick 71 (see test5.properties's
        // comment).
        assertFalse(logResult.contains("[Tick 11][Troll-0]"));
        assertFalse(logResult.contains("[Tick 12][Troll-0]"));
        assertFalse(logResult.contains("[Tick 13][Troll-0]"));
        assertFalse(logResult.contains("[Tick 11][Troll-1]"));
        assertFalse(logResult.contains("[Tick 12][Troll-1]"));
        assertFalse(logResult.contains("[Tick 13][Troll-1]"));

        // Orion is unscripted but still a monster - freezeMonsters()
        // applies to it exactly like every other monster, and it has
        // no freeze-immunity override of its own (only Wizard does), so
        // it goes fully silent too: its own continuous per-tick patrol
        // logging (visible everywhere else in this log) stops dead for
        // these 3 ticks, the same silence check used for the Trolls.
        assertFalse(logResult.contains("[Tick 11][Orion-0]"));
        assertFalse(logResult.contains("[Tick 12][Orion-0]"));
        assertFalse(logResult.contains("[Tick 13][Orion-0]"));

        // Wizard's freeze-immunity alternation (Wizard.onFrozenTick())
        // is never scriptable - freeze always burns the scripted
        // instruction without applying it - so which tick it logs on is
        // deterministic (a simple boolean toggle) but which cell it
        // moves to is a real, seeded-Random draw via its own
        // walkApproach(). Check the State only, not the exact Location.
        assertTickLineContains(logResult, "[Tick 11][Wizard-0]", "State: Frozen");
        assertTickLineContains(logResult, "[Tick 13][Wizard-0]", "State: Frozen");

        // Phase 3: PacMan finishes retreating to (1,1) - its own start
        // cell, the map's corner, as far from Orion's gold-patrol loop
        // as this map allows - by tick 15 and holds there; Troll-0
        // finishes retreating to (2,8) by tick 30 and holds; Wizard
        // holds wherever its own random freeze-drift left it. None of
        // these resting cells was ever on Orion's own travelled path
        // at any point before tick 83 (confirmed via this test's own
        // real log output, not hand-predicted) - so the only
        // collision-driven "GAME OVER" in this run is Troll-1's own
        // scripted one below, not an accidental one.
        int tick70Index = logResult.indexOf("[Tick 70]");
        assertTrue("Expected \"[Tick 70]\" to appear in the log", tick70Index >= 0);
        assertFalse(logResult.substring(0, tick70Index).contains("GAME OVER"));

        // Feature 1: Orion, running its own real patrol logic this
        // whole time (no script at all), visits every gold piece on the
        // map at some point during the run - including (4,2), the one
        // PacMan itself already ate - per spec: "Even though the gold
        // pieces are eaten by PacMan, Orion still will visit those
        // locations". Tick-independent, matching
        // test2Feature1NewMonsterTypes's own established style, since
        // Orion's exact tick-by-tick path is genuinely RNG-dependent.
        assertTrue(logResult.contains("[Orion-0] Location: 4-2. State: Normal"));
        assertTrue(logResult.contains("[Orion-0] Location: 10-2. State: Normal"));
        assertTrue(logResult.contains("[Orion-0] Location: 11-7. State: Normal"));

        // PacMan settles at (1,1) by tick 15 and never moves again -
        // Troll-1, held still through tick 70, then walks a scripted BFS
        // route (test5.properties's comment) from its own spawn (6,7) to
        // that resting cell, landing on it at tick 83 for a real,
        // deterministic kill - not PacMan's own auto-pilot wandering
        // into a monster once its script runs out, as in an earlier
        // version of this test.
        assertTrue(logResult.contains("[Tick 15][PacMan] Location: 1-1"));
        assertTrue(logResult.contains("[Tick 83][Troll-1] Location: 1-1. State: Normal"));
        assertTrue(logResult.contains("[Tick 83][PacMan] Location: 1-1"));
        assertTrue(logResult.contains("GAME OVER"));
    }
}