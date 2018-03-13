package cse417;

/** Stores information about an individual player. */
public class Player {

  private final String name;
  private final Position pos;
  private final String team;
  private final int price;
  private final float points;
  private final float playProb;

  /** Creates a player with the fields as specified. */
  public Player(String name, Position pos, String team, int price,
      float points, float playProb) {
    this.name = name;
    this.pos = pos;
    this.team = team;
    this.price = price;
    this.points = points;
    this.playProb = playProb;
  }

  /** Returns the name of this player. */
  public String getName() { return name; }

  /** Returns the position of the player. */
  public Position getPosition() { return pos; }

  /** Determines whether this player plays at the given position. */
  public boolean atPosition(Position p) { return pos.equals(p); }

  /** Returns the team that this player plays for. */
  public String getTeam() { return team; }

  /** Returns the price for this player measured in tenths (0.1). */
  public int getPrice() { return price; }

  /** Returns the projected points for this player if they play. */
  public float getPoints() { return points; }

  /** Returns the probability that this player will play. */
  public float getPlayProbability() { return playProb; }

  /** Returns the expected points for this player if in the starting lineup. */
  public float getExpectedPoints() { return playProb * points; }

  /**
   * Returns the expected points for this player when there is only a
   * {@code startProb} probability that they are one of the 11 starting players.
   */
  public float getExpectedPoints(float startProb) {
   return startProb * playProb * points;
  }

  @Override public boolean equals(Object o) {
    if (!(o instanceof Player))
      return false;
    Player p = (Player) o;
    return name.equals(p.name) && team.equals(p.team) && pos.equals(p.pos);
  }

  @Override public int hashCode() {
    return name.hashCode() ^ team.hashCode() ^ pos.hashCode();
  }

  @Override public String toString() {
    return String.format("%s (%s %s)", name, pos, team);
  }
}
