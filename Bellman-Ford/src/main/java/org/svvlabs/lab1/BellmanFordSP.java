package org.svvlabs.lab1;
import java.util.Arrays;

/**
 * Sequential implementation of the Bellman-Ford algorithm.
 */
public class BellmanFordSP {
    /**
     * Calculates the shortest path from a source vertex to all other vertices.
     *
     * @param graph  The graph to process.
     * @param source The starting vertex.
     * @return An array containing the shortest distances from the source to each vertex.
     * @throws IllegalArgumentException if a negative-weight cycle is detected.
     */
    public static int[] findShortestPaths(Graph graph, int source) {
        int V = graph.vertices;
        int[] distances = new int[V];

        // Step 1: Initialize distances from source to all other vertices as INFINITY
        Arrays.fill(distances, Integer.MAX_VALUE);
        distances[source] = 0;

        // Step 2: Relax all edges |V| - 1 times.
        // A simple shortest path from source to any other vertex can have at most |V| - 1 edges
        for (int i = 1; i < V; i++) {
            for (Edge edge : graph.edges) {
                int u = edge.source;
                int v = edge.destination;
                int weight = edge.weight;

                if (distances[u] != Integer.MAX_VALUE && distances[u] + weight < distances[v]) {
                    distances[v] = distances[u] + weight;
                }
            }
        }

        // Step 3: Check for negative-weight cycles.
        // If we get a shorter path after |V|-1 iterations, then there is a cycle.
        for (Edge edge : graph.edges) {
            int u = edge.source;
            int v = edge.destination;
            int weight = edge.weight;

            if (distances[u] != Integer.MAX_VALUE && distances[u] + weight < distances[v]) {
                throw new IllegalArgumentException("Graph contains a negative-weight cycle.");
            }
        }

        return distances;
    }

    public static void main(String[] args) {

    }
}