# 09 GET /annotate/copyNumberAlterations

```mermaid
flowchart LR
  A[HTTP GET /annotate/copyNumberAlterations] --> B{entrezGeneId and hugoSymbol both present?}
  B -- No --> E{copyNameAlterationType == null?}
  B -- Yes --> C{GeneUtils.isSameGene(entrezGeneId,hugoSymbol)?}
  C -- No --> D[Throw ApiHttpErrorException BAD_REQUEST]
  C -- Yes --> E

  E -- Yes --> F[Throw ApiHttpErrorException BAD_REQUEST missing copyNameAlterationType]
  E -- No --> G[resolveMatchedRG(referenceGenome)]

  G --> H{referenceGenome empty?}
  H -- No --> I[MainUtils.searchEnum]
  I --> J{matchedRG null?}
  J -- Yes --> K[Throw ApiHttpErrorException BAD_REQUEST invalid referenceGenome]
  J -- No --> L[MainUtils.stringToEvidenceTypes]
  H -- Yes --> L

  L --> M[cacheFetcher.processQuery with CNA alteration string]
  M --> N[JsonResultFactory.getIndicatorQueryRespWithoutGermline]
  N --> O[Return ResponseEntity 200]
```

## Drill-Down
- `resolveMatchedRG`: `diagram/methods/resolveMatchedRG.md`
