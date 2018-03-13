package cse417;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Program to find the lineup that has the highest expected points, subject to
 * constraints on the total budget, numbers at each position, and number from
 * one team.
 */
public class OptimalLineup {

  // Records the budget available for players.
  private final int budget;

  // Stores the list of all players.
  private final List<Player> allPlayers;

  // Stores the list of MIDs and FWDs in *decreasing order* by points.
  private final List<Player> attackers;

  // Records the best expected points of any feasible lineup seen so far.
  private float bestPoints = Float.NEGATIVE_INFINITY;

  // Records the best feasible lineup seen so far.
  private Lineup bestLineup;

  // Records the max points possible at GK and DEF for each price.
  private float[] bestPointsDefense;

  // Records the GKs and DEFs that achieve the points in bestPointsDefense.
  private Player[][] bestPlayersDefense;

  /** Prepares to compute the optimal lineup with the given parameters. */
  private OptimalLineup(List<Player> allPlayers, int budget) {
    this.budget = budget;

    this.allPlayers = new ArrayList<Player>(allPlayers);
    this.attackers = allPlayers.stream()
        .filter(p -> p.atPosition(Position.MID) || p.atPosition(Position.FWD))
        .collect(Collectors.toList());
    Collections.sort(this.attackers,
        (p,q) -> -Double.compare(p.getPoints(), q.getPoints()));

    this.bestPointsDefense = new float[budget+1];
    this.bestPlayersDefense = new Player[budget+1][];
  }

  /** Returns the optimal lineup or null if no feasible lineups exist. */
  public Lineup compute() {
    // Find the best options at defense.
    computeBestDefenseByPrice();

    // Recursively search for ways to fill out the attacking lineup.
    // Note: we set the team limit to 15 to turn off checking teams.
    int count = tryAttackingLineups(new Lineup(budget, 15), 0);
    System.err.printf("Explored %d partial lineups of attackers\n", count);
    System.err.println();

    return bestLineup;
  }

  /** Computes the best defense for each possible price. */
  private void computeBestDefenseByPrice() {

    // Compute the most points achievable at GK for each price by brute force.
    // We start with every price set to -infinity points, i.e., impossible.
    float[] bestPointsGK = new float[budget+1];
    Arrays.fill(bestPointsGK, Float.NEGATIVE_INFINITY);
    Player[][] bestPlayersGK = new Player[budget+1][];

    // Try every combination and see if it improves upon the best at that price
    List<Player> keepers = allPlayers.stream()
        .filter(p -> p.atPosition(Position.GK)).collect(Collectors.toList());
    int countGK = 0;
    for (int i = 0; i < keepers.size(); i++) {
      for (int j = i+1; j < keepers.size(); j++) {
        Lineup lineup = new Lineup(budget);
        lineup.add(keepers.get(i));
        lineup.add(keepers.get(j));

        float points = lineup.getExpectedPoints();
        int price = lineup.getTotalPrice();
        if (points > bestPointsGK[price]) {
          bestPointsGK[price] = points;
          bestPlayersGK[price] =
              lineup.getPlayers().toArray(new Player[lineup.size()]);
        }
        countGK++;
      }
    }
    System.err.printf("Tried %d goalkeeper combinations.\n", countGK);

    // Replace each entry with the best seen at that price or lower.
    fillMaxDownward(bestPointsGK, bestPlayersGK, false);

    // Compute the most points achievable at DEF for each price by brute force.
    // We use the same approach here as above.

    float[] bestPointsDEF = new float[budget+1];
    Arrays.fill(bestPointsDEF, Float.NEGATIVE_INFINITY);
    Player[][] bestPlayersDEF = new Player[budget+1][];

    List<Player> defenders = allPlayers.stream()
        .filter(p -> p.atPosition(Position.DEF)).collect(Collectors.toList());

    int countDEF = 0;
    
    for (int i = 0; i < defenders.size(); i++) {
        for (int j = i+1; j < defenders.size(); j++) {
        	for (int k = j + 1; k < defenders.size(); k++) {
        		for (int l = k + 1; l < defenders.size(); l++) {
        			for (int n = l + 1; n < defenders.size(); n++) {
        				Lineup lineup = new Lineup(budget);
						lineup.add(defenders.get(i));
						lineup.add(defenders.get(j));
						lineup.add(defenders.get(k));
						Player fourth = defenders.get(l);
						Player fifth = defenders.get(n);
						if (!lineup.canAdd(fourth)) {
							break;
						}
						lineup.add(fourth);
						if (lineup.canAdd(fifth)) {
							lineup.add(fifth);
							float points = lineup.getExpectedPoints();
							int price = lineup.getTotalPrice();
							if (points > bestPointsDEF[price]) {
								bestPointsDEF[price] = points;
							    bestPlayersDEF[price] =
							        lineup.getPlayers().toArray(new Player[lineup.size()]);
							}
							countDEF++;
						}
        			}
        		}
        	}
        }
      }

    System.err.printf("Tried %d defender combinations.\n", countDEF);

    fillMaxDownward(bestPointsDEF, bestPlayersDEF, true);

    // Fill in the best defense with the best combination of GK and DEF.
    Arrays.fill(bestPointsDefense, Float.NEGATIVE_INFINITY);
    for (int budgetGK = 0; budgetGK <= budget; budgetGK++) {
      for (int budgetDEF = 0; budgetDEF <= budget - budgetGK; budgetDEF++) {
        if (bestPointsGK[budgetGK] + bestPointsDEF[budgetDEF] > 
            bestPointsDefense[budgetGK + budgetDEF]) {
          bestPointsDefense[budgetGK + budgetDEF] =
              bestPointsGK[budgetGK] + bestPointsDEF[budgetDEF];
          bestPlayersDefense[budgetGK + budgetDEF] = new Player[] {
              bestPlayersGK[budgetGK][0], bestPlayersGK[budgetGK][1],
              bestPlayersDEF[budgetDEF][0], bestPlayersDEF[budgetDEF][1],
              bestPlayersDEF[budgetDEF][2], bestPlayersDEF[budgetDEF][3],
              bestPlayersDEF[budgetDEF][4] };
        }
      }
    }
  }

