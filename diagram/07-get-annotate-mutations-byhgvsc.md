# 07 GET /annotate/mutations/byHGVSc (Hidden)

```mermaid
flowchart LR
  A[HTTP GET /annotate/mutations/byHGVSc] --> B{hgvsc empty?}
  B -- Yes --> C[Throw ApiHttpErrorException BAD_REQUEST]
  B -- No --> D[resolveMatchedRG(referenceGenome)]

  D --> E{referenceGenome empty?}
  E -- No --> F[MainUtils.searchEnum]
  F --> G{matchedRG null?}
  G -- Yes --> H[Throw ApiHttpErrorException BAD_REQUEST invalid referenceGenome]
  G -- No --> I{AlterationUtils.isValidHgvsc(hgvsc)?}
  E -- Yes --> I
  I -- No --> J[Throw ApiHttpErrorException BAD_REQUEST invalid hgvsc]
  I -- Yes --> K[Build single AnnotateMutationByHGVScQuery]

  K --> L[set hgvsc/referenceGenome/tumorType/germline=false]
  L --> M[annotateMutationsByHGVSc(singleton)]

  M --> N[Partition by RG; null defaults to GRCh37]
  N --> O[annotateMutationsByHGVSc(GRCh37, grch37Queries)]
  N --> P[annotateMutationsByHGVSc(GRCh38, grch38Queries)]
  O --> Q[Reassemble original order]
  P --> Q

  Q --> R[HGVSc helper first pass loop]
  R --> S{cacheFetcher.hgvscShouldBeAnnotated(hgvsc)?}
  S -- No --> T[getIndicatorQueryFromHGVS(empty transcript) + set id]
  S -- Yes --> U[AlterationUtils.findAlterationWithGeneticType]
  U --> V{germline OR curated non-germline hit?}
  V -- Yes --> W[cacheFetcher.processQuery + set id]
  V -- No --> X[queue query for GN; store null placeholder]

  T --> Y{More first-pass queries?}
  W --> Y
  X --> Y
  Y -- Yes --> R
  Y -- No --> Z[GenomeNexusUtils.getHgvsVariantsAnnotation(queued)]

  Z --> A1{annotation count mismatch?}
  A1 -- Yes --> A2[Throw ApiException]
  A1 -- No --> A3[Second pass over result slots]

  A3 --> A4{slot is null placeholder?}
  A4 -- No --> A5[Keep existing response]
  A4 -- Yes --> A6[Read GN variant + AlterationUtils.getAlterationsFromGenomeNexus]
  A6 --> A7[getIndicatorQueryFromHGVS(selected transcript, variant hgvsg)]
  A7 --> A8[set hgvsInfo + id; replace slot]

  A5 --> A9{More slots?}
  A8 --> A9
  A9 -- Yes --> A3
  A9 -- No --> B1[return list]

  B1 --> B2[first element used by GET]
  B2 --> B3[Return ResponseEntity 200]

  subgraph FROM_HGVS[getIndicatorQueryFromHGVS]
    H1[QueryUtils.getQueryFromAlteration] --> H2[cacheFetcher.processQuery]
    H2 --> H3[addTranscriptAndExonToResponse]
    H3 --> H4[set query.hgvsInfo]
  end
```

## Drill-Down
- `resolveMatchedRG`: `diagram/methods/resolveMatchedRG.md`
- `annotateMutationsByHGVSc(List)`: `diagram/methods/annotateMutationsByHGVSc-list.md`
