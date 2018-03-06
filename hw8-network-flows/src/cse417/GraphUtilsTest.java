package cse417;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.function.*;

import org.junit.Test;
import static org.junit.Assert.*;


public class GraphUtilsTest {

  @Test public void testShortestPathOnSingleNodeGraph() {
    List<String> list = GraphUtils.shortestPath("1", "1", Arrays.asList("1"),
        n -> Arrays.asList(new String[0]));
    assertEquals(1, list.size());
    assertEquals("1", list.get(0));
  }

  @Test public void testShortestPathOnSingleEdgeGraph() {
    List<String> list = GraphUtils.shortestPath("1", "2",
        Arrays.asList("1", "2"),
        n -> "1".equals(n) ? Arrays.asList("2") :
            Arrays.asList(new String[0]));
    assertEquals(2, list.size());
    assertEquals("1", list.get(0));
    assertEquals("2", list.get(1));

    list = GraphUtils.shortestPath("2", "1", Arrays.asList("1", "2"),
        n -> "1".equals(n) ? Arrays.asList("2") :
            Arrays.asList(new String[0]));
    assertEquals(null, list);
  }

  @Test public void testShortestPathOnMediumExample() {
    List<String> nodes =
        Arrays.asList(new String[] {"1", "2", "3", "4", "5", "6", "7", "8"});
    Function<String, Collection<String>> adj = n -> {
          if ("1".equals(n))
            return Arrays.asList(new String[] {"2", "5"});
          else if ("2".equals(n))
            return Arrays.asList(new String[] {"1", "3", "7"});
          else if ("3".equals(n))
            return Arrays.asList(new String[] {"2", "4"});
          else if ("4".equals(n))
            return Arrays.asList(new String[] {"3", "8"});
          else if ("5".equals(n))
            return Arrays.asList(new String[] {"1", "6"});
          else if ("6".equals(n))
            return Arrays.asList(new String[] {"5", "7"});
          else if ("7".equals(n))
            return Arrays.asList(new String[] {"6", "8"});
          else if ("8".equals(n))
            return Arrays.asList(new String[] {"4", "7"});
          else
            throw new AssertionError("bad node: " + n);
        };

    List<String> list = GraphUtils.shortestPath("1", "8", nodes, adj);
    assertEquals(4, list.size());
    assertEquals("1", list.get(0));
    assertEquals("2", list.get(1));
    assertEquals("7", list.get(2));
    assertEquals("8", list.get(3));

    list = GraphUtils.shortestPath("8", "1", nodes, adj);
    assertEquals(5, list.size());
    assertEquals("8", list.get(0));
    assertEquals("4", list.get(1));
    assertEquals("3", list.get(2));
    assertEquals("2", list.get(3));
    assertEquals("1", list.get(4));
  }

  @Test public void testLeastCostPathOnSingleNodeGraph() {
    Map<String, String> prev = new HashMap<String, String>();
    Map<String, Double> dist = GraphUtils.leastCostPaths("1",
        Arrays.asList(new String[] {"1"}),
        (n, m) -> Double.POSITIVE_INFINITY, prev);
    assertEquals(1, dist.size());
    assertEquals(0., dist.get("1"), 1e-5);
    assertEquals(1, prev.size());
    assertEquals(null, prev.get("1"));

    // Test with prev = null 
    dist = GraphUtils.leastCostPaths("1",
        Arrays.asList(new String[] {"1"}),
        (n, m) -> Double.POSITIVE_INFINITY, null);
    assertEquals(1, dist.size());
    assertEquals(0., dist.get("1"), 1e-5);
  }

  @Test public void testLeastCostPathOnSingleEdgeGraph() {
    Map<String, String> prev = new HashMap<String, String>();
    Map<String, Double> dist = GraphUtils.leastCostPaths("1",
        Arrays.asList(new String[] {"1", "2"}),
        (n, m) -> "1".equals(n) && "2".equals(m) ? 1.: Double.POSITIVE_INFINITY,
        prev);
    assertEquals(2, dist.size());
    assertEquals(0., dist.get("1"), 1e-5);
    assertEquals(1., dist.get("2"), 1e-5);
    assertEquals(2, prev.size());
    assertEquals(null, prev.get("1"));
    assertEquals("1", prev.get("2"));
  }

