package org.svvlabs.lab2;

import java.util.List;

/**
 * Data Transfer Object representing a graph as a JSON payload.
 */
public class GraphRequest {
    private int vertices;
    private List<EdgeDto> edges;

    public GraphRequest() {}

    public int getVertices() {
        return vertices;
    }

    public void setVertices(int vertices) {
        this.vertices = vertices;
    }

    public List<EdgeDto> getEdges() {
        return edges;
    }

    public void setEdges(List<EdgeDto> edges) {
        this.edges = edges;
    }
}
