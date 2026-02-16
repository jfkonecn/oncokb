# 02 POST /annotate/mutations/byProteinChange

```mermaid
flowchart LR
  A[HTTP POST /annotate/mutations/byProteinChange] --> B{body == null?}
  B -- Yes --> C[Throw ApiHttpErrorException BAD_REQUEST]
  B -- No --> D[annotateMutationsByProteinChange(body)]

  D --> E[Loop each AnnotateMutationByProteinChangeQuery]
  E --> F[cacheFetcher.processQuery(...)]
  F --> G[resp.query.id = query.id]
  G --> H{More queries?}
  H -- Yes --> E
  H -- No --> I[Return list from helper]

  I --> J[JsonResultFactory.getIndicatorQueryRespWithoutGermline(list)]
  J --> K[Return ResponseEntity 200]
```

## Drill-Down
- `annotateMutationsByProteinChange(List)`: `diagram/methods/annotateMutationsByProteinChange-list.md`
