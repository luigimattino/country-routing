package it.lima.exception;

public class CountryNotFoundException extends RuntimeException {

    public CountryNotFoundException(String cca3) {
        super("Country not found: " + cca3);
    }
}
