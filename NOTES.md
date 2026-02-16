# NOTES: AnnotationsApiController First-Pass Trace

Source: `web/src/main/java/org/mskcc/cbio/oncokb/api/pub/v1/AnnotationsApiController.java`

## Trace Conventions
- Internal expansion boundary: continue through methods defined in `AnnotationsApiController`.
- External boundary: stop expanding when call enters other classes/frameworks/libs (for example `cacheFetcher`, `MainUtils`, `GenomeNexusUtils`, `AlterationUtils`, `QueryUtils`, `JsonResultFactory`, Spring `ResponseEntity`, Apache `StringUtils`, etc.).
- Diagram intent: nodes for methods and conditions that trigger method calls.

## Endpoint Inventory
1. GET `/annotate/mutations/byProteinChange`
2. POST `/annotate/mutations/byProteinChange`
3. GET `/annotate/mutations/byGenomicChange`
4. POST `/annotate/mutations/byGenomicChange`
5. GET `/annotate/mutations/byHGVSg`
6. POST `/annotate/mutations/byHGVSg`
7. GET `/annotate/mutations/byHGVSc` (hidden)
8. POST `/annotate/mutations/byHGVSc` (hidden)
9. GET `/annotate/copyNumberAlterations`
10. POST `/annotate/copyNumberAlterations`
11. GET `/annotate/structuralVariants`
12. POST `/annotate/structuralVariants`
13. GET `/annotation/search`
14. POST `/annotate/sample`

## Shared Internal Methods (Controller-local)
- `resolveMatchedRG(referenceGenome)`
  - If `referenceGenome` not empty: `MainUtils.searchEnum(ReferenceGenome.class, referenceGenome)`.
  - If enum resolution fails: throw BAD_REQUEST.
  - Return matched RG (can be `null` if input empty).

- `getIndicatorQueryFromGenomicLocation(...)`
  - `Query query = QueryUtils.getQueryFromAlteration(...)` (external boundary).
  - `cacheFetcher.processQuery(...)` (external boundary).
  - `addTranscriptAndExonToResponse(...)`.
  - Set `query.hgvsInfo` from transcript summary message.

- `getIndicatorQueryFromHGVS(...)`
  - Same structure as previous helper, using HGVS input.

- `annotateSample(sample)`
  - Initializes output lists.
  - If sample-level tumor type exists, applies it into each mutation/CNA/SV query via `setTumorTypeForQueries(...)`.
  - Conditionally dispatches to:
    - `annotateStructuralVariants(...)`
    - `annotateCopyNumberAlterations(...)`
    - `annotateMutationsByGenomicChange(...)`
    - `annotateMutationsByProteinChange(...)`
    - `annotateMutationsByHGVSg(...)` for `hgvsg`
    - `annotateMutationsByHGVSg(...)` for `cDnaChange`
  - Flattens mutation lists with stream and returns sample response.

- `setTumorTypeForQueries(queries, tumorType)`
  - Returns immediately if tumorType empty.
  - Otherwise sets tumorType on each query.

- `annotateStructuralVariants(structuralVariants)`
  - Per query:
    - Resolve `geneA` from `cacheFetcher.findGeneBySymbol(...)` if `query.geneA != null`; swallow `ApiException`; fallback to input gene values when unresolved.
    - Resolve `geneB` similarly.
    - Build `fusionName = FusionUtils.getFusionName(geneA, geneB)` (external).
    - Call `cacheFetcher.processQuery(...)` with SV parameters.
    - Set response query id.
  - Return list.

- `annotateCopyNumberAlterations(copyNumberAlterations)`
  - Per query:
    - Resolve gene by symbol/id if present; swallow `ApiException`; fallback to input gene fields.
    - Call `cacheFetcher.processQuery(...)` using normalized CNA string.
    - Set response query id.
  - Return list.

- `annotateMutationsByGenomicChange(mutations)`
  - Partition input by reference genome into GRCh37/GRCh38 lists and index maps.
  - Defaults null reference genome to GRCh37 on query object.
  - Call `annotateMutationsByGenomicChange(GRCh37, ...)` and `annotateMutationsByGenomicChange(GRCh38, ...)`.
  - Reassemble results in original order.

