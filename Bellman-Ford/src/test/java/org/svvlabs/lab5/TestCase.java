package org.svvlabs.lab5;

/**
 * Immutable value object representing a single data-driven test case.
 * Used by {@link TestDataLoader} and consumed by
 * {@link BellmanFordDataDrivenTest}.
 */
public class TestCase {

    /** Human-readable description shown in the JUnit report. */
    public final String description;

    /** Number of vertices in the graph. */
    public final int vertices;

    /** Source vertex index for Bellman-Ford. */
    public final int source;

    /**
     * Edge list: each row is {@code [sourceVertex, destinationVertex, weight]}.
     */
    public final int[][] edges;

    /**
     * Expected shortest distances from {@link #source} to every vertex.
     * {@code null} for negative-cycle test cases where we only check the exception.
     */
    public final int[] expectedDistances;

    /** {@code true} if the graph contains a negative-weight cycle. */
    public final boolean expectCycle;

    public TestCase(String description, int vertices, int source,
            int[][] edges, int[] expectedDistances, boolean expectCycle) {
        this.description = description;
        this.vertices = vertices;
        this.source = source;
        this.edges = edges;
        this.expectedDistances = expectedDistances;
        this.expectCycle = expectCycle;
    }

    /** Used by JUnit 5 to label the parameterized test run in the report. */
    @Override
    public String toString() {
        return description + " [V=" + vertices + ", src=" + source
                + ", E=" + edges.length + "]";
    }
}
