package cse417;

import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;


/** Provides some useful operations on graphs. */
public class GraphUtils {

  /**
   * Returns the shortest path in terms of number of edges between the two
   * given nodes.
   * @param source The node at which the path should start.
   * @param target The node at which the path should end.
   * @param nodes List of all the nodes in the graph.
   * @param adjacentNodes Function mapping a node to the list of nodes
   *     connected to it by an edge.
   * @returns A list of edges in the path from start to end (including both) or
   *     null if there is no path.
   */
  public static <N> List<N> shortestPath(N source, N target,
      Collection<N> nodes, Function<N, Collection<N>> adjacentNodes) {

    Map<N, N> prev = new HashMap<N, N>();  // Map to parent in BFS tree
    prev.put(source, null);

    Queue<N> queue = new LinkedList<N>();    
    queue.add(source);

    while (!queue.isEmpty()) {
      N node = queue.poll();
      for (N m : adjacentNodes.apply(node)) {
        if (!prev.containsKey(m)) {
          prev.put(m, node);
          queue.add(m);
        }
      }
    }

    if (!prev.containsKey(target)) {
      return null;
    } else {
      List<N> path = new ArrayList<N>();
      path.add(target);
      while (!source.equals(path.get(path.size()-1)))
        path.add(prev.get(path.get(path.size()-1)));

      Collections.reverse(path);
      return path;
    }
  }

  /**
   * Returns the least cost paths from the given node to every other node. The
   * information about the graph is provided in {@code nodes} and {@code cost}
   * (edge costs). <b>All costs must be non-negative</b>.
   * <p>
   * This method assumes that the graph is complete, so {@code cost} must
   * return a value for any pair of edges. There is little restriction here
   * since it can return Double.POSITIVE_INFINITY. However, this does mean that
   * the running time is O(n^2), where n is the number of edges, whereas O(m)
   * is achievable in sparse graphs with only m edges.
   *
   * @param source The node at which to start paths.
   * @param nodes List of all the nodes in the graph.
   * @param cost Function mapping pairs of nodes to the edge cost between them
   * @param prev If non-null, a map in which the shortest paths will be stored.
   *   Specifically, prev.get(n) will be the previous node on the shortest path
   *   from source to n or null if n is source.
   * @returns A map from node to the distance of the shortest path to there.
   *   This will be Double.POSITIVE_INFINITY if there is no path to that node.
   */
  public static <N> Map<N, Double> leastCostPaths(N source,
      Collection<N> nodes, ToDoubleBiFunction<N,N> cost, Map<N, N> prev) {

    final Map<N, Double> dist = new HashMap<N, Double>();
    for (N n : nodes)
      dist.put(n, Double.POSITIVE_INFINITY);

    Set<N> finished = new HashSet<N>();
    PriorityQueue<Path<N>> active = new PriorityQueue<Path<N>>(nodes.size(),
        (p, q) -> Double.compare(p.distance, q.distance));
    active.add(new Path<N>(source, null, 0.));

    while (!active.isEmpty()) {
      Path<N> p = active.poll();
      if (finished.contains(p.target))
        continue;
      finished.add(p.target);

      dist.put(p.target, p.distance);
      if (prev != null)
        prev.put(p.target, p.previous);

      for (N n : nodes) {
        if (!finished.contains(n)) {
          double newDist = p.distance + cost.applyAsDouble(p.target, n);
          active.add(new Path<N>(n, p.target, newDist));
        }
      }
    }

    return dist;
  }

  /**
   * Returns the least cost paths from the given node to every other node. The
   * cost of the edge between nodes is given in {@code cost}.
   * @param source The node at which to start paths.
   * @param nodes List of all the nodes in the graph.
   * @param cost Function mapping a pair of nodes to the edge cost between them
   * @returns A map from node to the cost of the least cost  path to that node
   */
  public static <N> Map<N, Double> leastCostPaths(N source,
      Collection<N> nodes, ToDoubleBiFunction<N,N> cost) {
    return leastCostPaths(source, nodes, cost, null);
  }

  /**
   * Computes a feasible flow that achieves the maximum possible flow out of
   * the source subject to upper bounds on the flow of each edge.
   * @param source Node from which flow originates.
   * @param target Node at which flow terminates.
   * @param nodes List of all nodes in the graph.
   * @param capacity Function returning the maximum flow on each edge.
   * @returns The maximum flow subject to the capacity constraints.
   */
  public static <N> ToDoubleBiFunction<N, N> maxFlow(N source, N target,
      Collection<N> nodes, ToDoubleBiFunction<N, N> capacity) {
    return maxFlow(source, target, nodes, (n, m) -> 0., capacity, (n, m) -> 0.);
  }

