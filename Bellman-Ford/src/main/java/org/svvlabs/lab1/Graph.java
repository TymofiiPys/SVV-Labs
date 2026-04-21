package org.svvlabs.lab1;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a directed graph using an edge list.
 */
class Graph {
    int vertices;
    List<Edge> edges;

    public Graph(int vertices) {
        this.vertices = vertices;
        this.edges = new ArrayList<>();
    }

    public void addEdge(int source, int destination, int weight) {
        edges.add(new Edge(source, destination, weight));
    }
}
