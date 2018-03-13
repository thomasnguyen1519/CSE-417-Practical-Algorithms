package cse417;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Used to compute an upper bound on how many points can be achieved by
 * completing a lineup. This is only an upper bound because it ignores some of
 * the limits on number of players from one team and, most importantly, may
 * over-estimate the play probability of some players. 
 */
public class LineupBound {

  /** Lineup that we are completing. */
  private final Lineup lineup;

  /** Records the budget for the <b>remaining</b> players. */
  private final int budget;

  /** Count of players needed at each position (in order GK, DEF, MID, FWD). */
  private final int[] numNeeded;

  /** Records the start probability for each needed player. */
  private final float[][] startProbs;

  /**
   * Creates an object to compute the upper bound for the given lineup using
   * only players with at least the given probability of playing in the
   * starting 11.
   */
  public LineupBound(Lineup partialLineup, float minPlayProb, int numNeededGK,
      int numNeededDEF, int numNeededMID, int numNeededFWD) {
    this.lineup = partialLineup;
    this.budget = lineup.getRemainingBudget();
    this.numNeeded = new int[] {
          numNeededGK, numNeededDEF, numNeededMID, numNeededFWD };

    this.startProbs = new float[][] {
          new float[numNeeded[0]], new float[numNeeded[1]],
          new float[numNeeded[2]], new float[numNeeded[3]]
        };
    for (int i = 0; i < numNeeded[0]; i++)
      startProbs[0][i] = lineup.maxStartProbability(
          Position.GK, (2-numNeeded[0])+i, minPlayProb);
    for (int i = 0; i < numNeeded[1]; i++)
      startProbs[1][i] = lineup.maxStartProbability(
          Position.DEF, (5-numNeeded[1])+i, minPlayProb);
    for (int i = 0; i < numNeeded[2]; i++)
      startProbs[2][i] = lineup.maxStartProbability(
          Position.MID, (5-numNeeded[2])+i, minPlayProb);
    for (int i = 0; i < numNeeded[3]; i++)
      startProbs[3][i] = lineup.maxStartProbability(
          Position.FWD, (3-numNeeded[3])+i, minPlayProb);
  }

  /**
   * Returns an upper bound on the maximum points achievable for completing the
   * partial lineup within the set budget using only the allowed players or 0
   * if there is no way to complete the lineup.
   * <p>
   * The players must be in decreasing order by points if playing. One can
   * check that putting a player with more expected points on the bench (or
   * even later on the bench) can only decrease expected points.
   */
  public float maxPoints(Iterator<Player> players) {

    // Record the largest possible index into the table.
    final int maxIndex = makeIndex(budget,
        numNeeded[0], numNeeded[1], numNeeded[2], numNeeded[3]);

    // Create a column for the empty roster.
    float[] emptyRosterPoints = new float[maxIndex+1];
    Arrays.fill(emptyRosterPoints, Float.NEGATIVE_INFINITY);
    emptyRosterPoints[makeIndex(0, 0, 0, 0, 0)] = 0;

    // We will keep only the last column and the one to be filled in next.
    float[] prevBest = emptyRosterPoints;
    float[] nextBest = new float[maxIndex+1];

    // Add a column for each player that is allowed.
    while (players.hasNext()) {
      Player p = players.next();

      // Start with the cost if the player is not included.
      System.arraycopy(prevBest, 0, nextBest, 0, maxIndex+1);

      // Consider each index to which this player can be added.
      for (int price = 0; price <= budget - p.getPrice(); price++) {
        for (int numGKs = 0; numGKs <= numNeeded[0]; numGKs++) {
          int newNumGKs = numGKs + (p.atPosition(Position.GK) ? 1 : 0);
          if (newNumGKs > numNeeded[0])
            continue;

          for (int numDEFs = 0; numDEFs <= numNeeded[1]; numDEFs++) {
            int newNumDEFs = numDEFs + (p.atPosition(Position.DEF) ? 1 : 0);
            if (newNumDEFs > numNeeded[1])
              continue;

            for (int numMIDs = 0; numMIDs <= numNeeded[2]; numMIDs++) {
              int newNumMIDs = numMIDs + (p.atPosition(Position.MID) ? 1 : 0);
              if (newNumMIDs > numNeeded[2])
                continue;

              for (int numFWDs = 0; numFWDs <= numNeeded[3]; numFWDs++) {
                int newNumFWDs = numFWDs + (p.atPosition(Position.FWD) ? 1 : 0);
                if (newNumFWDs > numNeeded[3])
                  continue;

                // Look up the start probability for this player.
                float startProb;
                switch (p.getPosition()) {
                  case GK:  startProb = startProbs[0][numGKs]; break;
                  case DEF: startProb = startProbs[1][numDEFs]; break;
                  case MID: startProb = startProbs[2][numMIDs]; break;
                  case FWD: startProb = startProbs[3][numFWDs]; break;
                  default: throw new AssertionError(p.getPosition());
                }

                // Compute the points added by this player, which depends on
                // their start probability.
                int index = makeIndex(price, numGKs, numDEFs, numMIDs, numFWDs);
                float newPoints =
                    prevBest[index] + startProb * p.getExpectedPoints();

                // See if this player is better than the best option seen so far
                int newIndex = makeIndex(price + p.getPrice(),
                    newNumGKs, newNumDEFs, newNumMIDs, newNumFWDs);
                if (newPoints > nextBest[newIndex])
                  nextBest[newIndex] = newPoints;
              }
            }
          }
        }
      }

      // Re-use the previous row as the new row now that we're done with it.
      float[] last = prevBest;
      prevBest = nextBest;
      nextBest = last;
    }

    // Find the optimal point total, which only includes rosters that fill out
    // the required number of players at each position.
    float maxPoints = Float.NEGATIVE_INFINITY;
    for (int price = 0; price <= budget; price++) {
      int index = makeIndex(price,
          numNeeded[0], numNeeded[1], numNeeded[2], numNeeded[3]);
      if (prevBest[index] > maxPoints)
        maxPoints = prevBest[index];
    }

    return lineup.getExpectedPoints() + maxPoints;  // points so far + added
  }

  /** Computes a row index for a price and set of used players. */
  private int makeIndex(
      int price, int numGKs, int numDEFs, int numMIDs, int numFWDs) {
    int positions = encodePositionCounts(numGKs, numDEFs, numMIDs, numFWDs);
    return positions * (budget + 1) + price;
  }

  /** Encodes the number at each position into an integer. */
  private int encodePositionCounts(
      int numGKs, int numDEFs, int numMIDs, int numFWDs) {
    assert 0 <= numGKs && numGKs <= numNeeded[0];
    assert 0 <= numDEFs && numDEFs <= numNeeded[1];
    assert 0 <= numMIDs && numMIDs <= numNeeded[2];
    assert 0 <= numFWDs && numFWDs <= numNeeded[3];
    return ((numGKs
        * (numNeeded[1]+1) + numDEFs)
        * (numNeeded[2]+1) + numMIDs)
        * (numNeeded[3]+1) + numFWDs;
  }
}
