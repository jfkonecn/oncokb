# annotateMutationsByHGVSc(List<AnnotateMutationByHGVScQuery>)

```mermaid
flowchart LR
  A["annotateMutationsByHGVSc(List)"] --> B["partition by referenceGenome"]
  B --> C{"query.referenceGenome null?"}
  C -- Yes --> D["RG = GRCh37"]
  C -- No --> E["RG = provided"]
  D --> F["query.setReferenceGenome(RG)"]
  E --> F
  F --> G{"RG == GRCh38?"}
  G -- Yes --> H["append to grch38 + index map"]
  G -- No --> I["append to grch37 + index map"]
  H --> J{"more queries?"}
  I --> J
  J -- Yes --> C
  J -- No --> K{"partition complete?"}
  K -- Yes --> L["call annotateMutationsByHGVSc(GRCh37, grch37Queries)"]
  L --> L1{"always invoke GRCh38 helper next?"}
  L1 -- Yes --> M["call annotateMutationsByHGVSc(GRCh38, grch38Queries)"]
  M --> N["reassemble original order"]
  N --> O["return result list"]
```

Downstream method: `diagram/methods/annotateMutationsByHGVSc-rg-list.md`
