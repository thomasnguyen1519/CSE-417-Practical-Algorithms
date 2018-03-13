package cse417;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * Program that finds the longest "sideways" trend in price data. The option
 * {@code --max-pct-change} controls how far apart the high and low (closing)
 * prices can be, in percentage terms, during a period for it to be consider a
 * sideways trend. This defaults to 5%.
 */
public class SidewaysTrend {

  /** Format for the dates used in the data files. */
  private static final DateFormat DATE_FORMAT =
      new SimpleDateFormat("dd-MMM-yy");

  /** Entry point for a program to build a model of NFL teams. */
  public static void main(String[] args) throws Exception {
    ArgParser argParser = new ArgParser("SidewaysTrend");
    argParser.addOption("max-pct-change", Double.class);
    argParser.addOption("naive", Boolean.class);
    args = argParser.parseArgs(args, 1, 1);

    double maxPctChange = argParser.hasOption("max-pct-change") ?
        argParser.getDoubleOption("max-pct-change") : 5.0;

    List<Date> dates = new ArrayList<Date>();
    List<Integer> prices = loadPrices(args[0], dates);

    Range longest;
    if (argParser.hasOption("naive")) {
      longest = findLongestSidewaysTrendNaive(maxPctChange, prices);
    } else {
      longest = findLongestSidewaysTrend(
          maxPctChange, prices, 0, prices.size()-1);
    }

    System.out.printf(
        "Longest sideways trend is from %s to %s (%d trading days)\n",
        DATE_FORMAT.format(dates.get(longest.firstIndex)),
        DATE_FORMAT.format(dates.get(longest.lastIndex)),
        longest.length());
    System.out.printf("Price range is %.2f to %.2f, a %.1f%% change\n",
        longest.lowPrice/100., longest.highPrice/100.,
        100. * (longest.highPrice - longest.lowPrice) / longest.lowPrice);
  }

  /**
   * Returns the prices in the file. Prices are returned in units of cents
   * ($0.01) to avoid roundoff issues elsewhere in the code.
   * @param fileName Name of the CSV file containing price data
   * @param dates If non-null, dates will be stored in this list. In this case,
   *     we will also check that the prices are in order of increasing date.
   */
  private static List<Integer> loadPrices(String fileName, List<Date> dates)
      throws IOException, ParseException {
    assert (dates == null) || (dates.size() == 0);

    // Stores the relevant information from one row of data.
    class Row {
      public final Date date;
      public final int price;
      public Row(Date date, int price) { this.date = date; this.price = price; }
    }
    List<Row> rows = new ArrayList<Row>();

    CsvParser parser = new CsvParser(fileName, true, new Object[] {
          DATE_FORMAT, Float.class, Float.class, Float.class, Float.class,
          String.class, String.class
        });
    while (parser.hasNext()) {
      String[] parts = parser.next();
      double close = Double.parseDouble(parts[1]);
      rows.add(new Row(DATE_FORMAT.parse(parts[0]), (int)(100 * close)));
    }

    // Put the rows in increasing order of date.
    Collections.sort(rows, (r1, r2) -> r1.date.compareTo(r2.date));

    // If requested, otput the dates from the file.
    if (dates != null) {
      for (Row row : rows)
        dates.add(row.date);
    }

    // Return the prices from the file.
    List<Integer> prices = new ArrayList<Integer>();
    for (Row row : rows)
      prices.add(row.price);
    return prices;
  }

  /** Returns the range with the longest sideways trend in the price data. */
  private static Range findLongestSidewaysTrendNaive(
      double maxPctChange, List<Integer> prices) {
	  if (maxPctChange < 0 || prices.isEmpty() || prices == null) {
		  throw new IllegalArgumentException();
	  }
	  Range maxRange = Range.fromOneIndex(0, prices);
	  for (int i = 0; i < prices.size(); i++) {
		  Range curr = Range.fromOneIndex(i, prices);
		  for (int j = i + 1; j < prices.size(); j++) {
			  curr = curr.concat(Range.fromOneIndex(j, prices));
			  if (!curr.percentChangeAtMost(maxPctChange)) {
				  break;
			  }
			  if (curr.length() > maxRange.length()) {
				  maxRange = curr;
			  }
		  }
	  }
	  return maxRange;
  }

