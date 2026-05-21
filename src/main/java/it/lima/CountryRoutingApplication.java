package it.lima;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CountryRoutingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CountryRoutingApplication.class, args);
    }
}
