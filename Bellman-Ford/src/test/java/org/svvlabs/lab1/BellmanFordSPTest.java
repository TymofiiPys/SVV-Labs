package org.svvlabs.lab1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BellmanFordSPTest {

    @Test
    public void testPositiveWeights() { 
        Graph graph = new Graph(5);
        graph.addEdge(0, 1, 4);
        graph.addEdge(0, 2, 2);
        graph.addEdge(1, 3, 2);
        graph.addEdge(1, 4, 3);
        graph.addEdge(2, 1, 1);
        graph.addEdge(2, 3, 4);
        graph.addEdge(2, 4, 5);

        int[] expectedDistances = {0, 3, 2, 5, 6};
        int[] actualDistances = BellmanFordSP.findShortestPaths(graph, 0);

        assertArrayEquals(expectedDistances, actualDistances,
                "Distances should match the shortest paths for positive weights.");
    }

    @Test
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

        int[] expectedDistances = {0, -1, 2, -2, 1};
        int[] actualDistances = BellmanFordSP.findShortestPaths(graph, 0);

        assertArrayEquals(expectedDistances, actualDistances,
                "Distances should accurately calculate paths involving negative edges.");
    }

    @Test
    public void testNegativeWeightCycleDetection() {
        Graph graph = new Graph(4);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, -1);
        graph.addEdge(2, 3, -1);
        graph.addEdge(3, 1, -1); // This creates a negative cycle: 1 -> 2 -> 3 -> 1 (Weight: -3)

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            BellmanFordSP.findShortestPaths(graph, 0);
        });

        assertEquals("Graph contains a negative-weight cycle.", exception.getMessage(),
                "Algorithm must detect and throw an exception for negative-weight cycles.");
    }

    @Test
    public void testDisconnectedGraph() {
        Graph graph = new Graph(4);
        graph.addEdge(0, 1, 5);
        // Vertices 2 and 3 are disconnected from 0 and 1
        graph.addEdge(2, 3, 2);

        int[] expectedDistances = {0, 5, Integer.MAX_VALUE, Integer.MAX_VALUE};
        int[] actualDistances = BellmanFordSP.findShortestPaths(graph, 0);

        assertArrayEquals(expectedDistances, actualDistances,
                "Unreachable vertices should remain at Integer.MAX_VALUE.");
    }

    @Test
    public void testSingleNode() {
        Graph graph = new Graph(1);

        int[] expectedDistances = {0};
        int[] actualDistances = BellmanFordSP.findShortestPaths(graph, 0);

        assertArrayEquals(expectedDistances, actualDistances,
                "A single node graph should return a distance of 0 to itself.");
    }
}