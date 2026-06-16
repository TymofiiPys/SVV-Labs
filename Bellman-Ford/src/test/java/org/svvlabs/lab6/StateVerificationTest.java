package org.svvlabs.lab6;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.svvlabs.lab1.BellmanFordSP;
import org.svvlabs.lab1.Edge;
import org.svvlabs.lab1.Graph;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>State verification</b> tests for {@link Graph}, {@link Edge} and
 * {@link BellmanFordSP}.
 *
 * <p>State verification means we examine the <em>observable state</em> of
 * objects after performing operations — no mocks, no interaction assertions.
 * We check:
 * <ul>
 *   <li>Graph field values after construction ({@code vertices}, {@code edges})</li>
 *   <li>Edge field values after {@code addEdge}</li>
 *   <li>Distance array contents returned by {@link BellmanFordSP#findShortestPaths}</li>
 *   <li>Exception state (type + message) on negative-weight cycles</li>
 * </ul>
 */
@DisplayName("State verification")
public class StateVerificationTest {

    // =========================================================================
    // Graph & Edge construction state
    // =========================================================================

    @Nested
    @DisplayName("Graph state after construction")
    class GraphConstructionState {

        @Test
        @DisplayName("vertex count is stored correctly")
        void vertexCountMatchesConstructorArgument() {
            Graph g = new Graph(7);
            assertEquals(7, g.vertices,
                    "graph.vertices must equal the constructor argument");
        }

        @Test
        @DisplayName("edge list is initialised and empty")
        void edgeListIsInitialisedEmpty() {
            Graph g = new Graph(3);
            assertNotNull(g.edges, "edge list must not be null");
            assertTrue(g.edges.isEmpty(), "edge list must be empty after construction");
        }

        @Test
        @DisplayName("edge fields are stored correctly after addEdge")
        void edgeFieldsStoredAfterAddEdge() {
            Graph g = new Graph(4);
            g.addEdge(1, 3, -7);

            assertEquals(1,  g.edges.size(), "exactly one edge should be present");
            Edge e = g.edges.get(0);
            assertEquals(1,  e.source,      "source must be 1");
            assertEquals(3,  e.destination, "destination must be 3");
            assertEquals(-7, e.weight,      "weight must be -7");
        }

        @Test
        @DisplayName("multiple edges are all stored in insertion order")
        void multipleEdgesStoredInOrder() {
            Graph g = new Graph(5);
            g.addEdge(0, 1, 2);
            g.addEdge(0, 2, 4);
            g.addEdge(1, 3, 6);

            assertEquals(3, g.edges.size());
            assertEquals(0, g.edges.get(0).source);      // edge 0: 0→1
            assertEquals(1, g.edges.get(0).destination);
            assertEquals(0, g.edges.get(1).source);      // edge 1: 0→2
            assertEquals(2, g.edges.get(1).destination);
            assertEquals(1, g.edges.get(2).source);      // edge 2: 1→3
            assertEquals(3, g.edges.get(2).destination);
        }

        @Test
        @DisplayName("negative-weight edge is stored without modification")
        void negativeWeightStoredAsIs() {
            Graph g = new Graph(2);
            g.addEdge(0, 1, -999);
            assertEquals(-999, g.edges.get(0).weight);
        }
    }

    // =========================================================================
    // BellmanFordSP result state
    // =========================================================================

    @Nested
    @DisplayName("BellmanFordSP result state")
    class BellmanFordResultState {

        private Graph graph;

        @BeforeEach
        void buildGraph() {
            // Topology: 0→1(4), 0→2(2), 1→3(2), 1→4(3), 2→1(1), 2→3(4), 2→4(5)
            // Expected from source 0: [0, 3, 2, 5, 6]
            graph = new Graph(5);
            graph.addEdge(0, 1, 4);
            graph.addEdge(0, 2, 2);
            graph.addEdge(1, 3, 2);
            graph.addEdge(1, 4, 3);
            graph.addEdge(2, 1, 1);
            graph.addEdge(2, 3, 4);
            graph.addEdge(2, 4, 5);
        }

        @Test
        @DisplayName("result array has length equal to vertex count")
        void resultArrayLengthEqualsVertexCount() {
            int[] d = BellmanFordSP.findShortestPaths(graph, 0);
            assertEquals(graph.vertices, d.length);
        }

        @Test
        @DisplayName("source vertex distance is always 0")
        void sourceDistanceIsZero() {
            int[] d = BellmanFordSP.findShortestPaths(graph, 0);
            assertEquals(0, d[0], "distance from source to itself must be 0");
        }

        @Test
        @DisplayName("all distances match expected shortest-path values")
        void allDistancesCorrect() {
            int[] expected = {0, 3, 2, 5, 6};
            int[] actual   = BellmanFordSP.findShortestPaths(graph, 0);
            assertArrayEquals(expected, actual);
        }

        @Test
        @DisplayName("unreachable vertices keep Integer.MAX_VALUE")
        void unreachableVerticesRemainMaxValue() {
            Graph disconnected = new Graph(4);
            disconnected.addEdge(0, 1, 5);
            disconnected.addEdge(2, 3, 2); // component separate from 0–1

            int[] d = BellmanFordSP.findShortestPaths(disconnected, 0);
            assertEquals(Integer.MAX_VALUE, d[2], "vertex 2 is unreachable");
            assertEquals(Integer.MAX_VALUE, d[3], "vertex 3 is unreachable");
        }

        @Test
        @DisplayName("negative-weight edges produce correct final state")
        void negativeWeightsYieldCorrectDistances() {
            Graph ng = new Graph(3);
            ng.addEdge(0, 1, -4);
            ng.addEdge(1, 2,  3);
            ng.addEdge(0, 2, 10);

            int[] d = BellmanFordSP.findShortestPaths(ng, 0);
            assertArrayEquals(new int[]{0, -4, -1}, d,
                    "path 0→1→2 (cost -1) must beat direct edge (cost 10)");
        }

        @Test
        @DisplayName("single-vertex graph returns [0]")
        void singleVertexGraph() {
            int[] d = BellmanFordSP.findShortestPaths(new Graph(1), 0);
            assertArrayEquals(new int[]{0}, d);
        }
    }

    // =========================================================================
    // Exception state on negative-weight cycle
    // =========================================================================

    @Nested
    @DisplayName("Exception state on negative-weight cycle")
    class NegativeCycleExceptionState {

        @Test
        @DisplayName("exception type is IllegalArgumentException")
        void exceptionTypeIsIllegalArgumentException() {
            Graph g = cyclicGraph();
            assertThrows(IllegalArgumentException.class,
                    () -> BellmanFordSP.findShortestPaths(g, 0));
        }

        @Test
        @DisplayName("exception message matches expected constant")
        void exceptionMessageIsCorrect() {
            Graph g = cyclicGraph();
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> BellmanFordSP.findShortestPaths(g, 0));

            assertEquals("Graph contains a negative-weight cycle.", ex.getMessage());
        }

        @Test
        @DisplayName("no exception is thrown for a zero-weight cycle")
        void zeroWeightCycleDoesNotThrow() {
            Graph g = new Graph(3);
            g.addEdge(0, 1,  1);
            g.addEdge(1, 2, -1);
            g.addEdge(2, 0,  0); // cycle weight = 0

            assertDoesNotThrow(() -> BellmanFordSP.findShortestPaths(g, 0));
        }

        // ---- helper ----
        private Graph cyclicGraph() {
            // Cycle 1→2(-1), 2→3(-1), 3→1(-1), total = -3
            Graph g = new Graph(4);
            g.addEdge(0, 1,  1);
            g.addEdge(1, 2, -1);
            g.addEdge(2, 3, -1);
            g.addEdge(3, 1, -1);
            return g;
        }
    }

    // =========================================================================
    // ShortestPathService result state (real collaborators, no mocks)
    // =========================================================================

    @Nested
    @DisplayName("ShortestPathService result state (in-memory repository)")
    class ServiceResultState {

        /**
         * Minimal in-memory implementation of {@link GraphRepository} used in
         * state-verification tests so we can construct a real service without mocking.
         */
        static class InMemoryGraphRepository implements GraphRepository {
            private Graph stored;
            @Override public void save(int id, Graph graph) { this.stored = graph; }
            @Override public Graph findById(int id)         { return stored; }
        }

        /**
         * No-op logger; state verification only cares about the distance array.
         */
        static class NoOpLogger implements ComputationLogger {
            @Override public void logSuccess(int source, int[] distances) {}
            @Override public void logError(String msg) {}
        }

        private ShortestPathService service;

        @BeforeEach
        void setUp() {
            service = new ShortestPathService(new InMemoryGraphRepository(), new NoOpLogger());
        }

        @Test
        @DisplayName("service returns correct distances for a positive-weight graph")
        void serviceReturnsCorrectDistances() {
            Graph g = new Graph(4);
            g.addEdge(0, 1, 1);
            g.addEdge(0, 2, 4);
            g.addEdge(1, 2, 2);
            g.addEdge(1, 3, 5);
            g.addEdge(2, 3, 1);

            int[] result = service.computeShortestPaths(1, g, 0);

            assertArrayEquals(new int[]{0, 1, 3, 4}, result);
        }

        @Test
        @DisplayName("service re-throws IllegalArgumentException on negative cycle")
        void serviceRethrowsOnNegativeCycle() {
            Graph g = new Graph(3);
            g.addEdge(0, 1,  1);
            g.addEdge(1, 2, -5);
            g.addEdge(2, 0,  2);

            assertThrows(IllegalArgumentException.class,
                    () -> service.computeShortestPaths(99, g, 0));
        }

        @Test
        @DisplayName("service constructor rejects null repository")
        void constructorRejectsNullRepository() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ShortestPathService(null, new NoOpLogger()));
        }

        @Test
        @DisplayName("service constructor rejects null logger")
        void constructorRejectsNullLogger() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ShortestPathService(new InMemoryGraphRepository(), null));
        }
    }
}
