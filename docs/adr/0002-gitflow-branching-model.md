# 0002. Gitflow branching model

- **Status:** Accepted
- **Date:** 2026-11-17

## Context

Work used to land through ad-hoc branches (`chore/*`, `refactor/*`, `temp_*`) and pull
requests opened directly against `main`, and CI only ran on pull requests targeting
`main`. Stacked pull requests therefore merged without any automated build or test.

## Decision

We follow Gitflow:

| Branch | Created from | Merges into | Purpose |
| --- | --- | --- | --- |
| `main` | — | — | Released, always-green code. Only release and hotfix merges. |
| `develop` | `main` | — | Integration branch for the next release. |
| `feature/<name>` | `develop` | `develop` | New work, refactors, docs, chores. |
| `release/<version>` | `develop` | `main` and `develop` | Stabilisation before a release. |
| `hotfix/<name>` | `main` | `main` and `develop` | Urgent production fixes. |

Rules:

- Every change reaches `develop` or `main` through a pull request; no direct pushes.
- A feature may be split into stacked PRs (child PR based on its parent feature branch);
  children merge into the parent before the parent merges into `develop`.
- PRs use merge commits so feature history stays readable. Release and hotfix merges
  into `main` are tagged `vX.Y.Z`.
- CI (`.github/workflows/ci.yml`) runs on every pull request and on pushes to `main`
  and `develop`.
- Commit messages follow Conventional Commits (`feat:`, `fix:`, `refactor:`, `test:`,
  `docs:`, `ci:`, `chore:`).

## Consequences

- `main` only changes through reviewed release or hotfix merges.
- Every PR, including stacked ones, gets a CI run before merge.
- There is one more long-lived branch (`develop`) to keep in sync after hotfixes.
- Branch protection on `main` and `develop` (required CI check, no direct pushes) is the
  recommended enforcement and lives in the repository settings, not in code.
