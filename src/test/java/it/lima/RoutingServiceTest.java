package it.lima;

import it.lima.config.CountryDataLoader;
import it.lima.exception.CountryNotFoundException;
import it.lima.exception.NoRouteFoundException;
import it.lima.service.RoutingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoutingServiceTest {

    @Mock
    private CountryDataLoader countryDataLoader;

    @InjectMocks
    private RoutingService routingService;

    /**
     * For testing purposes, we use a simple graph with 5 countries of depth.
     * Simple graph:
     *  CZE -- AUT -- ITA
     *  GBR  (island, no borders)
     *  AUS  (island, no borders)
     */
    @BeforeEach
    void setUp() {
        Map<String, Set<String>> graph = Map.of(
                "CZE", Set.of("AUT", "DEU", "POL", "SVK"),
                "AUT", Set.of("CZE", "DEU", "HUN", "ITA", "LIE", "SVK", "SVN", "CHE"),
                "ITA", Set.of("AUT", "FRA", "SMR", "SVN", "VAT", "CHE"),
                "DEU", Set.of("AUT", "BEL", "CZE", "DNK", "FRA", "LUX", "NLD", "POL", "CHE"),
                "GBR", Set.of(),
                "AUS", Set.of()
        );
        when(countryDataLoader.getBorderGraph()).thenReturn(graph);
    }

    @Test
    void shouldFindDirectRoute() {
        List<String> route = routingService.findRoute("CZE", "AUT");
        assertThat(route).containsExactly("CZE", "AUT");
    }

    @Test
    void shouldFindShortestRouteWithOneHop() {
        List<String> route = routingService.findRoute("CZE", "ITA");
        // CZE -> AUT -> ITA  (2 hops, shortest path)
        assertThat(route).containsExactly("CZE", "AUT", "ITA");
    }

    @Test
    void shouldReturnSingleCountryWhenOriginEqualsDestination() {
        List<String> route = routingService.findRoute("CZE", "CZE");
        assertThat(route).containsExactly("CZE");
    }

    @Test
    void shouldThrowNoRouteFoundExceptionForIsland() {
        assertThatThrownBy(() -> routingService.findRoute("CZE", "GBR"))
                .isInstanceOf(NoRouteFoundException.class)
                .hasMessageContaining("CZE")
                .hasMessageContaining("GBR");
    }

    @Test
    void shouldThrowNoRouteFoundBetweenTwoIslands() {
        assertThatThrownBy(() -> routingService.findRoute("GBR", "AUS"))
                .isInstanceOf(NoRouteFoundException.class);
    }

    @Test
    void shouldThrowCountryNotFoundForUnknownCode() {
        assertThatThrownBy(() -> routingService.findRoute("XXX", "CZE"))
                .isInstanceOf(CountryNotFoundException.class)
                .hasMessageContaining("XXX");
    }
}
