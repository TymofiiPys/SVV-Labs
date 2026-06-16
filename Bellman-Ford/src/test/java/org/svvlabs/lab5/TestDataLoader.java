package org.svvlabs.lab5;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility that reads JSON test-data files from the classpath and converts them
 * into {@link TestCase} objects ready for use with JUnit 5
 * {@code @MethodSource}.
 */
public class TestDataLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Loads shortest-path test cases from {@code /lab5/shortest_paths_cases.json}.
     *
     * @return list of test cases; each has a graph description, expected distances,
     *         and {@code expectCycle = false}
     */
    public static List<TestCase> loadShortestPathCases() {
        return loadFromJson("/lab5/shortest_paths_cases.json", false);
    }

    /**
     * Loads negative-cycle test cases from {@code /lab5/negative_cycle_cases.json}.
     *
     * @return list of test cases; each has a graph description and
     *         {@code expectCycle = true}
     */
    public static List<TestCase> loadNegativeCycleCases() {
        return loadFromJson("/lab5/negative_cycle_cases.json", true);
    }

    // -----------------------------------------------------------------------

    private static List<TestCase> loadFromJson(String resourcePath, boolean expectCycle) {
        List<TestCase> cases = new ArrayList<>();

        try (InputStream is = TestDataLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalStateException("Test-data resource not found: " + resourcePath);
            }

            JsonNode root = MAPPER.readTree(is);

            for (JsonNode node : root) {
                String description = node.path("description").asString("(no description)");
                int vertices = node.path("vertices").asInt();
                int source = node.path("source").asInt(0);

                // edges: array of [src, dst, weight] triples
                int[][] edges = parseEdges(node.path("edges"));

                // expected distances (only present in shortest-path cases)
                int[] expectedDistances = null;
                if (!expectCycle && node.has("expectedDistances")) {
                    JsonNode dists = node.get("expectedDistances");
                    expectedDistances = new int[dists.size()];
                    for (int i = 0; i < dists.size(); i++) {
                        expectedDistances[i] = dists.get(i).asInt();
                    }
                }

                cases.add(new TestCase(description, vertices, source, edges,
                        expectedDistances, expectCycle));
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load test data from " + resourcePath, e);
        }

        return cases;
    }

    private static int[][] parseEdges(JsonNode edgesNode) {
        int[][] edges = new int[edgesNode.size()][3];
        for (int i = 0; i < edgesNode.size(); i++) {
            JsonNode e = edgesNode.get(i);
            edges[i][0] = e.get(0).asInt(); // source
            edges[i][1] = e.get(1).asInt(); // destination
            edges[i][2] = e.get(2).asInt(); // weight
        }
        return edges;
    }
}
