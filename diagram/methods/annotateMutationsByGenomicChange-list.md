# annotateMutationsByGenomicChange(List<AnnotateMutationByGenomicChangeQuery>)

```mermaid
flowchart LR
  A["annotateMutationsByGenomicChange(List)"] --> B["partition by referenceGenome"]
  B --> C{"query.referenceGenome null?"}
  C -- Yes --> D["set query.referenceGenome = GRCh37"]
  C -- No --> E["keep RG"]
  D --> F{"RG == GRCh38?"}
  E --> F
  F -- Yes --> G["append to grch38 + index map"]
  F -- No --> H["append to grch37 + index map"]
  G --> I{"more queries?"}
  H --> I
  I -- Yes --> C
  I -- No --> J{"partition complete?"}
  J -- Yes --> K["call annotateMutationsByGenomicChange(GRCh37, grch37Queries)"]
  K --> K1{"always invoke GRCh38 helper next?"}
  K1 -- Yes --> L["call annotateMutationsByGenomicChange(GRCh38, grch38Queries)"]
  L --> M["reassemble outputs in original input order"]
  M --> N["return result list"]
```

Downstream method: `diagram/methods/annotateMutationsByGenomicChange-rg-list.md`
