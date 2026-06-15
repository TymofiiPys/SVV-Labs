package org.svvlabs.lab3;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.svvlabs.lab1.BellmanFordSP;
import org.svvlabs.lab1.Graph;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ordered unit tests for {@link Graph} and {@link BellmanFordSP}.
 *
 * <p>Tests are executed in the logical order defined by {@link Order}:
 * <ol>
 *   <li>Graph construction and edge management</li>
 *   <li>Shortest paths – positive weights</li>
 *   <li>Shortest paths – negative weights (no cycle)</li>
 *   <li>Shortest paths – varying source vertices</li>
 *   <li>Shortest paths – edge cases (single node, no edges, disconnected)</li>
 *   <li>Error cases – negative-weight cycle detection</li>
 * </ol>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BellmanFordOrderedTest {

    // =========================================================================
    // Graph construction
    // =========================================================================

    /**
     * A newly created graph must have the specified vertex count and an empty
     * edge list.
     */
    @Test
    @Order(1)
    public void testGraphInitialization() {
        Graph graph = new Graph(4);

        assertEquals(4, graph.vertices,
                "Vertex count should match the constructor argument.");
        assertNotNull(graph.edges,
                "Edge list should be initialized, not null.");
        assertTrue(graph.edges.isEmpty(),
                "Edge list should be empty upon construction.");
    }

    /**
     * Adding a single edge must be reflected in the edge list with correct
     * source, destination and weight values.
     */
    @Test
    @Order(2)
    public void testAddSingleEdge() {
        Graph graph = new Graph(3);
        graph.addEdge(0, 2, 7);

        assertEquals(1, graph.edges.size(),
                "Edge list should contain exactly one edge.");
        assertEquals(0, graph.edges.get(0).source,
                "Edge source should be 0.");
        assertEquals(2, graph.edges.get(0).destination,
                "Edge destination should be 2.");
        assertEquals(7, graph.edges.get(0).weight,
                "Edge weight should be 7.");
    }

    /**
     * Multiple edges can be added sequentially; the size and order of the
     * edge list must be maintained.
     */
    @Test
    @Order(3)
    public void testAddMultipleEdges() {
        Graph graph = new Graph(3);
        graph.addEdge(0, 1, 5);
        graph.addEdge(1, 2, 3);
        graph.addEdge(0, 2, 10);

        assertEquals(3, graph.edges.size(),
                "Edge list should contain three edges.");
    }

    /**
     * Edges with negative weights are valid input and must be stored without
     * modification.
     */
    @Test
    @Order(4)
    public void testAddEdgeWithNegativeWeight() {
        Graph graph = new Graph(2);
        graph.addEdge(0, 1, -8);

        assertEquals(1, graph.edges.size());
        assertEquals(-8, graph.edges.get(0).weight,
                "Negative weight must be stored as-is.");
    }

    // =========================================================================
    // Bellman-Ford – positive weights
    // =========================================================================

    /**
     * Classic graph with only positive weights.
     * Topology: 0→1(4), 0→2(2), 1→3(2), 1→4(3), 2→1(1), 2→3(4), 2→4(5)
     * Expected from source 0: [0, 3, 2, 5, 6]
     */
    @Test
    @Order(5)
    public void testPositiveWeights() {
        Graph graph = new Graph(5);
        graph.addEdge(0, 1, 4);
        graph.addEdge(0, 2, 2);
        graph.addEdge(1, 3, 2);
        graph.addEdge(1, 4, 3);
        graph.addEdge(2, 1, 1);
        graph.addEdge(2, 3, 4);
        graph.addEdge(2, 4, 5);

        int[] expected = {0, 3, 2, 5, 6};
        int[] actual = BellmanFordSP.findShortestPaths(graph, 0);

        assertArrayEquals(expected, actual,
                "Distances should match the shortest paths for positive weights.");
    }

    /**
     * A direct path is not necessarily shortest; the algorithm must prefer a
     * longer multi-hop route when it yields a lower total cost.
     * Topology: 0→1(10), 0→2(1), 2→1(1)
     * Expected: 0→1 via 0→2→1 costs 2, not 10.
     */
    @Test
    @Order(6)
    public void testShortestPathPreferredOverDirect() {
        Graph graph = new Graph(3);
        graph.addEdge(0, 1, 10);
        graph.addEdge(0, 2, 1);
        graph.addEdge(2, 1, 1);

        int[] expected = {0, 2, 1};
        int[] actual = BellmanFordSP.findShortestPaths(graph, 0);

        assertArrayEquals(expected, actual,
                "Indirect route 0→2→1 (cost 2) must beat the direct edge (cost 10).");
    }

    // =========================================================================
    // Bellman-Ford – negative weights (no cycle)
    // =========================================================================

    /**
     * Negative-weight edges must be relaxed correctly.
     * Topology from BellmanFordSPTest: 0→1(-1), 0→2(4), 1→2(3), 1→3(2),
     *   1→4(2), 3→2(5), 3→1(1), 4→3(-3)
     * Expected from source 0: [0, -1, 2, -2, 1]
     */
    @Test
    @Order(7)
    public void testNegativeWeightsWithoutCycle() {
        Graph graph = new Graph(5);
        graph.addEdge(0, 1, -1);
        graph.addEdge(0, 2, 4);
        graph.addEdge(1, 2, 3);
        graph.addEdge(1, 3, 2);
        graph.addEdge(1, 4, 2);
        graph.addEdge(3, 2, 5);
        graph.addEdge(3, 1, 1);
        graph.addEdge(4, 3, -3);

        int[] expected = {0, -1, 2, -2, 1};
        int[] actual = BellmanFordSP.findShortestPaths(graph, 0);

        assertArrayEquals(expected, actual,
                "Distances should accurately calculate paths involving negative edges.");
    }

    /**
     * A negative edge from source to destination must override any longer
     * positive direct route.
     * Topology: 0→1(-4), 1→2(3), 0→2(10)
     * Expected: [0, -4, -1]  (path via 1 costs -4+3=-1, beats direct 10)
     */
    @Test
    @Order(8)
    public void testNegativeEdgeBeatsPositiveDirect() {
        Graph graph = new Graph(3);
        graph.addEdge(0, 1, -4);
        graph.addEdge(1, 2, 3);
        graph.addEdge(0, 2, 10);

        int[] expected = {0, -4, -1};
        int[] actual = BellmanFordSP.findShortestPaths(graph, 0);

        assertArrayEquals(expected, actual,
                "Path 0→1→2 (cost -1) must beat the direct edge 0→2 (cost 10).");
    }

    // =========================================================================
    // Bellman-Ford – varying source vertex
    // =========================================================================

    /**
     * When the source is an interior vertex, distances to vertices with no
     * incoming path from that source must remain at {@link Integer#MAX_VALUE}.
     * Topology: 0→1(1), 1→2(2); source = 1
     * Expected: [MAX_VALUE, 0, 2]  (vertex 0 is unreachable from 1)
     */
    @Test
    @Order(9)
    public void testNonZeroSourceVertex() {
        Graph graph = new Graph(3);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 2);

        int[] expected = {Integer.MAX_VALUE, 0, 2};
        int[] actual = BellmanFordSP.findShortestPaths(graph, 1);

        assertArrayEquals(expected, actual,
                "Vertex 0 is unreachable from source 1 and must stay at MAX_VALUE.");
    }

    /**
     * Using the last vertex as source should correctly compute distances
     * only to reachable successors.
     * Topology: 0→1(3), 1→2(4), 2→3(5); source = 2
     * Expected: [MAX_VALUE, MAX_VALUE, 0, 5]
     */
    @Test
    @Order(10)
    public void testLastVertexAsSource() {
        Graph graph = new Graph(4);
        graph.addEdge(0, 1, 3);
        graph.addEdge(1, 2, 4);
        graph.addEdge(2, 3, 5);

        int[] expected = {Integer.MAX_VALUE, Integer.MAX_VALUE, 0, 5};
        int[] actual = BellmanFordSP.findShortestPaths(graph, 2);

        assertArrayEquals(expected, actual,
                "Only vertex 3 is reachable from source 2.");
    }

    // =========================================================================
    // Bellman-Ford – edge cases
    // =========================================================================

    /**
     * A graph with a single vertex and no edges has only one distance: 0 to
     * itself.
     */
    @Test
    @Order(11)
    public void testSingleNode() {
        Graph graph = new Graph(1);

        int[] expected = {0};
        int[] actual = BellmanFordSP.findShortestPaths(graph, 0);

        assertArrayEquals(expected, actual,
                "A single node graph should return a distance of 0 to itself.");
    }

    /**
     * A graph with two vertices but no edges: source distance is 0, the other
     * vertex is unreachable.
     */
    @Test
    @Order(12)
    public void testTwoVerticesNoEdges() {
        Graph graph = new Graph(2);

        int[] expected = {0, Integer.MAX_VALUE};
        int[] actual = BellmanFordSP.findShortestPaths(graph, 0);

        assertArrayEquals(expected, actual,
                "Vertex 1 has no incoming edge and must remain at MAX_VALUE.");
    }

    /**
     * In a disconnected graph, only vertices reachable from the source get
     * finite distances; all others remain at {@link Integer#MAX_VALUE}.
     * Topology: 0→1(5), 2→3(2); source = 0
     * Expected: [0, 5, MAX_VALUE, MAX_VALUE]
     */
    @Test
    @Order(13)
    public void testDisconnectedGraph() {
        Graph graph = new Graph(4);
        graph.addEdge(0, 1, 5);
        // Vertices 2 and 3 are disconnected from 0 and 1
        graph.addEdge(2, 3, 2);

        int[] expected = {0, 5, Integer.MAX_VALUE, Integer.MAX_VALUE};
        int[] actual = BellmanFordSP.findShortestPaths(graph, 0);

        assertArrayEquals(expected, actual,
                "Unreachable vertices should remain at Integer.MAX_VALUE.");
    }

    /**
     * A graph with parallel edges (multiple edges between the same pair of
     * vertices) must keep only the shortest cumulative path.
     * Topology: 0→1(10), 0→1(2); source = 0
     * Expected: [0, 2]
     */
    @Test
    @Order(14)
    public void testParallelEdges() {
        Graph graph = new Graph(2);
        graph.addEdge(0, 1, 10);
        graph.addEdge(0, 1, 2);

        int[] expected = {0, 2};
        int[] actual = BellmanFordSP.findShortestPaths(graph, 0);

        assertArrayEquals(expected, actual,
                "The lighter parallel edge (weight 2) must win.");
    }

    /**
     * A complete 3-vertex directed graph must compute all pairwise shortest
     * paths from vertex 0.
     * Topology: 0→1(1), 0→2(5), 1→2(2); source = 0
     * Expected: [0, 1, 3]  (0→2 via 1 costs 1+2=3, beats direct 5)
     */
    @Test
    @Order(15)
    public void testCompleteGraphShortestPaths() {
        Graph graph = new Graph(3);
        graph.addEdge(0, 1, 1);
        graph.addEdge(0, 2, 5);
        graph.addEdge(1, 2, 2);

        int[] expected = {0, 1, 3};
        int[] actual = BellmanFordSP.findShortestPaths(graph, 0);

        assertArrayEquals(expected, actual,
                "Indirect route 0→1→2 (cost 3) must beat direct edge 0→2 (cost 5).");
    }

    // =========================================================================
    // Bellman-Ford – negative-weight cycle detection
    // =========================================================================

    /**
     * A negative-weight cycle must cause {@link BellmanFordSP#findShortestPaths}
     * to throw {@link IllegalArgumentException} with the expected message.
     * Cycle: 1→2(-1), 2→3(-1), 3→1(-1) totals -3.
     */
    @Test
    @Order(16)
    public void testNegativeWeightCycleDetection() {
        Graph graph = new Graph(4);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, -1);
        graph.addEdge(2, 3, -1);
        graph.addEdge(3, 1, -1); // negative cycle: 1→2→3→1, total weight -3

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> BellmanFordSP.findShortestPaths(graph, 0));

        assertEquals("Graph contains a negative-weight cycle.", exception.getMessage(),
                "Algorithm must detect and throw an exception for negative-weight cycles.");
    }

    /**
     * A cycle whose total weight is exactly zero is not a negative cycle and
     * must not trigger the exception.
     * Cycle: 0→1(1), 1→2(-1), 2→0(0) totals 0.
     */
    @Test
    @Order(17)
    public void testZeroWeightCycleDoesNotThrow() {
        Graph graph = new Graph(3);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, -1);
        graph.addEdge(2, 0, 0); // cycle weight = 1 + (-1) + 0 = 0

        assertDoesNotThrow(() -> BellmanFordSP.findShortestPaths(graph, 0),
                "A zero-weight cycle must not be treated as a negative-weight cycle.");
    }

    /**
     * A cycle with a large positive total weight must not trigger the exception.
     * Cycle: 0→1(5), 1→2(3), 2→0(4) totals +12.
     */
    @Test
    @Order(18)
    public void testPositiveCycleDoesNotThrow() {
        Graph graph = new Graph(3);
        graph.addEdge(0, 1, 5);
        graph.addEdge(1, 2, 3);
        graph.addEdge(2, 0, 4); // positive cycle, total weight = +12

        assertDoesNotThrow(() -> BellmanFordSP.findShortestPaths(graph, 0),
                "A positive-weight cycle must not trigger the negative-cycle exception.");
    }
}
