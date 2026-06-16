package org.svvlabs.lab6;

import org.svvlabs.lab1.Graph;

/**
 * Repository abstraction for storing and retrieving {@link Graph} instances.
 *
 * <p>This interface is the primary seam used for <b>behaviour verification</b>:
 * tests can inject a mock and assert that {@link #save} and {@link #findById}
 * are called at the right moments with the right arguments.
 */
public interface GraphRepository {

    /**
     * Persists the graph under the given identifier.
     *
     * @param id    unique identifier for the graph
     * @param graph the graph to store
     */
    void save(int id, Graph graph);

    /**
     * Returns the graph stored under {@code id}, or {@code null} if none exists.
     *
     * @param id the identifier to look up
     * @return the stored graph, or {@code null}
     */
    Graph findById(int id);
}
