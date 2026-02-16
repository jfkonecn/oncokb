# 04 POST /annotate/mutations/byGenomicChange

```mermaid
flowchart LR
  A["HTTP POST /annotate/mutations/byGenomicChange"] --> B{"body == null?"}
  B -- Yes --> C["Throw ApiHttpErrorException BAD_REQUEST"]
  B -- No --> D["annotateMutationsByGenomicChange(body)"]

  D --> E["Partition by referenceGenome; null defaults to GRCh37"]
  E --> E1{"Partition complete?"}
  E1 -- Yes --> F["annotateMutationsByGenomicChange(GRCh37, grch37Queries)"]
  F --> F1{"Always invoke GRCh38 helper next?"}
  F1 -- Yes --> G["annotateMutationsByGenomicChange(GRCh38, grch38Queries)"]
  G --> H["Reassemble original order"]

  H --> I["Per RG helper: build GN query set (dedupe) from genomicLocation"]
  I --> J["GenomeNexusUtils.getGenomicLocationVariantsAnnotation"]
  J --> K{"annotation count mismatch?"}
  K -- Yes --> L["Throw ApiException"]
  K -- No --> M["Loop each query"]

  M --> N{"Has GN annotation?"}
  N -- No --> O["getIndicatorQueryFromGenomicLocation(empty transcript)"]
  N -- Yes --> P["AlterationUtils.getAlterationsFromGenomeNexus"]
  P --> Q["getIndicatorQueryForCuratedHgvs"]
  Q --> R{"Curated response found?"}
  R -- Yes --> S["Use curated response"]
  R -- No --> T{"query.germline false?"}
  T -- Yes --> U["getIndicatorQueryFromGenomicLocation(selected transcript)"]
  T -- No --> V["indicator remains null"]
  V --> O

  U --> W["set hgvsInfo"]
  W --> X["set response query id"]
  S --> X
  O --> X
  X --> Y{"More queries?"}
  Y -- Yes --> M
  Y -- No --> Z["return list"]

  Z --> A1["JsonResultFactory.getIndicatorQueryRespWithoutGermline"]
  A1 --> A2["Return ResponseEntity 200"]

  subgraph CURATED["getIndicatorQueryForCuratedHgvs"]
    C1["find alteration by hgvsg"] --> C2{"match + germline aligned?"}
    C2 -- Yes --> C3["cacheFetcher.processQuery"]
    C2 -- No --> C4["extract hgvsc from transcript summary"]
    C4 --> C5{"hgvsc split by ':' gives 2 parts?"}
    C5 -- Yes --> C6["find alteration by hgvsc part"]
    C6 --> C7{"match + germline aligned?"}
    C7 -- Yes --> C8["cacheFetcher.processQuery"]
    C7 -- No --> C9["return null"]
    C5 -- No --> C9
  end

  subgraph FROM_LOC["getIndicatorQueryFromGenomicLocation"]
    G1["QueryUtils.getQueryFromAlteration"] --> G2["cacheFetcher.processQuery"]
    G2 --> G3["addTranscriptAndExonToResponse"]
    G3 --> G4["set query.hgvsInfo"]
  end
```

## Drill-Down
- `annotateMutationsByGenomicChange(List)`: `diagram/methods/annotateMutationsByGenomicChange-list.md`
