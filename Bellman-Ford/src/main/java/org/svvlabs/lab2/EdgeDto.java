package org.svvlabs.lab2;

/**
 * Data Transfer Object representing a directed edge in the graph.
 */
public class EdgeDto {
    private int source;
    private int destination;
    private int weight;

    public EdgeDto() {}

    public EdgeDto(int source, int destination, int weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