  /**
   * Computes a feasible flow that achieves the maximum possible flow out of
   * the source subject to upper and lower bounds on the flow of each edge.
   * This method requires a feasible flow as input in order to get started.
   * @param source Node from which flow originates.
   * @param target Node at which flow terminates.
   * @param nodes List of all nodes in the graph.
   * @param minEdgeFlow Function returning the minimum flow on each edge.
   *     Each value must be non-negative.
   * @param maxEdgeFlow Function returning the maximum flow on each edge.
   *     Each value must be greater than or equal to the corresponding minimum.
   * @param feasibleFlow Some flow that satisfies the edge constraints given.
   * @returns The maximum flow subject to the given constraints
   */
  public static <N> ToDoubleBiFunction<N, N> maxFlow(
      N source, N target, final Collection<N> nodes,
      ToDoubleBiFunction<N, N> minEdgeFlow,
      ToDoubleBiFunction<N, N> maxEdgeFlow,
      ToDoubleBiFunction<N, N> feasibleFlow) {

    // Check the constraints are reasonable.
    for (N n : nodes) {
      for (N m : nodes) {
        if (!n.equals(m)) {
          assert 0 <= minEdgeFlow.applyAsDouble(n, m);
          assert minEdgeFlow.applyAsDouble(n, m) <=
              maxEdgeFlow.applyAsDouble(n, m);
        }
      }
    }

    // Check that flow is actually feasible.
    for (N n : nodes) {
      if (!n.equals(source) && !n.equals(target))
        assert Math.abs(imbalanceAt(n, nodes, feasibleFlow)) < EPSILON;
    }
    for (N n : nodes) {
      if (!n.equals(source)  && !n.equals(target)) {
        for (N m : nodes) {
          if (!n.equals(source)  && !m.equals(target)) {
            double flow = feasibleFlow.applyAsDouble(n, m);
            assert minEdgeFlow.applyAsDouble(n, m) <= flow &&
                   flow <= maxEdgeFlow.applyAsDouble(n, m);
          }
        }
      }
    }

    // Store the current flow in a map.
    final Map<Pair<N>, Double> flow = new HashMap<Pair<N>, Double>();
    for (N n : nodes) {
      for (N m : nodes) {
        if (!n.equals(m))
          flow.put(Pair.of(n, m), feasibleFlow.applyAsDouble(n, m));
      }
    }

    while (true) {
      // Compute the residual graph for the current flow.
      ToDoubleBiFunction<N, N> residual = (n, m) -> {
            return (maxEdgeFlow.applyAsDouble(n, m) - flow.get(Pair.of(n, m))) +
                (flow.get(Pair.of(m, n)) - minEdgeFlow.applyAsDouble(m, n));
          };

      // Attempt to find a path in the residual graph in which to increase flow
      List<N> augmentingPath = shortestPath(source, target, nodes, n -> {
            List<N> adjacent = new ArrayList<N>();
            for (N m : nodes) {
              if (!n.equals(m) && residual.applyAsDouble(n, m) >= EPSILON)
                adjacent.add(m);
            }
            return adjacent;
          });
      if (augmentingPath == null)
        break;  // if none exists, this is optimal

      // Find the largest amount that we can increase flow along this path.
      // Note: we can copy to an array list if augmentingPath is not one already
      double extra = Double.POSITIVE_INFINITY;
      for (int i = 1; i < augmentingPath.size(); i++) {
        N n = augmentingPath.get(i-1), m = augmentingPath.get(i);
        extra = Math.min(extra, residual.applyAsDouble(n, m));
      }
      assert extra >= EPSILON;

      // Increase the flow along this path.
      for (int i = 1; i < augmentingPath.size(); i++) {
        N n = augmentingPath.get(i-1), m = augmentingPath.get(i);

        Pair<N> p = Pair.of(n, m);
        double fwdExtra = Math.min(extra,
            maxEdgeFlow.applyAsDouble(n, m) - flow.get(p));
        flow.put(p, flow.get(p) + fwdExtra);

        Pair<N> q = Pair.of(m, n);
        double backExtra = Math.min(extra - fwdExtra,
            flow.get(q) - minEdgeFlow.applyAsDouble(m, n));
        flow.put(q, flow.get(q) - backExtra);

        assert Math.abs(fwdExtra + backExtra - extra) <= EPSILON;
      }
    }

    return (n, m) -> n.equals(m) ? 0. : flow.get(Pair.of(n, m));
  }

  /**
   * Returns the value of the given flow, i.e., the amount of flow going fom
   * source to target.
   */
  public static <N> double flowValue(N source, N target,
      Collection<N> nodes, ToDoubleBiFunction<N, N> flow) {
    return imbalanceAt(source, nodes, flow);
  }

  /**
   * Helper function that returns the flow imbalance &mdash; the amount that
   * incoming flow exceeds outgoing flow &mdash; at the given node.
   */
  public static <N> double imbalanceAt(
      N node, Collection<N> nodes, ToDoubleBiFunction<N, N> flow) {
    double s = 0;
    for (N n : nodes) {
      if (!node.equals(n))
        s += flow.applyAsDouble(n, node) - flow.applyAsDouble(node, n);
    }
    return s;
  }

  // TODO(future): minimum cost flow

  /** Numbers with absolute value below this are considered zero. */
  public static double EPSILON = 1e-8;

  /** Stores a pair of nodes. */
  private static class Pair<N> {
    public static <N> Pair<N> of(N first, N second) {
      return new Pair<N>(first, second);
    }

    public final N first;
    public final N second;

    private Pair(N first, N second) {
      this.first = first;
      this.second = second;
      assert !first.equals(second);  // we should never need to create these
    }

    @Override public boolean equals(Object o) {
      if (!(o instanceof Pair<?>))
        return false;
      Pair<?> p = (Pair<?>) o;
      return first.equals(p.first) && second.equals(p.second);
    }

    @Override public int hashCode() {
      return first.hashCode() ^ second.hashCode();
    }
  }

  /** Stores information about a path to a particular node. */
  private static class Path<N> {
    public final N target;
    public final N previous;
    public final double distance;

    public Path(N target, N previous, double distance) {
      this.target = target;
      this.previous = previous;
      this.distance = distance;
    }
  }

}
