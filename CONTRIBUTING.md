# Contributing

## Branching (Gitflow)

See [ADR-0002](docs/adr/0002-gitflow-branching-model.md) for the full model.

```
feature/* ──PR──▶ develop ──release PR──▶ main
hotfix/*  ──PR──▶ main ──merge back──▶ develop
```

1. Branch from `develop`: `git switch -c feature/<short-name> origin/develop`.
2. Commit with Conventional Commits (`feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `ci:`, `chore:`).
3. Open a PR into `develop`. Large features may be split into stacked PRs based on the
   parent feature branch; merge the children first.
4. CI must be green before merging.
5. Release: open a PR from `develop` (or `release/<version>`) into `main`, merge it, and
   tag the merge commit `vX.Y.Z`.

## Build and test

```bash
./gradlew testDebugUnitTest assembleDebug
```

Requires JDK 21 and the Android SDK (`local.properties` with `sdk.dir`, never committed).

## Architecture decisions

Structural decisions are recorded in [docs/adr](docs/adr/README.md). Add an ADR in the
same PR as the change it justifies.
