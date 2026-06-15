package org.svvlabs.lab2;

/**
 * Data Transfer Object for the Bellman-Ford shortest path request.
 * Combines a graph payload with the source vertex index.
 */
public class BellmanFordRequest {
    private GraphRequest graph;
    private int source;

    public BellmanFordRequest() {}

    public GraphRequest getGraph() {
        return graph;
    }

    public void setGraph(GraphRequest graph) {
        this.graph = graph;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }
}
