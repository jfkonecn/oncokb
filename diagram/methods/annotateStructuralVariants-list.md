# annotateStructuralVariants(List<AnnotateStructuralVariantQuery>)

```mermaid
flowchart LR
  A[annotateStructuralVariants(List)] --> B[loop queries]

  B --> C{query.geneA != null?}
  C -- Yes --> D[cacheFetcher.findGeneBySymbol external try/catch]
  C -- No --> E[geneA default empty]
  D --> F{resolved geneA empty?}
  F -- Yes --> G[fallback from query.geneA]
  F -- No --> H[use resolved geneA]
  E --> I{query.geneB != null?}
  G --> I
  H --> I

  I -- Yes --> J[cacheFetcher.findGeneBySymbol external try/catch]
  I -- No --> K[geneB default empty]
  J --> L{resolved geneB empty?}
  L -- Yes --> M[fallback from query.geneB]
  L -- No --> N[use resolved geneB]

  K --> O[FusionUtils.getFusionName external]
  M --> O
  N --> O
  O --> P[cacheFetcher.processQuery external]
  P --> Q[resp.query.id = query.id]
  Q --> R{more queries?}
  R -- Yes --> B
  R -- No --> S[return result list]
```
