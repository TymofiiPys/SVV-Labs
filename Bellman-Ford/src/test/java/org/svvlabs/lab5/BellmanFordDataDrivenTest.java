package org.svvlabs.lab5;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.svvlabs.lab1.BellmanFordSP;
import org.svvlabs.lab1.Graph;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Data-driven tests for {@link Graph} and {@link BellmanFordSP}.
 *
 * <p>Two external data sources are used:
 * <ul>
 *   <li><b>CSV</b> ({@code src/test/resources/lab5/graph_construction.csv}) –
 *       tabular cases for graph-construction assertions</li>
 *   <li><b>JSON</b> ({@code src/test/resources/lab5/shortest_paths_cases.json} and
 *       {@code negative_cycle_cases.json}) – structured graph cases loaded via
 *       {@link TestDataLoader} and fed through {@code @MethodSource}</li>
 * </ul>
 *
 * <p>No Spring context is started; only {@code org.svvlabs.lab1} classes are used.
 */
@DisplayName("Bellman-Ford data-driven tests")
public class BellmanFordDataDrivenTest {

    // =========================================================================
    // CSV-driven – graph construction
    // =========================================================================

    /**
     * Reads rows from {@code graph_construction.csv}.
     *
     * <p>Each row: {@code vertices, src, dst, weight, expectedEdgeCount}
     * <br>The test creates a graph, adds one edge, then verifies:
     * <ul>
     *   <li>vertex count is correct</li>
     *   <li>edge list has the expected size</li>
     *   <li>the stored source, destination and weight are correct</li>
     * </ul>
     */
    @ParameterizedTest(name = "[{index}] vertices={0} edge {1}→{2} w={3}")
    @DisplayName("Graph construction (CSV)")
    @CsvFileSource(resources = "/lab5/graph_construction.csv",
                   numLinesToSkip = 1)   // skip the header row
    public void testGraphConstruction(int vertices, int src, int dst,
                                      int weight, int expectedEdgeCount) {
        Graph graph = new Graph(vertices);
        graph.addEdge(src, dst, weight);

        assertNotNull(graph.edges,
                "Edge list must not be null after construction.");
        assertEquals(vertices, graph.vertices,
                "Vertex count must match constructor argument.");
        assertEquals(expectedEdgeCount, graph.edges.size(),
                "Edge list must contain exactly " + expectedEdgeCount + " edge(s).");
        assertEquals(src,    graph.edges.get(0).source,
                "Stored source vertex must match.");
        assertEquals(dst,    graph.edges.get(0).destination,
                "Stored destination vertex must match.");
        assertEquals(weight, graph.edges.get(0).weight,
                "Stored weight must match (including negative values).");
    }

    // =========================================================================
    // JSON-driven – shortest paths (happy path)
    // =========================================================================

    /**
     * Provides test cases from {@code shortest_paths_cases.json} via
     * {@link TestDataLoader#loadShortestPathCases()}.
     */
    static Stream<TestCase> shortestPathCases() {
        return TestDataLoader.loadShortestPathCases().stream();
    }

    /**
     * Builds a graph from the JSON data, runs Bellman-Ford, and asserts the
     * computed distances equal the expected distances stored in the file.
     *
     * <p>The test name in the report is the {@link TestCase#description} field
     * from the JSON, making failures immediately self-documenting.
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @DisplayName("Shortest paths (JSON)")
    @MethodSource("shortestPathCases")
    public void testShortestPaths(TestCase tc) {
        Graph graph = buildGraph(tc);

        int[] actual = BellmanFordSP.findShortestPaths(graph, tc.source);

        assertArrayEquals(tc.expectedDistances, actual,
                "Shortest distances do not match for case: " + tc.description);
    }

    // =========================================================================
    // JSON-driven – negative-weight cycle detection
    // =========================================================================

    /**
     * Provides test cases from {@code negative_cycle_cases.json} via
     * {@link TestDataLoader#loadNegativeCycleCases()}.
     */
    static Stream<TestCase> negativeCycleCases() {
        return TestDataLoader.loadNegativeCycleCases().stream();
    }

    /**
     * Asserts that {@link BellmanFordSP#findShortestPaths} throws
     * {@link IllegalArgumentException} with the canonical cycle-detection
     * message for every graph that contains a negative-weight cycle.
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @DisplayName("Negative-cycle detection (JSON)")
    @MethodSource("negativeCycleCases")
    public void testNegativeCycleDetection(TestCase tc) {
        Graph graph = buildGraph(tc);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> BellmanFordSP.findShortestPaths(graph, tc.source),
                "Expected exception for negative-weight cycle in: " + tc.description);

        assertEquals("Graph contains a negative-weight cycle.", ex.getMessage(),
                "Exception message must match the expected constant.");
    }

    // =========================================================================
    // Helper
    // =========================================================================

    /**
     * Constructs a {@link Graph} from a {@link TestCase}'s vertex count and
     * edge triples.
     */
    private static Graph buildGraph(TestCase tc) {
        Graph graph = new Graph(tc.vertices);
        for (int[] e : tc.edges) {
            graph.addEdge(e[0], e[1], e[2]);
        }
        return graph;
    }
}