  @Test public void testLeastCostPathOnSingleTriangleGraph() {
    Map<String, String> prev = new HashMap<String, String>();
    Map<String, Double> dist = GraphUtils.leastCostPaths("1",
        Arrays.asList(new String[] {"1", "2", "3"}),
        (n, m) -> {
          if ("1".equals(n) && "2".equals(m))
            return 1.;
          if ("1".equals(n) && "3".equals(m))
            return 1.;
          if ("2".equals(n) && "3".equals(m))
            return 1.;
          if ("3".equals(n) && "2".equals(m))
            return 8.;
          return Double.POSITIVE_INFINITY;
        }, prev);
    assertEquals(3, dist.size());
    assertEquals(0., dist.get("1"), 1e-5);
    assertEquals(1., dist.get("2"), 1e-5);
    assertEquals(1., dist.get("3"), 1e-5);
    assertEquals(3, prev.size());
    assertEquals(null, prev.get("1"));
    assertEquals("1", prev.get("2"));
    assertEquals("1", prev.get("3"));

    prev = new HashMap<String, String>();
    dist = GraphUtils.leastCostPaths("1",
        Arrays.asList(new String[] {"1", "2", "3"}),
        (n, m) -> {
          if ("1".equals(n) && "2".equals(m))
            return 1.;
          if ("1".equals(n) && "3".equals(m))
            return 3.;
          if ("2".equals(n) && "3".equals(m))
            return 1.;
          if ("3".equals(n) && "2".equals(m))
            return 8.;
          return Double.POSITIVE_INFINITY;
        }, prev);
    assertEquals(3, dist.size());
    assertEquals(0., dist.get("1"), 1e-5);
    assertEquals(1., dist.get("2"), 1e-5);
    assertEquals(2., dist.get("3"), 1e-5);
    assertEquals(3, prev.size());
    assertEquals(null, prev.get("1"));
    assertEquals("1", prev.get("2"));
    assertEquals("2", prev.get("3"));
  }

  @Test public void testLeastCostPathOnMediumExample() {
    Map<String, Double> dist = GraphUtils.leastCostPaths("1",
        Arrays.asList(new String[] {"1", "2", "3", "4", "5", "6"}),
        (n, m) -> {
          if ("1".equals(n) && "2".equals(m))
            return 6.;
          if ("1".equals(n) && "3".equals(m))
            return 4.;
          if ("2".equals(n) && "3".equals(m))
            return 2.;
          if ("2".equals(n) && "4".equals(m))
            return 2.;
          if ("3".equals(n) && "4".equals(m))
            return 1.;
          if ("3".equals(n) && "5".equals(m))
            return 2.;
          if ("4".equals(n) && "6".equals(m))
            return 7.;
          if ("5".equals(n) && "4".equals(m))
            return 1.;
          if ("5".equals(n) && "6".equals(m))
            return 3.;
          return Double.POSITIVE_INFINITY;
        }, null);
    assertEquals(6, dist.size());
    assertEquals(0., dist.get("1"), 1e-5);
    assertEquals(6., dist.get("2"), 1e-5);
    assertEquals(4., dist.get("3"), 1e-5);
    assertEquals(5., dist.get("4"), 1e-5);
    assertEquals(6., dist.get("5"), 1e-5);
    assertEquals(9., dist.get("6"), 1e-5);
  }

