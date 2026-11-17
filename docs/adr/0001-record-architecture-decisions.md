# 0001. Record architecture decisions

- **Status:** Accepted
- **Date:** 2026-11-17

## Context

Several people and AI agents work on this repository. Structural choices (module
layering, how large classes are split, branching rules) were previously recorded only in
commit messages and a local coordination file, which made them hard to find and easy to
undo by accident.

## Decision

We keep Architecture Decision Records in `docs/adr/`, one Markdown file per decision,
numbered sequentially and using the lightweight format in [template.md](template.md).
An ADR is added in the same pull request as the change it justifies.

## Consequences

- Reviewers can check a structural change against its stated rationale.
- Accepted ADRs are immutable; reversing a decision needs a new ADR that supersedes it.
- Small, local choices do not need an ADR. Use one when a decision constrains future work.
