package org.svvlabs.lab6;

/**
 * Observer interface for recording Bellman-Ford computation events.
 *
 * <p>This interface is the secondary mock-point for <b>behaviour verification</b>:
 * tests assert that exactly the right log method is called after a successful
 * run or after a negative-cycle error.
 */
public interface ComputationLogger {

    /**
     * Called after a successful shortest-path computation.
     *
     * @param source    the source vertex that was used
     * @param distances the resulting distance array
     */
    void logSuccess(int source, int[] distances);

    /**
     * Called when the algorithm aborts due to a negative-weight cycle.
     *
     * @param errorMessage the message from the thrown exception
     */
    void logError(String errorMessage);
}
