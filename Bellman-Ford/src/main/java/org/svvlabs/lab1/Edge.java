package org.svvlabs.lab1;

/**
 * Represents a directed edge in the graph.
 */
public class Edge {
    public int source;
    public int destination;
    public int weight;

    public Edge(int source, int destination, int weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }
}
