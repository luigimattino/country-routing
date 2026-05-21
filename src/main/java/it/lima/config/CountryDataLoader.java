package it.lima.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.lima.model.Country;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class CountryDataLoader {

    private static final String COUNTRIES_URL =
            "https://raw.githubusercontent.com/mledoze/countries/master/countries.json";

    // Adjacency map: cca3 -> set of neighbouring cca3 codes
    @Getter
    private Map<String, Set<String>> borderGraph = new HashMap<>();

    @PostConstruct
    public void load() {
        try {
            log.info("Fetching country data from {}", COUNTRIES_URL);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(COUNTRIES_URL))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            List<Country> countries = mapper.readValue(
                    response.body(),
                    new TypeReference<>() {}
            );

            Map<String, Set<String>> graph = new HashMap<>();
            for (Country country : countries) {
                if (country.getCca3() != null) {
                    List<String> borders = country.getBorders();
                    graph.put(
                            country.getCca3(),
                            borders != null
                                    ? Set.copyOf(borders)
                                    : Collections.emptySet()
                    );
                }
            }

            this.borderGraph = Collections.unmodifiableMap(graph);
            log.info("Loaded {} countries into border graph", borderGraph.size());

        } catch (Exception e) {
            throw new IllegalStateException("Failed to load country data from remote URL", e);
        }
    }
}
