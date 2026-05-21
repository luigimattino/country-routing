![Build](https://github.com/luigimattino/country-routing/actions/workflows/build.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen)
![Build](https://img.shields.io/badge/build-maven-blue)

# Country Land Route Calculator

A Spring Boot REST service that calculates the shortest land route between any two countries using BFS on the world border graph.

## Requirements

- Java 17+
- Maven 3.8+
- Internet access on startup (country data is fetched from GitHub)

## Build & Run

### Option 1 – Maven wrapper (recommended)

```bash
# Clone / enter the project
cd country-routing

# Build & run tests
./mvnw clean verify

# Start the application
./mvnw spring-boot:run
```

### Option 2 – System Maven

```bash
mvn clean verify
mvn spring-boot:run
```

### Option 3 – Executable JAR

```bash
mvn clean package
java -jar target/country-routing-1.0.0.jar
```

The server starts on **http://localhost:8080**.

---

## IntelliJ IDEA Setup

1. **File → Open** – select the `country-routing` folder (IntelliJ detects the `pom.xml` automatically).
2. Wait for Maven to download dependencies (bottom progress bar).
3. Ensure the **Project SDK** is set to Java 17+: *File → Project Structure → Project*.
4. Run `CountryRoutingApplication` via the green ▶ button, or use **Maven** panel → `spring-boot:run`.

---

## API Usage

### Endpoint

```
GET /routing/{origin}/{destination}
```

Path variables use **cca3** country codes (ISO 3166-1 alpha-3).

### Examples

**Land route exists:**
```bash
curl http://localhost:8080/routing/CZE/ITA
```
```json
{ "route": ["CZE", "AUT", "ITA"] }
```

**No land route (island):**
```bash
curl -i http://localhost:8080/routing/CZE/AUS
```
```
HTTP/1.1 400 Bad Request
{ "error": "No land route found from CZE to AUS" }
```

**Unknown country code:**
```bash
curl -i http://localhost:8080/routing/XXX/ITA
```
```
HTTP/1.1 400 Bad Request
{ "error": "Country not found: XXX" }
```

---

## How It Works

1. **Startup** – `CountryDataLoader` fetches `countries.json` from GitHub and builds an in-memory adjacency map (`cca3 → Set<neighbour cca3>`).
2. **Request** – `RoutingController` accepts the cca3 pair and delegates to `RoutingService`.
3. **BFS** – The service runs Breadth-First Search on the border graph, which guarantees the **shortest** path (fewest border crossings).
4. **Caching** – Computed routes are stored in a `ConcurrentMapCache` so repeated queries are O(1).
5. **Error handling** – `GlobalExceptionHandler` maps domain exceptions to HTTP 400.

---

## Project Structure

```
src/
├── main/java/it/lima/
│   ├── CountryRoutingApplication.java   # Entry point
│   ├── config/
│   │   ├── CountryDataLoader.java       # Fetches & builds border graph on startup
│   │   └── CacheConfig.java             # In-memory route cache
│   ├── controller/
│   │   └── RoutingController.java       # GET /routing/{origin}/{destination}
│   ├── exception/
│   │   ├── CountryNotFoundException.java
│   │   ├── NoRouteFoundException.java
│   │   └── GlobalExceptionHandler.java  # Maps exceptions → HTTP 400
│   ├── model/
│   │   ├── Country.java                 # JSON deserialization model
│   │   └── RouteResponse.java           # API response DTO
│   └── service/
│       └── RoutingService.java          # BFS routing algorithm
├── test/java/it/lima/
│    └── RoutingServiceTest.java          # Unit tests (Mockito)
└── RoutingControllerRequests.http        # API request for rapid testings
```