  /** Replaces each element with the max over every equal or lower index. */
  private static void fillMaxDownward(
      float[] points, Player[][] players, boolean verbose) {
    float maxPoints = points[0];
    Player[] maxPlayers = players[0];
    for (int i = 1; i < points.length; i++) {
      if (maxPoints > points[i]) {
        points[i] = maxPoints;
        players[i] = maxPlayers;
      } else {
        if (verbose && points[i] > maxPoints)
          System.err.printf("*** %3d %5.2f\n", i, points[i]);
        maxPoints = points[i];
        maxPlayers = players[i];
      }
    }
  }

  /**
   * Tries all ways of filling in the missing attacking players in the given
   * lineup using the players in allPlayers[start..] that might achieve more
   * expected points than {@code bestPoints}.
   * @returns the number of partial lineups explored
   */
  private int tryAttackingLineups(Lineup lineup, int start) {
	  if (start == this.attackers.size()) {
		  return 1;
	  }
	  int budgetLeft = lineup.getRemainingBudget();
	  LineupBound attackBound = new LineupBound(lineup, 0, 0, 0, 
			  Lineup.POSITION_LIMIT.get(Position.MID) - lineup.sizeByPosition(Position.MID),
			  Lineup.POSITION_LIMIT.get(Position.FWD) - lineup.sizeByPosition(Position.FWD));
	  float completeUpperBound = bestPointsDefense[budgetLeft] + attackBound.maxPoints(
			  				this.attackers.subList(start, this.attackers.size()).iterator());
	  if (completeUpperBound - bestPoints < 1e-5) {
		  return 1;
	  }
	  Player[] bestDef = bestPlayersDefense[budgetLeft];
	  if (lineup.size() == 8 && budget >= lineup.getTotalPrice() && bestDef != null) {
		  for (Player defPlayer : bestDef) {
			  lineup.add(defPlayer);
		  }
		  float lineupPoints = lineup.getExpectedPoints();
		  if (bestPoints < lineupPoints) {
			  bestLineup = lineup;
			  bestPoints = lineupPoints;
		  }
		  return 1;
	  }
	  Player attacker = this.attackers.get(start);
	  if (lineup.canAdd(attacker)) {
		  Lineup newLineup = lineup.clone();
		  newLineup.add(attacker);
		  return 1 + tryAttackingLineups(newLineup, start + 1) +
				 tryAttackingLineups(lineup, start + 1);
	  }
	  return 1 + tryAttackingLineups(lineup, start + 1);
  }