  /**
   * Returns the range with the longest sideways trend in the price data from
   * {@code firstIndex} to {@code lastIndex} (inclusive).
   */
  private static Range findLongestSidewaysTrend(double maxPctChange,
      List<Integer> prices, int firstIndex, int lastIndex) {
    assert firstIndex <= lastIndex;
    if (maxPctChange < 0 || firstIndex >= prices.size() || lastIndex < 0 ||
    		firstIndex < 0 || lastIndex >= prices.size() ||
            prices == null) {
    	throw new IllegalArgumentException();
    }
    if (firstIndex == lastIndex) {
    	return Range.fromOneIndex(firstIndex, prices);
    }
	int midPoint = (firstIndex + lastIndex) / 2;
	Range right = findLongestSidewaysTrend(maxPctChange, prices, midPoint + 1, lastIndex);
	Range left = findLongestSidewaysTrend(maxPctChange, prices, firstIndex, midPoint);
	Range crossing = findLongestSidewaysTrendCrossingMidpoint(maxPctChange, prices,
															  firstIndex, midPoint, lastIndex);
	return maxRange(left, crossing, right);
  }

  /**
   * Returns the range with the longest sideways trend in the price data from
   * {@code firstIndex} to {@code lastIndex} (inclusive) that either starts
   * at or before {@code midIndex} or ends at or after {@code midIndex+1}. (If
   * no such range defines a sideways trend, then it returns null.)
   */
  private static Range findLongestSidewaysTrendCrossingMidpoint(
      double maxPctChange, List<Integer> prices, int firstIndex, int midIndex,
      int lastIndex) {
	  Queue<Range> rights = populateRightRanges(prices, midIndex + 1, lastIndex);
      Stack<Range> lefts = populateLeftRanges(prices, firstIndex, midIndex);
	  Range max = rights.peek();
	  Range rightSide = rights.remove();
	  while (!lefts.isEmpty()) {
		  Range leftSide = lefts.pop();
		  Range nextRightSide = getRightSide(leftSide, rightSide, rights, maxPctChange);
		  if (nextRightSide != null) {
			  rightSide = nextRightSide;
			  Range curr = leftSide.concat(rightSide);
			  if (curr.length() - 1 >= max.length()) {
				  max = curr;
			  }
		  }
	  }
	  if (max.length() > 1) {
		  return max;
      }
      return null;
  }
  
  /*
  	Returns a Queue of Ranges that represents all the Ranges for the right side.
  	Accepts a List of Integers representing the prices, an int for the
  	beginning start point, and another int for the end point.
  */
  private static Queue<Range> populateRightRanges(List<Integer> prices, int begin,
                                            	  int end) {
	  Queue<Range> ranges = new LinkedList<Range>();
	  Range curr = Range.fromOneIndex(begin, prices);
	  ranges.add(curr);
	  for (int i = begin + 1; i <= end; i++) {
		  curr = curr.concat(Range.fromOneIndex(i, prices));
		  ranges.add(curr);
	  }
	  return ranges;
  }
  
  /*
  	Returns a Stack of Ranges that represents all the Ranges for the left.
  	Accepts a List of Integers representing the prices, an int for the
  	beginning start point, and another int for the end point.
  */
  private static Stack<Range> populateLeftRanges(List<Integer> prices, int begin,
                                           		 int end) {
	  Stack<Range> ranges = new Stack<Range>();
	  Range curr = Range.fromOneIndex(end, prices);
	  ranges.add(curr);
	  for (int i = end - 1; i > begin - 1; i--) {
		  curr = Range.fromOneIndex(i, prices).concat(curr);
		  ranges.add(curr);
	  }
	  return ranges;
  }

  /*
    Determines and returns the Range for the right side of the algorithm.
    Accepts Range left and right, a Queue of Ranges, and a double representing
    the max percent change.
  */
  private static Range getRightSide(Range left, Range right,
        Queue<Range> rightRanges, double maxPtDiff) {
    Range cross = left.concat(right);
    if (!cross.percentChangeAtMost(maxPtDiff)) {
        return null;
    }
    while (!rightRanges.isEmpty()) {
        Range nextRight = rightRanges.element();
        cross = left.concat(nextRight);
        if (cross.percentChangeAtMost(maxPtDiff)) {
            right = rightRanges.remove();
        } else {
            break;
        }
    }
    return right;
  }
  
  /*
    Returns the determined max Range based on the Range left, cross, right
    params.
  */
  private static Range maxRange(Range left, Range crossPoint, Range right) {
    Range max = left;
    if (right.length() > max.length()) {
        max = right;
    }
    if (crossPoint != null && crossPoint.length() - 1 >= max.length()) {
        max = crossPoint;
    }
    return max;
  }
}
