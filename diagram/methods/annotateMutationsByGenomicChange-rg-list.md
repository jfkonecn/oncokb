# annotateMutationsByGenomicChange(ReferenceGenome, List<AnnotateMutationByGenomicChangeQuery>)

```mermaid
flowchart LR
  A[annotateMutationsByGenomicChange(RG,List)] --> B[loop queries to build queriesToGN/queryIndexMap]
  B --> C[GenomeNexusUtils.convertGenomicLocation external]
  C --> D{cacheFetcher.genomicLocationShouldBeAnnotated external?}
  D -- Yes --> E{already deduped in map?}
  E -- No --> F[add to map and queriesToGN]
  E -- Yes --> G[skip add]
  D -- No --> G
  F --> H{more queries for GN build?}
  G --> H
  H -- Yes --> B
  H -- No --> I[GenomeNexusUtils.getGenomicLocationVariantsAnnotation external]

  I --> J{annotation count mismatch?}
  J -- Yes --> K[throw ApiException]
  J -- No --> L[allAlterations = AlterationUtils.getAllAlterations external]

  L --> M[loop each query for response]
  M --> N{queryIndexMap contains query.genomicLocation?}
  N -- No --> O[getIndicatorQueryFromGenomicLocation(empty transcript)]
  N -- Yes --> P[AlterationUtils.getAlterationsFromGenomeNexus external]
  P --> Q[selectedAnnotatedAlteration = first or empty]
  Q --> R[getIndicatorQueryForCuratedHgvs]
  R --> S{indicatorQueryResp == null and not germline?}
  S -- Yes --> T[getIndicatorQueryFromGenomicLocation(selected transcript)]
  S -- No --> U[keep current indicator]

  T --> V[set hgvsInfo]
  V --> W[set response query id]
  U --> X{indicatorQueryResp still null?}
  X -- Yes --> O
  X -- No --> W
  O --> W

  W --> Y{more queries?}
  Y -- Yes --> M
  Y -- No --> Z[return result list]
```

Downstream methods:
- `diagram/methods/getIndicatorQueryForCuratedHgvs.md`
- `diagram/methods/getIndicatorQueryFromGenomicLocation.md`
