# annotateCopyNumberAlterations(List<AnnotateCopyNumberAlterationQuery>)

```mermaid
flowchart LR
  A[annotateCopyNumberAlterations(List)] --> B[loop queries]
  B --> C{query.gene != null?}
  C -- No --> D[gene default empty]
  C -- Yes --> E[cacheFetcher.findGeneBySymbol external try/catch]
  E --> F{resolved gene null?}
  F -- Yes --> G[fallback gene from query input]
  F -- No --> H[use resolved gene]

  D --> I[cacheFetcher.processQuery external]
  G --> I
  H --> I
  I --> J[resp.query.id = query.id]
  J --> K{more queries?}
  K -- Yes --> B
  K -- No --> L[return result list]
```
