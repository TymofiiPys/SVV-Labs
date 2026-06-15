package org.svvlabs.lab2;

import org.svvlabs.lab1.BellmanFordSP;
import org.svvlabs.lab1.Graph;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * REST controller exposing graph creation and Bellman-Ford shortest-path endpoints.
 *
 * <p>All endpoints are prefixed with {@code /api/graph}.
 */
@RestController
@RequestMapping("/api/graph")
public class GraphController {

    // -----------------------------------------------------------------------
    // Endpoint 1 – Create random graph by number of vertices (query param)
    // GET /api/graph/random?vertices=5
    // -----------------------------------------------------------------------

    /**
     * Creates a random directed graph with the specified number of vertices.
     * Edges are generated randomly (up to vertices*(vertices-1) directed edges,
     * random weight in [-10, 20]).
     *
     * @param vertices number of vertices (must be &gt; 0)
     * @return 200 with a {@link GraphRequest} JSON body on success,
     *         400 if {@code vertices} is negative or zero
     */
    @GetMapping("/random")
    public ResponseEntity<?> createRandomGraph(@RequestParam int vertices) {
        if (vertices <= 0) {
            return ResponseEntity
                    .badRequest()
                    .body("Number of vertices must be a positive integer, got: " + vertices);
        }

        Random random = new Random();
        GraphRequest graphRequest = new GraphRequest();
        graphRequest.setVertices(vertices);

        List<EdgeDto> edges = new ArrayList<>();
        for (int u = 0; u < vertices; u++) {
            for (int v = 0; v < vertices; v++) {
                if (u != v && random.nextBoolean()) {
                    int weight = random.nextInt(31) - 10; // [-10, 20]
                    edges.add(new EdgeDto(u, v, weight));
                }
            }
        }
        graphRequest.setEdges(edges);

        return ResponseEntity.ok(graphRequest);
    }

    // -----------------------------------------------------------------------
    // Endpoint 2 – Create graph from JSON payload
    // GET /api/graph/create  (body: GraphRequest JSON)
    // -----------------------------------------------------------------------

    /**
     * Accepts a graph as a JSON payload and echoes it back, confirming creation.
     *
     * @param graphRequest the graph definition
     * @return 200 with the received graph on success,
     *         400 if payload is missing or vertices &lt;= 0
     */
    @GetMapping("/create")
    public ResponseEntity<?> createGraphFromPayload(@RequestBody GraphRequest graphRequest) {
        if (graphRequest == null) {
            return ResponseEntity.badRequest().body("Graph payload must not be null.");
        }
        if (graphRequest.getVertices() <= 0) {
            return ResponseEntity
                    .badRequest()
                    .body("Number of vertices must be a positive integer, got: "
                            + graphRequest.getVertices());
        }
        if (graphRequest.getEdges() == null) {
            return ResponseEntity.badRequest().body("Edges list must not be null.");
        }

        // Validate that all edge vertex indices are within bounds
        for (EdgeDto edge : graphRequest.getEdges()) {
            if (edge.getSource() < 0 || edge.getSource() >= graphRequest.getVertices()
                    || edge.getDestination() < 0
                    || edge.getDestination() >= graphRequest.getVertices()) {
                return ResponseEntity.badRequest()
                        .body("Edge vertex index out of bounds: source=" + edge.getSource()
                                + ", destination=" + edge.getDestination()
                                + ", vertices=" + graphRequest.getVertices());
            }
        }

        return ResponseEntity.ok(graphRequest);
    }

    // -----------------------------------------------------------------------
    // Endpoint 3 – Run Bellman-Ford shortest paths
    // GET /api/graph/shortest-paths  (body: BellmanFordRequest JSON)
    // -----------------------------------------------------------------------

    /**
     * Runs the Bellman-Ford algorithm on the provided graph and returns the
     * shortest distances from the source vertex to every other vertex.
     *
     * @param request contains the graph definition and the source vertex index
     * @return 200 with an array of distances on success,
     *         400 if input is invalid or a negative-weight cycle is detected
     */
    @GetMapping("/shortest-paths")
    public ResponseEntity<?> findShortestPaths(@RequestBody BellmanFordRequest request) {
        if (request == null || request.getGraph() == null) {
            return ResponseEntity.badRequest().body("Request body with 'graph' and 'source' must not be null.");
        }

        GraphRequest graphRequest = request.getGraph();
        int vertices = graphRequest.getVertices();
        int source = request.getSource();

        if (vertices <= 0) {
            return ResponseEntity.badRequest()
                    .body("Number of vertices must be a positive integer, got: " + vertices);
        }
        if (source < 0 || source >= vertices) {
            return ResponseEntity.badRequest()
                    .body("Source vertex index out of bounds: source=" + source
                            + ", vertices=" + vertices);
        }
        if (graphRequest.getEdges() == null) {
            return ResponseEntity.badRequest().body("Edges list must not be null.");
        }

        // Build lab1 Graph from DTO
        Graph graph = new Graph(vertices);
        for (EdgeDto edge : graphRequest.getEdges()) {
            if (edge.getSource() < 0 || edge.getSource() >= vertices
                    || edge.getDestination() < 0 || edge.getDestination() >= vertices) {
                return ResponseEntity.badRequest()
                        .body("Edge vertex index out of bounds: source=" + edge.getSource()
                                + ", destination=" + edge.getDestination());
            }
            graph.addEdge(edge.getSource(), edge.getDestination(), edge.getWeight());
        }

        try {
            int[] distances = BellmanFordSP.findShortestPaths(graph, source);
            // Convert Integer.MAX_VALUE (unreachable) to null for clearer JSON output
            List<Object> result = new ArrayList<>();
            for (int d : distances) {
                result.add(d == Integer.MAX_VALUE ? null : d);
            }
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
