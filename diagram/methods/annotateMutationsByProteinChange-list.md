# annotateMutationsByProteinChange(List<AnnotateMutationByProteinChangeQuery>)

```mermaid
flowchart LR
  A[annotateMutationsByProteinChange(List)] --> B[loop queries]
  B --> C[cacheFetcher.processQuery external]
  C --> D[resp.query.id = query.id]
  D --> E{more queries?}
  E -- Yes --> B
  E -- No --> F[return result list]
```
