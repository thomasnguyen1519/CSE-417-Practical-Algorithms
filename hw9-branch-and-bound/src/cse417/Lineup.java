package cse417;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Stores information about a lineup, which may be only partially filled. */
public class Lineup {

  /** Limit on the number of players at each position in a lineup. */
  public static final Map<Position, Integer> POSITION_LIMIT =
      new HashMap<Position, Integer>();
  static {
    POSITION_LIMIT.put(Position.GK, 2);
    POSITION_LIMIT.put(Position.DEF, 5);
    POSITION_LIMIT.put(Position.MID, 5);
    POSITION_LIMIT.put(Position.FWD, 3);
  }

  /** Stores the players in the lineup. */
  private final List<Player> players = new ArrayList<Player>();

  /** Stores the players in the lineup by position. */ 
  private final Map<Position, List<Player>> playersByPos;

  /** Stores the limit for the total price of the players. */
  private int budget;

  /** Stores the limit on number of players from one team. */
  private int teamLimit;

  /** Creates an empty lineup with the given budget. */
  public Lineup(int budget) {
    this(budget, 3);
  }

  /** Creates an empty lineup with the given budget. */
  public Lineup(int budget, int teamLimit) {
    assert budget > 0;
    assert teamLimit >= 3;

    this.budget = budget;
    this.teamLimit = teamLimit;

    playersByPos = new HashMap<Position, List<Player>>();
    for (Position pos : Position.values())
      playersByPos.put(pos, new ArrayList<Player>());
  }

  /** Returns the total budget for this lineup. */
  public int getBudget() { return budget; }

  /** Returns the remaining budget for this lineup. */
  public int getRemainingBudget() { return budget - getTotalPrice(); }

  /** Returns the number of players in this lineup. */
  public int size() { return players.size(); }

  /** Returns the number of players in this lineup at the given position. */
  public int sizeByPosition(Position pos) {
    return playersByPos.get(pos).size();
  }

  /** Returns the number of players in this lineup from the given team. */
  public int sizeByTeam(String team) {
    int count = 0;
    for (Player p : players)
      if (team.equals(p.getTeam()))
        count++;
    return count;
  }

  /** Returns the players in this lineup. */
  public List<Player> getPlayers() { return new ArrayList<Player>(players); }

  /** Returns the players in this lineup at the given position. */
  public List<Player> getPlayersByPosition(Position pos) {
    return new ArrayList<Player>(playersByPos.get(pos));
  }

  /** Returns the teams used in this lineup. */
  public Set<String> getTeams() {
    Set<String> teams = new HashSet<String>();
    for (Player p : players)
      teams.add(p.getTeam());
    return teams;
  }

  /** Determines whether the given player can be added to the lineup. */
  public boolean canAdd(Player player) {
    // Check the total player limit
    if (players.size() + 1 > 15)
      return false;

    // Check the price limit. (Note: we need to account for round-off error.)
    if (getTotalPrice() + player.getPrice() > budget)
      return false;

    // Check the per-position limit
    final Position pos = player.getPosition();
    if (playersByPos.get(pos).size() + 1 > POSITION_LIMIT.get(pos))
      return false; 

    // Check the per-team limit
    int teamCount = (int) players.stream()
        .filter(p -> p.getTeam().equals(player.getTeam())).count();
    if (teamCount + 1 > teamLimit)
      return false;

    return true;
  }

  /** Adds the given player to this lineup. */
  public void add(Player player) {
    assert canAdd(player);
    players.add(player);
    playersByPos.get(player.getPosition()).add(player);
  }

  /** Removes the given player from this lineup. */
  public void remove(Player player) {
    players.remove(player);
    playersByPos.get(player.getPosition()).remove(player);
  }

  /** Removes the total price of the players in this lineup (so far). */
  public int getTotalPrice() {
    return players.stream().mapToInt(p -> p.getPrice()).sum();
  }

