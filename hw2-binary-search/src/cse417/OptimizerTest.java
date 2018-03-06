package cse417;

import java.util.function.DoubleFunction;

import org.junit.Test;
import static org.junit.Assert.*;


public class OptimizerTest {

  @Test
  public void testFindMinimum() {
    DoubleFunction<Double> f = x -> Math.abs(x);
    assertEquals(0, Optimizer.findMinimumOfUnimodal(f, -1, 1), 1e-5);
    assertEquals(0, Optimizer.findMinimumOfUnimodal(f, -10, 1), 1e-5);
    assertEquals(0, Optimizer.findMinimumOfUnimodal(f, -1, 10), 1e-5);

    DoubleFunction<Double> g = x -> Math.abs(x-1);
    assertEquals(1, Optimizer.findMinimumOfUnimodal(g, 0, 2), 1e-5);
    assertEquals(1, Optimizer.findMinimumOfUnimodal(g, 0, 10), 1e-5);

    DoubleFunction<Double> h = x -> (x+3) * (x+3);
    assertEquals(-3, Optimizer.findMinimumOfUnimodal(h, -5, 5), 1e-5);
    assertEquals(-3, Optimizer.findMinimumOfUnimodal(h, -25, 25), 1e-5);
    assertEquals(-3, Optimizer.findMinimumOfUnimodal(h, -25, 25, 1e-10), 1e-10);
  }

  @Test
  public void testFindMaximum() {
    DoubleFunction<Double> h = x -> -(x-3) * (x-3);
    assertEquals(3, Optimizer.findMaximumOfUnimodal(h, -5, 5), 1e-5);
    assertEquals(3, Optimizer.findMaximumOfUnimodal(h, -25, 25), 1e-5);
    assertEquals(3, Optimizer.findMaximumOfUnimodal(h, -25, 25, 1e-10), 1e-10);
  }
}
