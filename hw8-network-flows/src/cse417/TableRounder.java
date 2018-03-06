package cse417;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleFunction;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static cse417.GraphUtils.EPSILON;



/**
 * Program that reads a table of numbers stored in a CSV and writes a new CSV
 * with the entries in the table rounded in such a way that the colum and row
 * sums are equal to the correct sums rounded.
 */
public class TableRounder {

  /** Entry point for a program to round table entries. */
  public static void main(String[] args) throws IOException {
    ArgParser argParser = new ArgParser("Table");
    argParser.addOption("header", Boolean.class);
    argParser.addOption("digits", Integer.class);
    argParser.addOption("out-file", String.class);
    args = argParser.parseArgs(args, 1, 1);

    // If the user asks us to round to a digit after the decimal place, we
    // multiply by a power of 10 so that rounding integers after scaling is the
    // same as rounding at the desired decimal place. (We scale back below.)
    int digits = argParser.hasOption("digits") ?
        argParser.getIntegerOption("digits") : 0;
    final double scale = Math.pow(10, digits);

    CsvParser csvParser = new CsvParser(args[0]);
    String[] header = null;
    if (argParser.hasOption("header")) {
      assert csvParser.hasNext();
      header = csvParser.next();
    }

    // Read the table from the CSV.
    List<double[]> table = new ArrayList<double[]>();
    while (csvParser.hasNext()) {
      table.add(Arrays.asList(csvParser.next()).stream()
          .mapToDouble(s -> scale * Double.parseDouble(s)).toArray());
      if (table.size() > 2) {
        assert table.get(table.size()-2).length ==
               table.get(table.size()-1).length;
      }
    }

    roundTable(table);

    // Output the rounded tables.
    PrintStream output = !argParser.hasOption("out-file") ? System.out :
        new PrintStream(new FileOutputStream(
            argParser.getStringOption("out-file")));
    if (header != null)
      writeRow(output, header);  // echo the header to the output
    for (double[] vals : table) {
      writeRow(output,
          DoubleStream.of(vals).map(v -> v / scale).toArray(), digits);
    }
  }

  /** Modifies the given table so that each entry is rounded to an integer. */
  static void roundTable(final List<double[]> table) {
    if (table.size() == 0) return;
    
	int numRows = table.size();
	int numCols = table.get(0).length;
	
	double[] rowSum = new double[numRows];
	double[] colSum = new double[numCols];
	double[][] newTable = new double[numRows][numCols];
	
	for (int i = 0; i < numRows; i++) {
		for (int j = 0; j < numCols; j++) {
			newTable[i][j] = table.get(i)[j];
			rowSum[i] += table.get(i)[j];
			colSum[j] += table.get(i)[j];
		}
	}
	
	Integer source = -numCols - 1;
	Integer sink = numRows + 1;
	List<Integer> nodes = new ArrayList<Integer>();
	
    for (int i = 0; i < numRows + 1; i++) {
    	nodes.add(i);
    }
    for (int i = 0; i < numCols + 1; i++) {
    	nodes.add(-i);
    }
    
	ToDoubleBiFunction<Integer, Integer> minEdgeFlow = (a, b) -> {
		if (a.equals(source) && (b < 0 && b >= -numCols)) {
			return Math.floor(colSum[-b - 1]);
		} else if ((a > 0 && a <= numRows) && b.equals(sink)) {
			return Math.floor(rowSum[a - 1]);
		} else if ((b > 0 && b <= numRows) && (a < 0 && a >= -numCols)) {
			return Math.floor(newTable[b - 1][-a - 1]);
		}
		return 0.0;};
	
	ToDoubleBiFunction<Integer, Integer> maxEdgeFlow = (a, b) -> {
		if (a.equals(source) && (b < 0 && b >= -numCols)) {
			return Math.ceil(colSum[-b - 1]);
		} else if ((a > 0 && a <= numRows) && b.equals(sink)) {
			return Math.ceil(rowSum[a - 1]);
		} else if ((b > 0 && b <= numRows) && (a < 0 && a >= -numCols)) {
			return Math.ceil(newTable[b - 1][-a - 1]);
		}
		return 0.0;};
	
	ToDoubleBiFunction<Integer, Integer> flow = findFeasibleBoundedFlow(source, sink, nodes,
																		minEdgeFlow, maxEdgeFlow);
	for (int i = 0; i < numCols; i++) {
		for (int j = 0; j < numRows; j++) {
			table.get(j)[i] = flow.applyAsDouble(-i - 1, j + 1);
		}
	}
  }