- `annotateMutationsByGenomicChange(referenceGenome, queries)`
  - Build de-duplicated GN request list (`queriesToGN`) for queries requiring GN (`cacheFetcher.genomicLocationShouldBeAnnotated(...)`).
  - Call `GenomeNexusUtils.getGenomicLocationVariantsAnnotation(...)`.
  - If annotation count mismatch: throw `ApiException`.
  - For each query:
    - If query had GN annotation:
      - Convert annotation to alteration candidates via `AlterationUtils.getAlterationsFromGenomeNexus(...)`.
      - Pick first or empty fallback.
      - Try curated match via `getIndicatorQueryForCuratedHgvs(...)`.
      - If curated match missing and query is not germline:
        - Call `getIndicatorQueryFromGenomicLocation(...)` with selected alteration.
        - Set `hgvsInfo`.
    - If still null:
      - Call `getIndicatorQueryFromGenomicLocation(...)` with empty transcript result.
    - Set response query id.
  - Return list.

- `annotateMutationsByProteinChange(mutations)`
  - Per query call `cacheFetcher.processQuery(...)`.
  - Set response query id.
  - Return list.

- `annotateMutationsByHGVSg(mutations)`
  - Partition by reference genome with default GRCh37.
  - Call genome-specific overload for both references.
  - Reassemble in original order.

- `annotateMutationsByHGVSg(referenceGenome, queries)`
  - Build deduplicated HGVSg list requiring GN (`cacheFetcher.hgvsgShouldBeAnnotated(...)`).
  - Call `GenomeNexusUtils.getHgvsVariantsAnnotation(...)`.
  - Mismatch count => throw `ApiException`.
  - For each query:
    - If query present in GN map:
      - Convert annotation via `AlterationUtils.getAlterationsFromGenomeNexus(...)`.
      - Try curated path via `getIndicatorQueryForCuratedHgvs(...)`.
      - If curated null and not germline:
        - Call `getIndicatorQueryFromHGVS(...)` with selected alteration.
        - Set `hgvsInfo`.
    - If still null:
      - Call `getIndicatorQueryFromHGVS(...)` with empty transcript result.
    - Set response query id.
  - Return list.

- `getIndicatorQueryForCuratedHgvs(query, germlineQuery, hgvsg, selectedAnnotatedAlteration, referenceGenome, allAlterations)`
  - Try direct curated lookup on `hgvsg` via `AlterationUtils.findAlterationWithGeneticType(...)`.
  - If matched and germline flag aligned: `cacheFetcher.processQuery(...)` and return.
  - Else extract `hgvsc` from transcript summary if available.
  - If `hgvsc` has `gene:alteration` format, take right side and lookup curated alteration again.
  - If second curated match found and germline flag aligned: `cacheFetcher.processQuery(...)` and return.
  - Else return null.

- `annotateMutationsByHGVSc(mutations)`
  - Partition by reference genome with default GRCh37.
  - Call genome-specific overload for both references.
  - Reassemble original order.

- `annotateMutationsByHGVSc(referenceGenome, queries)`
  - Initialize result list with placeholders (`null`) for rows requiring GN fallback.
  - Preload `allAlterations` externally.
  - First pass per query:
    - If `cacheFetcher.hgvscShouldBeAnnotated(hgvsc)`:
      - Attempt curated match with `AlterationUtils.findAlterationWithGeneticType(...)`.
      - If germline OR curated non-germline hit:
        - `cacheFetcher.processQuery(...)`, set id, append result.
      - Else:
        - Register query for GN using gene alias if available.
        - Place `null` sentinel in result and map index to query.
    - Else:
      - Call `getIndicatorQueryFromHGVS(...)` with empty transcript result.
      - Set id and append.
  - Fetch GN annotations via `GenomeNexusUtils.getHgvsVariantsAnnotation(...)`; mismatch => throw `ApiException`.
  - Second pass over result indices:
    - For each null slot:
      - Pull variant annotation.
      - Derive transcript alteration via `AlterationUtils.getAlterationsFromGenomeNexus(...)`.
      - Build response via `getIndicatorQueryFromHGVS(...)` using variant HGVSg.
      - Set `hgvsInfo` and id.
      - Replace null slot.
  - Return list.

- `addTranscriptAndExonToResponse(response, summary)`
  - If summary has transcriptId => set canonical transcript.
  - If summary has exon => set exon prefix before `/`.

## Endpoint-Specific First-Pass Notes