  @Test public void testLeastCostPathOnMediumExample2() {
    Map<String, String> prev = new HashMap<String, String>();
    Map<String, Double> dist = GraphUtils.leastCostPaths("A",
        Arrays.asList(new String[] {
            "A", "B", "C", "D", "E", "F", "G", "H", "I"}),
        (n, m) -> {
          if ("A".equals(n) && "B".equals(m) || "B".equals(n) && "A".equals(m))
            return 3;
          if ("A".equals(n) && "C".equals(m) || "C".equals(n) && "A".equals(m))
            return 5;
          if ("A".equals(n) && "D".equals(m) || "D".equals(n) && "A".equals(m))
            return 7;
          if ("B".equals(n) && "D".equals(m) || "D".equals(n) && "B".equals(m))
            return 1;
          if ("B".equals(n) && "E".equals(m) || "E".equals(n) && "B".equals(m))
            return 7;
          if ("C".equals(n) && "D".equals(m) || "D".equals(n) && "C".equals(m))
            return 3;
          if ("C".equals(n) && "F".equals(m) || "F".equals(n) && "C".equals(m))
            return 2;
          if ("D".equals(n) && "E".equals(m) || "E".equals(n) && "D".equals(m))
            return 2;
          if ("D".equals(n) && "F".equals(m) || "F".equals(n) && "D".equals(m))
            return 3;
          if ("D".equals(n) && "G".equals(m) || "G".equals(n) && "D".equals(m))
            return 1;
          if ("E".equals(n) && "G".equals(m) || "G".equals(n) && "E".equals(m))
            return 2;
          if ("E".equals(n) && "H".equals(m) || "H".equals(n) && "E".equals(m))
            return 1;
          if ("F".equals(n) && "G".equals(m) || "G".equals(n) && "F".equals(m))
            return 3;
          if ("F".equals(n) && "I".equals(m) || "I".equals(n) && "F".equals(m))
            return 4;
          if ("G".equals(n) && "H".equals(m) || "H".equals(n) && "G".equals(m))
            return 3;
          if ("G".equals(n) && "I".equals(m) || "I".equals(n) && "G".equals(m))
            return 2;
          if ("H".equals(n) && "I".equals(m) || "I".equals(n) && "H".equals(m))
            return 5;
          return Double.POSITIVE_INFINITY;
        }, prev);

    assertEquals(9, dist.size());
    assertEquals(0., dist.get("A"), 1e-5);
    assertEquals(3., dist.get("B"), 1e-5);
    assertEquals(5., dist.get("C"), 1e-5);
    assertEquals(4., dist.get("D"), 1e-5);
    assertEquals(6., dist.get("E"), 1e-5);
    assertEquals(7., dist.get("F"), 1e-5);
    assertEquals(5., dist.get("G"), 1e-5);
    assertEquals(7., dist.get("H"), 1e-5);
    assertEquals(7., dist.get("I"), 1e-5);

    assertEquals(9, prev.size());
    assertEquals(null, prev.get("A"));
    assertEquals("A", prev.get("B"));
    assertEquals("A", prev.get("C"));
    assertEquals("B", prev.get("D"));
    assertEquals("D", prev.get("E"));
    assertEquals("D", prev.get("F"));
    assertEquals("D", prev.get("G"));
    assertEquals("E", prev.get("H"));
    assertEquals("G", prev.get("I"));
  }

  @Test public void testMaxFlowOnSingleEdgeGraph() {
    ToDoubleBiFunction<Integer, Integer> flow = GraphUtils.maxFlow(1, 2,
        Arrays.asList(1, 2),
        (n, m) -> (n.equals(1) && m.equals(2)) ? 3. : 0.);
    assertEquals(3., flow.applyAsDouble(1, 2), 1e-5);
    assertEquals(0., flow.applyAsDouble(2, 1), 1e-5);
  }

  @Test public void testMaxFlowOnMediumExample() {
    ToDoubleBiFunction<Integer, Integer> flow = GraphUtils.maxFlow(1, 4,
        Arrays.asList(1, 2, 3, 4),
        (n, m) -> {
          if (n.equals(1) && m.equals(2))
            return 2.;
          if (n.equals(1) && m.equals(3))
            return 4.;
          if (n.equals(2) && m.equals(3))
            return 3.;
          if (n.equals(2) && m.equals(4))
            return 1.;
          if (n.equals(3) && m.equals(4))
            return 5.;
          return 0.;
        });
    assertEquals(2., flow.applyAsDouble(1, 2), 1e-5);
    assertEquals(4., flow.applyAsDouble(1, 3), 1e-5);
    assertEquals(1., flow.applyAsDouble(2, 3), 1e-5);
    assertEquals(1., flow.applyAsDouble(2, 4), 1e-5);
    assertEquals(5., flow.applyAsDouble(3, 4), 1e-5);
  }

  @Test public void testMaxFlowWithBacktracking() {
    // Start with a feasible flow that forces some removal
    ToDoubleBiFunction<Integer, Integer> flow = GraphUtils.maxFlow(1, 4,
        Arrays.asList(1, 2, 3, 4),
        (n, m) -> 0.,
        (n, m) -> {
          if (n.equals(1) && m.equals(2))
            return 2.;
          if (n.equals(1) && m.equals(3))
            return 4.;
          if (n.equals(2) && m.equals(3))
            return 3.;
          if (n.equals(2) && m.equals(4))
            return 1.;
          if (n.equals(3) && m.equals(4))
            return 5.;
          return 0.;
        }, (n,m) -> {
          if (n.equals(1) && m.equals(2))
            return 2.;
          if (n.equals(2) && m.equals(3))
            return 2.;
          if (n.equals(3) && m.equals(4))
            return 2.;
          return 0.;
        });
    assertEquals(2., flow.applyAsDouble(1, 2), 1e-5);
    assertEquals(4., flow.applyAsDouble(1, 3), 1e-5);
    assertEquals(1., flow.applyAsDouble(2, 3), 1e-5);
    assertEquals(1., flow.applyAsDouble(2, 4), 1e-5);
    assertEquals(5., flow.applyAsDouble(3, 4), 1e-5);
  }

}
