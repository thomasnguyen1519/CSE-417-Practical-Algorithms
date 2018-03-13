package cse417;

/** Stores information about an individual player. */
public class Player {

  private final String name;
  private final Position pos;
  private final String team;
  private final String opponent;
  private final int price;
  private final float ptsExpected, ptsStdDev;

  /**
   * Creates a player with the fields as specified. Playing away is indicated
   * by prefixing '@' on the opponent.
   */
  public Player(String name, Position pos, String team, String opponent,
      int price, float ptsExpected, float ptsStdDev) {
    this.name = name;
    this.pos = pos;
    this.team = team;
    this.opponent = opponent;
    this.price = price;
    this.ptsExpected = ptsExpected;
    this.ptsStdDev = ptsStdDev;
  }

  /** Returns the name of this player. */
  public String getName() { return name; }

  /** Returns the position of the player. */
  public Position getPosition() { return pos; }

  /** Determines whether this player plays at the given position. */
  public boolean atPosition(Position pos) { return this.pos.equals(pos); }

  /** Returns the team that this player plays for. */
  public String getTeam() { return team; }

  /** Returns the team that this player plays against. */
  public String getOpponent() {
    return (opponent.charAt(0) == '@') ? opponent.substring(1) : opponent;
  }

  /** Returns whether this player is playing at home. */
  public boolean isAtHome() { return opponent.charAt(0) != '@'; }

  /** Returns the price for this player. */
  public int getPrice() { return price; }

  /** Returns the projected points for this player. */
  public float getPointsExpected() { return ptsExpected; }

  /** Returns the stddev of the projected points for this player. */
  public float getPointsStdDev() { return ptsStdDev; }

  /** Returns the variance of the projected points for this player. */
  public float getPointsVariance() { return ptsStdDev * ptsStdDev; }

  @Override public String toString() {
    return String.format("%s (%s %s, $%d)", name, pos, team, price);
  }
}
