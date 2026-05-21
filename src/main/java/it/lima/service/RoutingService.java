package it.lima.service;

import it.lima.config.CountryDataLoader;
import it.lima.exception.CountryNotFoundException;
import it.lima.exception.NoRouteFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutingService {

    private final CountryDataLoader countryDataLoader;

    /**
     * Finds the shortest land route between two countries using BFS.
     * Results are cached to avoid repeated computation for the same pair.
     *
     * @param origin      cca3 code of the starting country
     * @param destination cca3 code of the target country
     * @return ordered list of cca3 codes representing the route (inclusive of origin and destination)
     */
    @Cacheable("routes")
    public List<String> findRoute(String origin, String destination) {
        Map<String, Set<String>> graph = countryDataLoader.getBorderGraph();

        validateCountry(origin, graph);
        validateCountry(destination, graph);

        if (origin.equals(destination)) {
            return List.of(origin);
        }

        log.debug("Finding route from {} to {}", origin, destination);

        // BFS: each queue entry is the current country code.
        // We track visited nodes and their parent to reconstruct the path.
        Deque<String> queue = new ArrayDeque<>();
        Map<String, String> parent = new HashMap<>();

        queue.add(origin);
        parent.put(origin, null);

        while (!queue.isEmpty()) {
            String current = queue.poll();

            for (String neighbour : graph.getOrDefault(current, Collections.emptySet())) {
                if (!parent.containsKey(neighbour)) {
                    parent.put(neighbour, current);

                    if (neighbour.equals(destination)) {
                        return reconstructPath(parent, origin, destination);
                    }

                    queue.add(neighbour);
                }
            }
        }

        throw new NoRouteFoundException(origin, destination);
    }

    private void validateCountry(String cca3, Map<String, Set<String>> graph) {
        if (!graph.containsKey(cca3)) {
            throw new CountryNotFoundException(cca3);
        }
    }

    private List<String> reconstructPath(Map<String, String> parent, String origin, String destination) {
        List<String> path = new ArrayList<>();
        String current = destination;

        while (current != null) {
            path.add(current);
            current = parent.get(current);
        }

        Collections.reverse(path);
        log.debug("Route found: {}", path);
        return path;
    }
}
