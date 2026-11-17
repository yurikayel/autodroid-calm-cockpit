# Architecture Decision Records

Short records of decisions that shape this codebase, in the format described in
[ADR-0001](0001-record-architecture-decisions.md).

| ADR | Title | Status |
| --- | --- | --- |
| [0001](0001-record-architecture-decisions.md) | Record architecture decisions | Accepted |
| [0002](0002-gitflow-branching-model.md) | Gitflow branching model | Accepted |
| [0003](0003-weather-repository-collaborators.md) | Split the weather repository into collaborators | Accepted |
| [0004](0004-weather-viewmodel-coordinators.md) | Extract coordinators from the weather ViewModel | Accepted |

## Adding a record

1. Copy [template.md](template.md) to `NNNN-short-title.md` using the next free number.
2. Fill in context, decision and consequences. Keep it to one decision.
3. Add it to the table above and open the PR with the ADR alongside the code it describes.
4. Never rewrite an accepted ADR. To change course, add a new ADR and mark the old one
   `Superseded by ADR-NNNN`.
