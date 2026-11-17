# 0003. Split the weather repository into collaborators

- **Status:** Accepted
- **Date:** 2026-11-17

## Context

`WeatherRepositoryImpl` (about 700 lines) handled geocoding, forecast fetch and caching,
location history and deduplication, nearby/country prefetch, route weather and error
mapping. It read the system clock directly, so cache-TTL behaviour could not be tested
deterministically, and its size exceeded the project's per-file and per-function limits.

## Decision

`WeatherRepositoryImpl` stays the single implementation of `WeatherRepository` but
delegates to collaborators in the same package, each with one responsibility:

- `WeatherGeocodingCollaborator`: search and reverse geocoding.
- `WeatherForecastCollaborator`: remote forecast, marine data, Room persistence, place images.
- `WeatherHistoryCollaborator`: canonical location resolution, viewed history, eviction.
- `WeatherPrefetchCollaborator`: nearby and country warm-up.
- `RouteWeatherCollaborator`: origin/destination weather for a route.
- `WeatherRepositoryErrorExt.kt`: exception to `AppError` mapping.

Time comes from an injected `java.time.Clock` (provided in `WeatherAppModule`).
Freshness rules live in `CachePolicy` as pure functions over primitive values.

## Consequences

- Each collaborator can be unit-tested with a mocked DAO/API and a fixed clock.
- The public `WeatherRepository` contract is unchanged for consumers.
- Collaborators share `WeatherDao`; transactional boundaries stay inside the DAO.
- More classes to navigate; keeping them in one package keeps them discoverable.
