package cse417;


/** Records information about a drive in an NFL game. */
public class Drive {

  /** Week of the NFL season. */
  public final int week;

  /** Name of the team on offense. */
  public final String offense;

  /** Name of the team on defense. */
  public final String defense;

  /** Average points scored from the starting field position. */
  public final double expPointsAtStart;

  /** Average point scored from the ending field position. */
  public final double expPointsAtEnd;

  /** Creates a drive with the given description. */
  public Drive(int week, String offense, String defense,
      double expPointsAtStart, double expPointsAtEnd) {
    this.week = week;
    this.offense = offense;
    this.defense = defense;
    this.expPointsAtStart = expPointsAtStart;
    this.expPointsAtEnd = expPointsAtEnd;
  }
}
