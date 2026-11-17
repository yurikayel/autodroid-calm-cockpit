# 0004. Extract coordinators from the weather ViewModel

- **Status:** Accepted
- **Date:** 2026-11-17

## Context

`WeatherViewModel` (about 740 lines) owned search debouncing, map-tap and GPS
resolution, country cache warming and the detail "hop" buffer, plus the
`WeatherUiState` definition. Thirteen mutable fields and a `TooManyFunctions`
suppression made regressions likely, and the logic could not be tested without the
ViewModel.

## Decision

The ViewModel remains the only owner of `WeatherUiState` (unidirectional data flow).
Logic that does not need to mutate state moves into collaborators that return values or
flows, which the ViewModel folds into state:

- `WeatherSearchCoordinator`: debounced geocoding returning `SearchOutcome`.
- `WeatherMapGpsCoordinator`: map-tap and device-location resolution, theme settling.
- `WeatherCountryWarmer`: budgeted, staggered country prefetch on the ViewModel scope.
- `DetailHopBuffer`: holds the forecast or error that arrives while the camera hops.
- `WeatherUiState` moves to its own file.

Connectivity is exposed to coordinators as a `StateFlow<ConnectivityStatus>` instead of
a mutable field.

## Consequences

- Coordinators have JVM unit tests independent of Android and the ViewModel.
- State mutation stays in one place, so UI behaviour remains predictable.
- The ViewModel is still above the ideal size; further extraction (route forecast,
  top picks) should follow the same pattern.