  /** Returns the points expected to be scored in this lineup (so far). */
  public float getExpectedPoints() {
    float points = 0;

    List<Player> keepers = playersByPos.get(Position.GK);
    if (keepers.size() > 0)
      points += keepers.get(0).getExpectedPoints();
    if (keepers.size() > 1) {
      // GK2 only starts if GK1 doesn't play
      points += keepers.get(1).getExpectedPoints(
          1 - keepers.get(0).getPlayProbability());
    }

    List<Player> defenders = playersByPos.get(Position.DEF);
    if (defenders.size() > 0)
      points += defenders.get(0).getExpectedPoints();
    if (defenders.size() > 1)
      points += defenders.get(1).getExpectedPoints();
    if (defenders.size() > 2)
      points += defenders.get(2).getExpectedPoints();
    if (defenders.size() > 3) {
      // DEF4 only starts if one of DEF1-3 doesn't play
      points += defenders.get(3).getExpectedPoints(probOneDoesntPlay(
          defenders.get(0).getPlayProbability(),
          defenders.get(1).getPlayProbability(),
          defenders.get(2).getPlayProbability()));
    }
    if (defenders.size() > 4) {
      // DEF5 only starts if two of DEF1-4 doesn't play
      points += defenders.get(4).getExpectedPoints(probTwoDontPlay(
          defenders.get(0).getPlayProbability(),
          defenders.get(1).getPlayProbability(),
          defenders.get(2).getPlayProbability(),
          defenders.get(3).getPlayProbability()));
    }

    List<Player> midfielders = playersByPos.get(Position.MID);
    if (midfielders.size() > 0)
      points += midfielders.get(0).getExpectedPoints();
    if (midfielders.size() > 1)
      points += midfielders.get(1).getExpectedPoints();
    if (midfielders.size() > 2)
      points += midfielders.get(2).getExpectedPoints();
    if (midfielders.size() > 3)
      points += midfielders.get(3).getExpectedPoints();
    if (midfielders.size() > 4) {
      // MID5 only starts if one of MID1-4 doesn't play
      points += midfielders.get(4).getExpectedPoints(probOneDoesntPlay(
          midfielders.get(0).getPlayProbability(),
          midfielders.get(1).getPlayProbability(),
          midfielders.get(2).getPlayProbability(),
          midfielders.get(3).getPlayProbability()));
    }

    List<Player> forwards = playersByPos.get(Position.FWD);
    if (forwards.size() > 0)
      points += forwards.get(0).getExpectedPoints();
    if (forwards.size() > 1)
      points += forwards.get(1).getExpectedPoints();
    if (forwards.size() > 2)
      points += forwards.get(2).getExpectedPoints();

    return points;
  }

  /**
   * Returns the maximum start probability for the n-th (zero-based) player at
   * the given position. This probability is known if players 0..n have all
   * been added, but if not, this will overestimate.
   */
  public float maxStartProbability(Position pos, int n, float minProb) {
    switch (pos) {
      case GK:
        List<Player> keepers = playersByPos.get(Position.GK);
        switch (n) {
          case 0: return 1;
          case 1: return 1 - minPlayProb(keepers, 0, minProb);
        }
        break;

      case DEF:
        List<Player> defenders = playersByPos.get(Position.DEF);
        switch (n) {
          case 0: return 1;
          case 1: return 1;
          case 2: return 1;
          case 3: return probOneDoesntPlay(
              minPlayProb(defenders, 0, minProb),
              minPlayProb(defenders, 1, minProb),
              minPlayProb(defenders, 2, minProb));
          case 4: return probTwoDontPlay(
              minPlayProb(defenders, 0, minProb),
              minPlayProb(defenders, 1, minProb),
              minPlayProb(defenders, 2, minProb),
              minPlayProb(defenders, 3, minProb));
        }
        break;

      case MID:
        List<Player> midfielders = playersByPos.get(Position.MID);
        switch (n) {
          case 0: return 1;
          case 1: return 1;
          case 2: return 1;
          case 3: return 1;
          case 4: return probOneDoesntPlay(
              minPlayProb(midfielders, 0, minProb),
              minPlayProb(midfielders, 1, minProb),
              minPlayProb(midfielders, 2, minProb),
              minPlayProb(midfielders, 3, minProb));
        }
        break;

      case FWD:
        switch (n) {
          case 0: return 1;
          case 1: return 1;
          case 2: return 1;
        }
        break;
    }

    assert false : "Bad inputs: " + pos + " " + n;
    return 0;
  }

  // Returns the play probability of the given player in the list or, if none
  // exists, minProb if their spot is in the starting 11 and 0 otherwise.
  private static float minPlayProb(
      List<Player> players, int index, float minProb) {
    if (index < players.size())
      return players.get(index).getPlayProbability();

    return (players.size() < 11) ? minProb : 0.0f;
  }

  // Returns the probability that one (or more) of the three players with the
  // given play probabilities does not play.
  private static float probOneDoesntPlay(float pr1, float pr2, float pr3) {
    return 1 - pr1 * pr2 * pr3;
  }

  // Returns the probability that one (or more) of the four players with the
  // given play probabilities does not play.
  private static float probOneDoesntPlay(
      float pr1, float pr2, float pr3, float pr4) {
    return 1 - pr1 * pr2 * pr3 * pr4;
  }

  // Returns the probability that two of the four players with the given play
  // probabilities do not play.
  private static float probTwoDontPlay(
      float pr1, float pr2, float pr3, float pr4) {
    // If we don't have all playing or all but one playing, then two don't play
    return 1 - (pr1 * pr2 * pr3 * pr4 +
                (1-pr1) * pr2 * pr3 * pr4 +
                pr1 * (1-pr2) * pr3 * pr4 +
                pr1 * pr2 * (1-pr3) * pr4 +
                pr1 * pr2 * pr3 * (1-pr4));
  }

  /** Returns a copy of this lineup. */
  public Lineup clone() {
    Lineup lineup = new Lineup(budget, teamLimit);
    for (Player p : players)
      lineup.add(p);
    return lineup;
  }
}
