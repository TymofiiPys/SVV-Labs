package org.svvlabs.lab6;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svvlabs.lab1.Graph;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * <b>Behaviour verification</b> tests for {@link ShortestPathService}.
 *
 * <p>Behaviour verification means we assert <em>how</em> the system under test
 * interacts with its collaborators — which methods were called, how many times,
 * in which order, and with which arguments — rather than what value is returned.
 *
 * <p>The two collaborators ({@link GraphRepository} and {@link ComputationLogger})
 * are replaced by Mockito mocks injected via {@link MockitoExtension}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Behaviour verification")
public class BehaviourVerificationTest {

    @Mock
    GraphRepository repository;

    @Mock
    ComputationLogger logger;

    ShortestPathService service;

    @BeforeEach
    void setUp() {
        service = new ShortestPathService(repository, logger);
    }

    // =========================================================================
    // GraphRepository interaction
    // =========================================================================

    @Nested
    @DisplayName("GraphRepository interactions")
    class RepositoryInteractions {

        @Test
        @DisplayName("save is called exactly once per computation")
        void saveCalledOnce() {
            Graph g = simpleGraph();
            service.computeShortestPaths(42, g, 0);

            verify(repository, times(1)).save(42, g);
        }

        @Test
        @DisplayName("save receives the exact graph ID passed to the service")
        void saveReceivesCorrectGraphId() {
            Graph g = simpleGraph();
            service.computeShortestPaths(7, g, 0);

            ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);
            verify(repository).save(idCaptor.capture(), any(Graph.class));
            assertEquals(7, idCaptor.getValue(), "save must be called with graphId=7");
        }

        @Test
        @DisplayName("save receives the exact same graph object passed to the service")
        void saveReceivesExactGraphObject() {
            Graph g = simpleGraph();
            service.computeShortestPaths(1, g, 0);

            ArgumentCaptor<Graph> graphCaptor = ArgumentCaptor.forClass(Graph.class);
            verify(repository).save(anyInt(), graphCaptor.capture());
            assertSame(g, graphCaptor.getValue(),
                    "the same graph reference must be forwarded to the repository");
        }

        @Test
        @DisplayName("findById is never called during computeShortestPaths")
        void findByIdNeverCalled() {
            service.computeShortestPaths(1, simpleGraph(), 0);
            verify(repository, never()).findById(anyInt());
        }

