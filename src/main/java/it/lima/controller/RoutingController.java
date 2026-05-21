package it.lima.controller;

import it.lima.model.RouteResponse;
import it.lima.service.RoutingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/routing")
@RequiredArgsConstructor
public class RoutingController {

    private final RoutingService routingService;

    /**
     * Returns the shortest land route from origin to destination country.
     *
     * @param origin      cca3 country code of the starting point (e.g. CZE)
     * @param destination cca3 country code of the end point (e.g. ITA)
     * @return JSON with a "route" array of cca3 codes; HTTP 400 if no land route exists
     */
    @GetMapping("/{origin}/{destination}")
    public ResponseEntity<RouteResponse> getRoute(
            @PathVariable String origin,
            @PathVariable String destination) {

        return ResponseEntity.ok(
                new RouteResponse(routingService.findRoute(
                        origin.toUpperCase(),
                        destination.toUpperCase()
                ))
        );
    }
}