  /** Default maximum that can be spent on players in a lineup (in tenths). */
  private static final int DEFAULT_BUDGET = 1000; // 100

  /** Entry point for a program to compute optimal lineups. */
  public static void main(String[] args) throws Exception {
    ArgParser argParser = new ArgParser("OptimalLineup");
    argParser.addOption("budget", Double.class);
    argParser.addOption("enforce-per-team-limit", Boolean.class);
    args = argParser.parseArgs(args, 1, 1);

    int budget = argParser.hasOption("budget") ?
        toTenths(argParser.getDoubleOption("budget")) : DEFAULT_BUDGET;

    // Parse the list of players from the file given in args[0]
    List<Player> players = new ArrayList<Player>();
    CsvParser parser = new CsvParser(args[0], true, new Object[] {
          // name, team, position, points, price, play prob
          String.class, String.class, String.class,
          Float.class, Float.class, Float.class
        });
    while (parser.hasNext()) {
      String[] row = parser.next();
      players.add(new Player(row[0], Position.valueOf(row[2]), row[1],
          toTenths(Float.parseFloat(row[4])),
          Float.parseFloat(row[3]), Float.parseFloat(row[5])));
    }

    Lineup optLineup = argParser.hasOption("enforce-per-team-limit")
        ? findOptimalLineup(players, budget, "")
        : new OptimalLineup(players, budget).compute();
    displayLineup(optLineup, budget);
  }

  /**
   * Returns the optimal lineup using only the given players and within the
   * given budget. This heavily leans on the {@code OptimalLineup} class, but
   * adds additional searching to enforce the per-team limit.
   */
  private static Lineup findOptimalLineup(
      List<Player> players, int budget, String label) {
    // TODO (extra credit): implement this
    return null;
  }

  /** Converts a price into tenths. */
  private static int toTenths(double price) {
    return (int) Math.round(10 * price);
  }

  /** Displays a lineup, which is assumed to meet the position constraints. */
  private static void displayLineup(Lineup lineup, int budget) {
    if (lineup == null) {
      System.out.println("*** No solution");
      return;
    }

    List<Player> keepers = lineup.getPlayersByPosition(Position.GK);
    List<Player> defenders = lineup.getPlayersByPosition(Position.DEF);
    List<Player> midfielders = lineup.getPlayersByPosition(Position.MID);
    List<Player> forwards = lineup.getPlayersByPosition(Position.FWD);

    assert keepers.size() == 2;
    assert defenders.size() == 5;
    assert midfielders.size() == 5;
    assert forwards.size() == 3;

    assert lineup.getTotalPrice() <= budget;

    System.out.printf(" GK  %s\n", describePlayer(keepers.get(0)));
    System.out.printf("     %s\n", describePlayer(keepers.get(1)));
    System.out.printf("DEF  %s\n", describePlayer(defenders.get(0)));
    System.out.printf("     %s\n", describePlayer(defenders.get(1)));
    System.out.printf("     %s\n", describePlayer(defenders.get(2)));
    System.out.printf("     %s\n", describePlayer(defenders.get(3)));
    System.out.printf("     %s\n", describePlayer(defenders.get(4)));
    System.out.printf("MID  %s\n", describePlayer(midfielders.get(0)));
    System.out.printf("     %s\n", describePlayer(midfielders.get(1)));
    System.out.printf("     %s\n", describePlayer(midfielders.get(2)));
    System.out.printf("     %s\n", describePlayer(midfielders.get(3)));
    System.out.printf("     %s\n", describePlayer(midfielders.get(4)));
    System.out.printf("FWD  %s\n", describePlayer(forwards.get(0)));
    System.out.printf("     %s\n", describePlayer(forwards.get(1)));
    System.out.printf("     %s\n", describePlayer(forwards.get(2)));
    System.out.printf("*** Totals: price £%.1fm, expected points %.1f\n",
        lineup.getTotalPrice() / 10., lineup.getExpectedPoints());
  }

  /** Returns a short description of a player with price and opponent. */
  private static String describePlayer(Player p) {
    return String.format("%-25s %3s %6s %5.2f %3.0f%%",
        p.getName(), p.getTeam(), String.format("£%.1fm", p.getPrice() / 10.),
        p.getPoints(), 100 * p.getPlayProbability());
  }
}