  /**
   * Returns a flow that satisfies the given constraints or null if none
   * exists.
   */
  static ToDoubleBiFunction<Integer, Integer> findFeasibleBoundedFlow(
      final Integer source, final Integer sink, Collection<Integer> nodes,
      ToDoubleBiFunction<Integer, Integer> minEdgeFlow,
      ToDoubleBiFunction<Integer, Integer> maxEdgeFlow) {
	  
	  Map<Integer, Double> map = new HashMap<Integer, Double>();
	  for (Integer val : nodes) {
		  map.put(val, -GraphUtils.imbalanceAt(val, nodes, minEdgeFlow));
	  }
	  ToDoubleBiFunction<Integer, Integer> max = (a, b) -> {
		  if (b.equals(source) && a.equals(sink)) {
			  return Double.POSITIVE_INFINITY;
		  } else {
			  return maxEdgeFlow.applyAsDouble(a, b) - minEdgeFlow.applyAsDouble(a, b);
		  }
	  };
	  ToDoubleBiFunction<Integer, Integer> feasibleFlow = findFeasibleDemandFlow(nodes, max,
			  																	(a) -> map.get(a));
	  return (a, b) -> feasibleFlow.applyAsDouble(a, b) + minEdgeFlow.applyAsDouble(a, b);
  }

  /**
   * Returns a circulation that satisfies the given capacity constraints (upper
   * bounds) and demands or null if none exists.
   */
  static ToDoubleBiFunction<Integer, Integer> findFeasibleDemandFlow(
      Collection<Integer> nodes,
      final ToDoubleBiFunction<Integer, Integer> capacity,
      final ToDoubleFunction<Integer> demand) {

    // Make sure that the demands could even possibly be met.
    double surplus = 0, deficit = 0;
    for (Integer n : nodes) {
      if (demand.applyAsDouble(n) >= EPSILON)
        surplus += demand.applyAsDouble(n);
      if (demand.applyAsDouble(n) <= -EPSILON)
        deficit += -demand.applyAsDouble(n);
    }
    assert Math.abs(surplus - deficit) <= 1e-5;

    Integer max = 0;
    List<Integer> updatedNodes = new ArrayList<Integer>(nodes);
    for (Integer val : nodes) {
    	max = Math.max(max, val);
    }
    final Integer source = max + 1;
    final Integer sink = source + 1;
    updatedNodes.add(sink);
    updatedNodes.add(source);
    return GraphUtils.maxFlow(source, sink, updatedNodes, (a, b) -> {
    	if ((!a.equals(source) && !a.equals(sink)) && b.equals(sink) &&
    		 demand.applyAsDouble(a) > 0) {
			return demand.applyAsDouble(a);
		} else if ((!b.equals(source) && !b.equals(sink)) &&
					a.equals(source) && demand.applyAsDouble(b) < 0) {
			return -demand.applyAsDouble(b);
		} else if ((!a.equals(source) && !a.equals(sink)) &&
					(!b.equals(source) && !b.equals(sink))) {
			return capacity.applyAsDouble(a, b);
		}
		return 0.0;});
  }

  /**
   * Outputs a CSV row of the given values with the specified number of digits
   * after the decimal.
   */
  private static void writeRow(PrintStream out, double[] vals, int digits) {
    final String fmt = String.format("%%.%df", digits);
    DoubleFunction<String> fmtVal = v -> String.format(fmt, v);
    writeRow(out, DoubleStream.of(vals).mapToObj(fmtVal)
        .toArray(n -> new String[n]));
  }

  /**
   * Outputs a CSV row containing the given values. Note that the current
   * implementation assumes that there are no commas in the column values.
   */
  private static void writeRow(PrintStream out, String[] row) {
    for (int i = 0; i < row.length; i++)
      assert row[i].indexOf(',') < 0;  // quoting not supported here

    out.println(Stream.of(row).collect(Collectors.joining(",")).toString());
  }
}
