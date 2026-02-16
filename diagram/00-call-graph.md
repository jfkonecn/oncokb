# 00 Controller Call Graph (Drill-Down Index)

```mermaid
flowchart LR
  E1["GET byProteinChange"] --> M1["resolveMatchedRG"];
  E2["POST byProteinChange"] --> M2["annotateMutationsByProteinChange list"];

  E3["GET byGenomicChange"] --> M1;
  E3 --> M3["annotateMutationsByGenomicChange list"];
  E4["POST byGenomicChange"] --> M3;

  E5["GET byHGVSg"] --> M1;
  E5 --> M8["annotateMutationsByHGVSg list"];
  E6["POST byHGVSg"] --> M8;

  E7["GET byHGVSc"] --> M1;
  E7 --> M10["annotateMutationsByHGVSc list"];
  E8["POST byHGVSc"] --> M10;

  E9["GET copyNumberAlterations"] --> M1;
  E10["POST copyNumberAlterations"] --> M14["annotateCopyNumberAlterations list"];

  E11["GET structuralVariants"] --> M1;
  E12["POST structuralVariants"] --> M13["annotateStructuralVariants list"];

  E14["POST annotateSample"] --> M15["annotateSample"];

  M15 --> M16["setTumorTypeForQueries"];
  M15 --> M13;
  M15 --> M14;
  M15 --> M3;
  M15 --> M2;
  M15 --> M8;

  M3 --> M4["annotateMutationsByGenomicChange rg list"];
  M4 --> M5["getIndicatorQueryForCuratedHgvs"];
  M4 --> M6["getIndicatorQueryFromGenomicLocation"];
  M6 --> M12["addTranscriptAndExonToResponse"];

  M8 --> M9["annotateMutationsByHGVSg rg list"];
  M9 --> M5;
  M9 --> M7["getIndicatorQueryFromHGVS"];
  M7 --> M12;

  M10 --> M11["annotateMutationsByHGVSc rg list"];
  M11 --> M7;
```

## Method Diagram Links
- `resolveMatchedRG`: `diagram/methods/resolveMatchedRG.md`
- `annotateMutationsByProteinChange(List)`: `diagram/methods/annotateMutationsByProteinChange-list.md`
- `annotateMutationsByGenomicChange(List)`: `diagram/methods/annotateMutationsByGenomicChange-list.md`
- `annotateMutationsByGenomicChange(ReferenceGenome,List)`: `diagram/methods/annotateMutationsByGenomicChange-rg-list.md`
- `getIndicatorQueryForCuratedHgvs`: `diagram/methods/getIndicatorQueryForCuratedHgvs.md`
- `getIndicatorQueryFromGenomicLocation`: `diagram/methods/getIndicatorQueryFromGenomicLocation.md`
- `getIndicatorQueryFromHGVS`: `diagram/methods/getIndicatorQueryFromHGVS.md`
- `annotateMutationsByHGVSg(List)`: `diagram/methods/annotateMutationsByHGVSg-list.md`
- `annotateMutationsByHGVSg(ReferenceGenome,List)`: `diagram/methods/annotateMutationsByHGVSg-rg-list.md`
- `annotateMutationsByHGVSc(List)`: `diagram/methods/annotateMutationsByHGVSc-list.md`
- `annotateMutationsByHGVSc(ReferenceGenome,List)`: `diagram/methods/annotateMutationsByHGVSc-rg-list.md`
- `addTranscriptAndExonToResponse`: `diagram/methods/addTranscriptAndExonToResponse.md`
- `annotateStructuralVariants(List)`: `diagram/methods/annotateStructuralVariants-list.md`
- `annotateCopyNumberAlterations(List)`: `diagram/methods/annotateCopyNumberAlterations-list.md`
- `annotateSample`: `diagram/methods/annotateSample.md`
- `setTumorTypeForQueries`: `diagram/methods/setTumorTypeForQueries.md`
- `processQuery (CacheFetcher + IndicatorUtils)`: `diagram/methods/processQuery.md`
