package cse417;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.function.*;

import org.junit.Test;
import static org.junit.Assert.*;


public class TableRounderTest {

  @Test public void testFindFeasibleBoundedFlow() {
    List<Integer> nodes = Arrays.asList(new Integer[] {1, 2, 3, 4});
    ToDoubleBiFunction<Integer, Integer> f =
        TableRounder.findFeasibleBoundedFlow(1, 4, nodes,
            (a, b) -> (a == 3 && b == 4) ? 2.0 : 0.0,  // get 2 on (3,4) edge
            (a, b) -> (a == 3 && b == 4) ? 2.0 :
                (a == 1 && b == 4) ? 0.0 : (a < b) ? 1.0 : 0.0);

    for (int i = 1; i <= 4; i++)
      for (int j = 1; j <= 4; j++)
        if (i != j)
          System.err.printf("%d %d %.0f\n", i, j, f.applyAsDouble(i, j));

    assertEquals(1, f.applyAsDouble(1, 2), 1e-5);
    assertEquals(1, f.applyAsDouble(1, 3), 1e-5);
    assertEquals(0, f.applyAsDouble(1, 4), 1e-5);
    assertEquals(0, f.applyAsDouble(2, 1), 1e-5);
    assertEquals(1, f.applyAsDouble(2, 3), 1e-5);
    assertEquals(0, f.applyAsDouble(2, 4), 1e-5);
    assertEquals(0, f.applyAsDouble(3, 1), 1e-5);
    assertEquals(0, f.applyAsDouble(3, 2), 1e-5);
    assertEquals(2, f.applyAsDouble(3, 4), 1e-5);
    assertEquals(0, f.applyAsDouble(4, 2), 1e-5);
    assertEquals(0, f.applyAsDouble(4, 3), 1e-5);
  }


  @Test public void testFindFeasibleDemandFlow() {
    List<Integer> nodes = Arrays.asList(new Integer[] {1, 2, 3});
    ToDoubleBiFunction<Integer, Integer> f =
        TableRounder.findFeasibleDemandFlow(nodes,
            (a, b) -> 1.0,
            (a) -> (a == 1) ? 2 : -1);

    assertEquals(0, f.applyAsDouble(1, 2), 1e-5);
    assertEquals(0, f.applyAsDouble(1, 3), 1e-5);
    assertEquals(1, f.applyAsDouble(2, 1), 1e-5);
    assertEquals(0, f.applyAsDouble(2, 3), 1e-5);
    assertEquals(1, f.applyAsDouble(3, 1), 1e-5);
    assertEquals(0, f.applyAsDouble(3, 2), 1e-5);
  }

}
