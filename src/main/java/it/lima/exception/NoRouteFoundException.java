package it.lima.exception;

public class NoRouteFoundException extends RuntimeException {

    public NoRouteFoundException(String origin, String destination) {
        super("No land route found from " + origin + " to " + destination);
    }
}
