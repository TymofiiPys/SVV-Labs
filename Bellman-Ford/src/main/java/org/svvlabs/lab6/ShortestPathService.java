package org.svvlabs.lab6;

import org.svvlabs.lab1.BellmanFordSP;
import org.svvlabs.lab1.Graph;

/**
 * Application service that orchestrates graph storage and shortest-path
 * computation.
 *
 * <p>This class is the <em>system under test</em> for both verification styles:
 * <ul>
 *   <li><b>State verification</b> – callers inspect the {@code int[]} returned
 *       by {@link #computeShortestPaths} to assert correct distances.</li>
 *   <li><b>Behaviour verification</b> – test doubles injected via the
 *       constructor allow assertions on which collaborator methods were called,
 *       in which order, and with which arguments.</li>
 * </ul>
 */
public class ShortestPathService {

    private final GraphRepository repository;
    private final ComputationLogger logger;

    /**
     * @param repository storage collaborator (injected; never {@code null})
     * @param logger     logging collaborator (injected; never {@code null})
     */
    public ShortestPathService(GraphRepository repository, ComputationLogger logger) {
        if (repository == null) throw new IllegalArgumentException("repository must not be null");
        if (logger     == null) throw new IllegalArgumentException("logger must not be null");
        this.repository = repository;
        this.logger     = logger;
    }

    /**
     * Saves {@code graph} under {@code graphId}, runs Bellman-Ford from
     * {@code source}, logs the outcome, and returns the distance array.
     *
     * <p>If the graph contains a negative-weight cycle the exception is logged
     * via {@link ComputationLogger#logError} and then re-thrown.
     *
     * @param graphId unique identifier used to store the graph
     * @param graph   the graph to process
     * @param source  source vertex index
     * @return shortest distances from {@code source} to every vertex
     * @throws IllegalArgumentException if a negative-weight cycle is detected
     */
    public int[] computeShortestPaths(int graphId, Graph graph, int source) {
        repository.save(graphId, graph);

        try {
            int[] distances = BellmanFordSP.findShortestPaths(graph, source);
            logger.logSuccess(source, distances);
            return distances;
        } catch (IllegalArgumentException e) {
            logger.logError(e.getMessage());
            throw e;
        }
    }
}
