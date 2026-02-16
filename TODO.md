# TODO: Diagram Completion Plan (Method-Call Conditions)

## Goal
Update the existing endpoint and method diagrams so they explicitly show the **conditions that trigger calls to each internal method**.

## Current State (Already Done)
- Planning and notes exist:
  - `PLAN.md`
  - `NOTES.md`
- Endpoint-level diagrams exist in:
  - `diagram/`
- Method-level drill-down diagrams exist in:
  - `diagram/methods/`
- Call-graph index exists:
  - `diagram/00-call-graph.md`
- Mermaid hardening work already applied:
  - labels are now quoted for square and decision nodes.

## Notes File (Must Use)
- Primary analysis notes file:
  - `NOTES.md`
- Purpose:
  - endpoint inventory,
  - shared helper call traces,
  - diagram design/safety rules.
- Requirement for next pass:
  - update `NOTES.md` first when discovering any new branch/call-condition detail, then update diagrams.

## Known Gap To Fix
Some diagrams show method nodes but do not consistently show the exact **decision condition** immediately before each method call (for example, null checks, branch guards, and loop/partition conditions that cause the call).

## Source of Truth
- Controller to trace from:
  - `web/src/main/java/org/mskcc/cbio/oncokb/api/pub/v1/AnnotationsApiController.java`

## Work Plan
1. Re-scan every endpoint method and list each internal method call with its direct guard condition(s).
2. Re-scan every private helper and list each downstream internal call with guard condition(s).
3. Update endpoint diagrams in `diagram/*.md`:
   - Add explicit decision nodes right before each internal method call.
   - Ensure each call edge is labeled by the guard path (`Yes`/`No` or equivalent).
4. Update method diagrams in `diagram/methods/*.md`:
   - Same rule: every internal call must have visible triggering condition path.
5. Keep stopping expansion at external class calls (cacheFetcher, MainUtils, GenomeNexusUtils, etc.).
6. Re-run Mermaid safety checks from `NOTES.md` (quoted node labels + balanced fences).
7. Final consistency pass:
   - Each endpoint diagram has drill-down links.
   - Method call graph (`diagram/00-call-graph.md`) reflects all endpoint and helper call relationships.

## Acceptance Criteria
- For every internal method call, the diagram shows:
  - the call target,
  - the condition(s) that trigger it,
  - and the opposite branch when relevant.
- Endpoint diagrams and method diagrams are consistent with code paths in `AnnotationsApiController.java`.
- Mermaid blocks remain renderable under GitHub Mermaid rules.

## How To Resume From a Fresh Context Window
1. Open this file first:
   - `TODO.md`
2. Open existing notes and plan:
   - `PLAN.md`
   - `NOTES.md`
3. Open controller source:
   - `web/src/main/java/org/mskcc/cbio/oncokb/api/pub/v1/AnnotationsApiController.java`
4. Review current diagram inventory:
   - `diagram/`
   - `diagram/methods/`
   - `diagram/00-call-graph.md`
5. Apply updates in this order:
   - endpoint diagrams (`diagram/*.md`, excluding `00-call-graph.md`)
   - method diagrams (`diagram/methods/*.md`)
   - call graph index (`diagram/00-call-graph.md`) if relationships changed
6. Before finishing, run a static Mermaid safety sweep:
   - confirm no unquoted `ID[...]` or `ID{...}` in Mermaid blocks,
   - confirm balanced Mermaid fences in all diagram files.
7. Summarize exactly which files changed and which method-call conditions were added.

## Notes for Next Pass
- Keep unrelated worktree changes untouched.
- Focus on accuracy over compactness.
- Do not collapse conditions into vague labels like "validated"; use explicit branch wording from code.
