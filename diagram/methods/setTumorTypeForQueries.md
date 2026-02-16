# setTumorTypeForQueries(List<T>, String tumorType)

```mermaid
flowchart LR
  A["setTumorTypeForQueries"] --> B{Strings.isEmpty(tumorType) external?}
  B -- Yes --> C["return"]
  B -- No --> D["loop queries"]
  D --> E["query.setTumorType(tumorType)"]
  E --> F{more queries?}
  F -- Yes --> D
  F -- No --> C
```