### 1) GET /annotate/mutations/byProteinChange
- Method: `annotateMutationsByProteinChangeGet(...)`
- Conditions:
  - If both `entrezGeneId` and `hugoSymbol` provided and not same gene => BAD_REQUEST.
- Call path:
  - `resolveMatchedRG(referenceGenome)`.
  - Build `Query(...)`.
  - `cacheFetcher.processQuery(...)`.
  - `JsonResultFactory.getIndicatorQueryRespWithoutGermline(...)`.
  - Return 200.

### 2) POST /annotate/mutations/byProteinChange
- Method: `annotateMutationsByProteinChangePost(body)`
- Conditions:
  - If `body == null` => BAD_REQUEST.
- Call path:
  - `annotateMutationsByProteinChange(body)`.
  - Inside helper: loop -> `cacheFetcher.processQuery(...)`, set id.
  - `JsonResultFactory.getIndicatorQueryRespWithoutGermline(...)`.
  - Return 200.

### 3) GET /annotate/mutations/byGenomicChange
- Method: `annotateMutationsByGenomicChangeGet(...)`
- Conditions:
  - If genomicLocation empty => BAD_REQUEST.
- Call path:
  - `resolveMatchedRG(referenceGenome)`.
  - Build single `AnnotateMutationByGenomicChangeQuery`, set germline=false, evidence types.
  - `annotateMutationsByGenomicChange(singleton)`.
  - Internal deep path (see shared helper notes): partition by RG -> genome-specific method -> GN fetch + curated HGVS fallback + transcript-based fallback.
  - `JsonResultFactory.getIndicatorQueryRespWithoutGermline(...)`.
  - Return 200.

### 4) POST /annotate/mutations/byGenomicChange
- Method: `annotateMutationsByGenomicChangePost(body)`
- Conditions:
  - If body null => BAD_REQUEST.
- Call path:
  - `annotateMutationsByGenomicChange(body)` with same deep path as endpoint #3 helper chain.
  - `JsonResultFactory.getIndicatorQueryRespWithoutGermline(...)`.
  - Return 200.

### 5) GET /annotate/mutations/byHGVSg
- Method: `annotateMutationsByHGVSgGet(...)`
- Conditions:
  - If hgvsg empty => BAD_REQUEST.
  - Else if invalid hgvsg format via `AlterationUtils.isValidHgvsg` => BAD_REQUEST.
- Call path:
  - `resolveMatchedRG(referenceGenome)`.
  - Build single query (germline=false, evidence types).
  - `annotateMutationsByHGVSg(singleton)`.
  - Deep helper path: partition by RG -> GN HGVS fetch + curated lookup (`getIndicatorQueryForCuratedHgvs`) + fallback `getIndicatorQueryFromHGVS`.
  - `JsonResultFactory.getIndicatorQueryRespWithoutGermline(...)`.
  - Return 200.

### 6) POST /annotate/mutations/byHGVSg
- Method: `annotateMutationsByHGVSgPost(body)`
- Conditions:
  - If body null => BAD_REQUEST.
- Call path:
  - `annotateMutationsByHGVSg(body)` with same helper chain as endpoint #5.
  - `JsonResultFactory.getIndicatorQueryRespWithoutGermline(...)`.
  - Return 200.

### 7) GET /annotate/mutations/byHGVSc (hidden)
- Method: `annotateMutationsByHGVScGet(...)`
- Conditions:
  - If hgvsc empty => BAD_REQUEST.
  - Else if invalid hgvsc via `AlterationUtils.isValidHgvsc` => BAD_REQUEST.
- Call path:
  - `resolveMatchedRG(referenceGenome)`.
  - Build single HGVSc query with germline=false.
  - `annotateMutationsByHGVSc(singleton)`.
  - Deep helper path: partition by RG -> curated-vs-GN branching in HGVSc overload -> optional GN second pass -> `getIndicatorQueryFromHGVS` fallback.
  - Return 200 (no germline-filter wrapper here).

### 8) POST /annotate/mutations/byHGVSc (hidden)
- Method: `annotateMutationsByHGVScPost(body)`
- Conditions:
  - If body null => BAD_REQUEST.
- Call path:
  - `annotateMutationsByHGVSc(body)` with same deep helper chain as endpoint #7.
  - Return 200.

