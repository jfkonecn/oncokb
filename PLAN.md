# AnnotationsApiController Diagram Plan

## Scope
- Source file: `web/src/main/java/org/mskcc/cbio/oncokb/api/pub/v1/AnnotationsApiController.java`
- Include all controller endpoints (public and hidden endpoints in this controller).
- One Mermaid flowchart per endpoint.
- Diagram depth: follow all internal function calls and condition branches until reaching external library/framework calls.
- First pass must be notes-first: produce `NOTES.md` before diagram authoring.

## Endpoint Inventory (14 diagrams)
1. `GET /annotate/mutations/byProteinChange`
2. `POST /annotate/mutations/byProteinChange`
3. `GET /annotate/mutations/byGenomicChange`
4. `POST /annotate/mutations/byGenomicChange`
5. `GET /annotate/mutations/byHGVSg`
6. `POST /annotate/mutations/byHGVSg`
7. `GET /annotate/mutations/byHGVSc` (hidden)
8. `POST /annotate/mutations/byHGVSc` (hidden)
9. `GET /annotate/copyNumberAlterations`
10. `POST /annotate/copyNumberAlterations`
11. `GET /annotate/structuralVariants`
12. `POST /annotate/structuralVariants`
13. `GET /annotation/search`
14. `POST /annotate/sample`

## Output Structure
- Notes: `NOTES.md`
- Diagram directory: `diagram/`
- Diagram files (per your naming pattern): `diagram/01-<endpoint-name>.md`, `diagram/02-<endpoint-name>.md`, etc.
- Each diagram file will contain Mermaid `flowchart LR` code and a short legend for branch semantics.

## Milestones

### Milestone 1: First-Pass Static Trace Notes
- Read the controller end-to-end and map each endpoint method.
- For each endpoint, record:
  - Input params and required validations.
  - Branch conditions and error paths.
  - Internal method calls (controller private methods).
  - Shared helper usage and where call chains terminate at external libs.
- Produce `NOTES.md` with endpoint sections plus a shared-method cross-reference.

### Milestone 2: Diagram Design Rules From Notes
- Derive consistent node conventions from `NOTES.md`:
  - Endpoint entry node.
  - Validation decision nodes.
  - Internal call nodes.
  - External call terminal nodes.
  - Response/exception exit nodes.
- Define how loops, maps, and fallback chains are represented.
- Add these conventions at the top of `NOTES.md` (or a short appendix section).

### Milestone 3: Author 14 Endpoint Diagrams
- Create `diagram/`.
- Generate one flowchart per endpoint using the ordered naming scheme.
- Ensure each diagram captures:
  - All endpoint-level conditions.
  - All internal call paths until external boundaries.
  - Null/missing input handling and explicit BAD_REQUEST branches.
  - Success response path.

### Milestone 4: Consistency and Completeness Review
- Cross-check each diagram against `NOTES.md` and controller code.
- Verify that every endpoint has exactly one diagram and no method is skipped.
- Verify that internal call-depth rule is respected uniformly.
- Fix gaps in diagrams and notes.

### Milestone 5: Final Readout
- Provide a concise summary of what was produced:
  - `NOTES.md` coverage.
  - List of created files in `diagram/`.
  - Any ambiguous behaviors discovered in code that may need follow-up.

## Working Rules
- Stop path expansion at external library/framework calls (e.g., Spring, Genome Nexus client, utility/library methods outside controller-level internal flow).
- Preserve exact endpoint behavior, including hidden endpoint handling.
- Favor accuracy over visual compactness; deep paths should remain explicit.