        @Test
        @DisplayName("save is still called even when the graph has a negative cycle")
        void saveCalledBeforeCycleDetected() {
            Graph cyclic = negativeCycleGraph();
            assertThrows(IllegalArgumentException.class,
                    () -> service.computeShortestPaths(99, cyclic, 0));

            // save must have been called before the exception propagated
            verify(repository, times(1)).save(99, cyclic);
        }
    }

    // =========================================================================
    // ComputationLogger interaction – happy path
    // =========================================================================

    @Nested
    @DisplayName("ComputationLogger interactions – success path")
    class LoggerSuccessInteractions {

        @Test
        @DisplayName("logSuccess is called exactly once after a valid computation")
        void logSuccessCalledOnce() {
            service.computeShortestPaths(1, simpleGraph(), 0);
            verify(logger, times(1)).logSuccess(anyInt(), any(int[].class));
        }

        @Test
        @DisplayName("logSuccess receives the correct source vertex")
        void logSuccessReceivesCorrectSource() {
            service.computeShortestPaths(1, simpleGraph(), 0);

            ArgumentCaptor<Integer> srcCaptor = ArgumentCaptor.forClass(Integer.class);
            verify(logger).logSuccess(srcCaptor.capture(), any(int[].class));
            assertEquals(0, srcCaptor.getValue(),
                    "logSuccess must be called with source=0");
        }

        @Test
        @DisplayName("logSuccess receives the correct distance array")
        void logSuccessReceivesCorrectDistances() {
            // Graph: 0→1(1), 0→2(4), 1→2(2) → from 0: [0, 1, 3]
            Graph g = new Graph(3);
            g.addEdge(0, 1, 1);
            g.addEdge(0, 2, 4);
            g.addEdge(1, 2, 2);

            service.computeShortestPaths(1, g, 0);

            ArgumentCaptor<int[]> distCaptor = ArgumentCaptor.forClass(int[].class);
            verify(logger).logSuccess(anyInt(), distCaptor.capture());
            assertArrayEquals(new int[]{0, 1, 3}, distCaptor.getValue(),
                    "logSuccess must receive the computed distance array");
        }

        @Test
        @DisplayName("logError is never called on a successful computation")
        void logErrorNeverCalledOnSuccess() {
            service.computeShortestPaths(1, simpleGraph(), 0);
            verify(logger, never()).logError(anyString());
        }
    }

    // =========================================================================
    // ComputationLogger interaction – negative-cycle error path
    // =========================================================================

    @Nested
    @DisplayName("ComputationLogger interactions – negative-cycle error path")
    class LoggerErrorInteractions {

        @Test
        @DisplayName("logError is called exactly once when a negative cycle is detected")
        void logErrorCalledOnce() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.computeShortestPaths(1, negativeCycleGraph(), 0));

            verify(logger, times(1)).logError(anyString());
        }

        @Test
        @DisplayName("logError receives the exception message verbatim")
        void logErrorReceivesCorrectMessage() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.computeShortestPaths(1, negativeCycleGraph(), 0));

            ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);
            verify(logger).logError(msgCaptor.capture());
            assertEquals("Graph contains a negative-weight cycle.", msgCaptor.getValue());
        }

        @Test
        @DisplayName("logSuccess is never called when a negative cycle is detected")
        void logSuccessNeverCalledOnCycle() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.computeShortestPaths(1, negativeCycleGraph(), 0));

            verify(logger, never()).logSuccess(anyInt(), any(int[].class));
        }
    }

    // =========================================================================
    // Interaction ordering
    // =========================================================================

    @Nested
    @DisplayName("Collaboration order")
    class CollaborationOrder {

        @Test
        @DisplayName("repository.save is called BEFORE logger.logSuccess")
        void saveBeforeLogSuccessOnHappyPath() {
            service.computeShortestPaths(1, simpleGraph(), 0);

            InOrder order = inOrder(repository, logger);
            order.verify(repository).save(anyInt(), any(Graph.class));
            order.verify(logger).logSuccess(anyInt(), any(int[].class));
        }

        @Test
        @DisplayName("repository.save is called BEFORE logger.logError on cycle")
        void saveBeforeLogErrorOnCycle() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.computeShortestPaths(1, negativeCycleGraph(), 0));

            InOrder order = inOrder(repository, logger);
            order.verify(repository).save(anyInt(), any(Graph.class));
            order.verify(logger).logError(anyString());
        }

        @Test
        @DisplayName("no unexpected interactions occur on the happy path")
        void noUnexpectedInteractionsOnHappyPath() {
            service.computeShortestPaths(1, simpleGraph(), 0);

            verify(repository).save(anyInt(), any(Graph.class));
            verify(logger).logSuccess(anyInt(), any(int[].class));
            verifyNoMoreInteractions(repository, logger);
        }

        @Test
        @DisplayName("no unexpected interactions occur on the error path")
        void noUnexpectedInteractionsOnErrorPath() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.computeShortestPaths(1, negativeCycleGraph(), 0));

            verify(repository).save(anyInt(), any(Graph.class));
            verify(logger).logError(anyString());
            verifyNoMoreInteractions(repository, logger);
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Simple 3-vertex graph with no cycles: 0→1(1), 0→2(4), 1→2(2). */
    private static Graph simpleGraph() {
        Graph g = new Graph(3);
        g.addEdge(0, 1, 1);
        g.addEdge(0, 2, 4);
        g.addEdge(1, 2, 2);
        return g;
    }

    /**
     * Graph containing a negative-weight cycle:
     * 0→1(1), 1→2(-5), 2→0(2) — total cycle weight = -2.
     */
    private static Graph negativeCycleGraph() {
        Graph g = new Graph(3);
        g.addEdge(0, 1,  1);
        g.addEdge(1, 2, -5);
        g.addEdge(2, 0,  2);
        return g;
    }
}
