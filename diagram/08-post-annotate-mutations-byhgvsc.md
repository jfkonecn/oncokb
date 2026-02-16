# 08 POST /annotate/mutations/byHGVSc (Hidden)

```mermaid
flowchart LR
  A[HTTP POST /annotate/mutations/byHGVSc] --> B{body == null?}
  B -- Yes --> C[Throw ApiHttpErrorException BAD_REQUEST]
  B -- No --> D[annotateMutationsByHGVSc(body)]

  D --> E[Partition by referenceGenome; null defaults to GRCh37]
  E --> F[annotateMutationsByHGVSc(GRCh37, grch37Queries)]
  E --> G[annotateMutationsByHGVSc(GRCh38, grch38Queries)]
  F --> H[Reassemble original order]
  G --> H

  H --> I[Per RG helper first pass over queries]
  I --> J{hgvscShouldBeAnnotated?}
  J -- No --> K[getIndicatorQueryFromHGVS(empty transcript); set id]
  J -- Yes --> L[find curated alteration]
  L --> M{germline OR curated non-germline hit?}
  M -- Yes --> N[cacheFetcher.processQuery; set id]
  M -- No --> O[enqueue for GN and append null placeholder]

  K --> P{More first-pass queries?}
  N --> P
  O --> P
  P -- Yes --> I
  P -- No --> Q[GenomeNexusUtils.getHgvsVariantsAnnotation]

  Q --> R{count mismatch?}
  R -- Yes --> S[Throw ApiException]
  R -- No --> T[Second pass on result indices]

  T --> U{Current slot null?}
  U -- No --> V[Keep response]
  U -- Yes --> W[lookup GN annotation + getAlterationsFromGenomeNexus]
  W --> X[getIndicatorQueryFromHGVS(selected transcript, variant hgvsg)]
  X --> Y[set hgvsInfo + id; replace null]

  V --> Z{More indices?}
  Y --> Z
  Z -- Yes --> T
  Z -- No --> A1[Return list]

  A1 --> A2[Return ResponseEntity 200]

  subgraph FROM_HGVS[getIndicatorQueryFromHGVS]
    H1[QueryUtils.getQueryFromAlteration] --> H2[cacheFetcher.processQuery]
    H2 --> H3[addTranscriptAndExonToResponse]
    H3 --> H4[set query.hgvsInfo]
  end
```

## Drill-Down
- `annotateMutationsByHGVSc(List)`: `diagram/methods/annotateMutationsByHGVSc-list.md`
