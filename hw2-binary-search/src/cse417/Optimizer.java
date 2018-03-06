package cse417;

import java.util.function.DoubleFunction;


/**
 * Provides utility methods for finding the minimum or maximum of unimodal
 * functions. When looking for the minimum, by unimodal, we mean that the
 * function must have a <b>unique</b> local minimum, whereas when looking for
 * the maximum, to be unimodal, the function must have a unique local maximum.
 */
public class Optimizer {

  /** The default for the maximum error in estimating the optimum. */
  public static double DEFAULT_TOLERANCE = 1e-12;

  /** 
   * Returns a point close to the minimum of the given function on doubles,
   * which must be unimodal with a minimum lying between {@code a} and {@code b}
   * By "close", we mean within a distance of {@code DEFAULT_TOLERANCE}.
   */
  public static double findMinimumOfUnimodal(
      DoubleFunction<Double> f, double a, double b) {
    return findMinimumOfUnimodal(f, a, b, DEFAULT_TOLERANCE);
  }

  /** 
   * Returns a point within a distance of {@code tol} of the minimum of the
   * given function on doubles. The function must be unimodal with its minimum
   * lying between {@code a} and {@code b}.
   */
  public static double findMinimumOfUnimodal(
      DoubleFunction<Double> f, double a, double b, double tol) {
    assert a <= b;

    // Maintains the invariant that the minimu lies in [a,b]. Each iteration
    // shrinks the distance between these points.
    while (b - a > tol) {
      double x1 = a + (b - a) / 3;
      double x2 = a + 2 * (b - a) / 3;
      if (f.apply(x1) < f.apply(x2)) {
        b = x2;
      } else {
        a = x1;
      }
    }

    return (a + b) / 2;
  }

  /** 
   * Returns a point close to the maximum of the given function on doubles,
   * which must be unimodal with a maximum lying between {@code a} and {@code b}
   * By "close", we mean within a distance of {@code DEFAULT_TOLERANCE}.
   */
  public static double findMaximumOfUnimodal(
      DoubleFunction<Double> f, double a, double b) {
    return findMaximumOfUnimodal(f, a, b, DEFAULT_TOLERANCE);
  }

  /** 
   * Returns a point within a distance of {@code tol} of the maximum of the
   * given function on doubles. The function must be unimodal with its maximum
   * lying between {@code a} and {@code b}.
   */
  public static double findMaximumOfUnimodal(
      DoubleFunction<Double> f, double a, double b, double tol) {
    return findMinimumOfUnimodal(x -> -f.apply(x), a, b, tol);
  }
}