### 9) GET /annotate/copyNumberAlterations
- Method: `annotateCopyNumberAlterationsGet(...)`
- Conditions:
  - If both gene identifiers provided and inconsistent => BAD_REQUEST.
  - Else if `copyNameAlterationType == null` => BAD_REQUEST.
- Call path:
  - `resolveMatchedRG(referenceGenome)`.
  - `cacheFetcher.processQuery(...)` with CNA alteration string.
  - `JsonResultFactory.getIndicatorQueryRespWithoutGermline(...)`.
  - Return 200.

### 10) POST /annotate/copyNumberAlterations
- Method: `annotateCopyNumberAlterationsPost(body)`
- Conditions:
  - If body null => BAD_REQUEST.
- Call path:
  - `annotateCopyNumberAlterations(body)`.
  - Helper loop: optional gene resolution via `cacheFetcher.findGeneBySymbol(...)`, fallback fields, then `cacheFetcher.processQuery(...)`, set id.
  - `JsonResultFactory.getIndicatorQueryRespWithoutGermline(...)`.
  - Return 200.

### 11) GET /annotate/structuralVariants
- Method: `annotateStructuralVariantsGet(...)`
- Conditions:
  - If `structuralVariantType == null` => BAD_REQUEST.
  - Else if `isFunctionalFusion == null` => BAD_REQUEST.
  - If A gene id+symbol mismatch => BAD_REQUEST.
  - Else if B gene id+symbol mismatch => BAD_REQUEST.
- Call path:
  - Resolve `geneA` via `cacheFetcher.findGeneBySymbol(...)` in try/catch, fallback to raw input values when unresolved.
  - Resolve `geneB` similarly.
  - `resolveMatchedRG(referenceGenome)`.
  - `FusionUtils.getFusionName(geneA, geneB)`.
  - `cacheFetcher.processQuery(...)` for structural variant.
  - `JsonResultFactory.getIndicatorQueryRespWithoutGermline(...)`.
  - Return 200.

### 12) POST /annotate/structuralVariants
- Method: `annotateStructuralVariantsPost(body)`
- Conditions:
  - If body null => BAD_REQUEST.
- Call path:
  - `annotateStructuralVariants(body)`.
  - Helper loop: resolve geneA/geneB with try/catch fallbacks -> `FusionUtils.getFusionName(...)` -> `cacheFetcher.processQuery(...)` -> set id.
  - `JsonResultFactory.getIndicatorQueryRespWithoutGermline(...)`.
  - Return 200.

### 13) GET /annotation/search
- Method: `annotationSearchGet(query, limit)`
- Conditions:
  - If `limit == null`, set default 10.
  - If `query != null` and `query.length >= 2`, perform search; otherwise keep empty set.
- Call path:
  - `annotationSearch(query)` (static external import).
  - Convert to `LinkedHashSet` for order retention.
  - `MainUtils.getLimit(...)`.
  - Return 200.

### 14) POST /annotate/sample
- Method: `annotateSamplePost(body)`
- Conditions:
  - If body null => BAD_REQUEST.
- Call path:
  - For each sample in body: `annotateSample(sample)`.
  - `annotateSample` dispatch conditions:
    - If structuralVariants exists -> `setTumorTypeForQueries` -> `annotateStructuralVariants`.
    - If copyNumberAlterations exists -> `setTumorTypeForQueries` -> `annotateCopyNumberAlterations`.
    - If mutations exists:
      - If genomicChange exists -> `setTumorTypeForQueries` -> `annotateMutationsByGenomicChange`.
      - If proteinChange exists -> `setTumorTypeForQueries` -> `annotateMutationsByProteinChange`.
      - If hgvsg exists -> `setTumorTypeForQueries` -> `annotateMutationsByHGVSg`.
      - If cDnaChange exists -> `setTumorTypeForQueries` -> `annotateMutationsByHGVSg`.
    - Merge mutation lists and return sample response.
  - Return 200 with list.

## Diagram Design Rules Derived from Notes
- Use `flowchart LR`.
- Include explicit decision nodes for every `if`/`else if` that changes call path.
- Show helper method nodes as separate steps if they are controller-local.
- For helper methods with loops, include one decision/loop node and explicit branch outcomes (for example curated hit vs GN fallback).
- Mark terminal nodes where external methods are called and not expanded further.
- Show BAD_REQUEST throws as explicit terminal nodes.
- Show success responses as explicit terminal nodes.
