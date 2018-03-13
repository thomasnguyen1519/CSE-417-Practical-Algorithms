package cse417;

import java.util.List;


/** Records information about a range / subsequence in the price data. */
public class Range {

  /** Returns a range consisting of the given index only. */
  public static Range fromOneIndex(int index, List<Integer> prices) {
    return new Range(index, prices.get(index));
  }

  public final int firstIndex;
  public final int lastIndex;
  public final int lowPrice;
  public final int highPrice;

  /** Creates a range consisting of only one price. */
  private Range(int index, int price) {
    firstIndex = lastIndex = index;
    lowPrice = highPrice = price;
  }

  /** Creates a range consisting of any number of consecutive prices. */
  private Range(int firstIndex, int lastIndex, int lowPrice, int highPrice) {
    assert firstIndex <= lastIndex;
    assert lowPrice <= highPrice;
    this.firstIndex = firstIndex;
    this.lastIndex = lastIndex;
    this.lowPrice = lowPrice;
    this.highPrice = highPrice;
  }

  /**
   * Returns a range consisting of this one followed by the other. This
   * requies that the other range must immediate follows this one.
   */
  public Range concat(Range other) {
    assert this.lastIndex + 1 == other.firstIndex;
    return new Range(firstIndex, other.lastIndex,
        Math.min(this.lowPrice, other.lowPrice),
        Math.max(this.highPrice, other.highPrice));
  }

  /** Returns the length of the range. */
  public int length() { return lastIndex + 1 - firstIndex; }

  /**
   * Determines whether the percent change between the low and high price is
   * less than or equal to the given amount.
   */
  public boolean percentChangeAtMost(double pctChanged) {
    // highPrice / lowPrice <= 1 + pctChanged/100
    return highPrice <= lowPrice * (1 + pctChanged/100);
  }
}
