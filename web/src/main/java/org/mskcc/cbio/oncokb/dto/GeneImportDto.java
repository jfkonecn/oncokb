package org.mskcc.cbio.oncokb.dto;

import java.util.Date;
import java.util.List;

/**
 * DTO for gene import data to replace JSON object usage
 */
public class GeneImportDto {
    private String name;
    private String summary;
    private String background;
    private GeneTypeDto type;
    private String isoformOverride;
    private String dmpRefseqId;
    private String isoformOverrideGrch38;
    private String dmpRefseqIdGrch38;
    private List<MutationDto> mutations;
    private List<VusDto> vus;

    // Metadata fields
    private String summaryUuid;
    private Date summaryLastEdit;
    private String backgroundUuid;
    private Date backgroundLastEdit;

    public static class GeneTypeDto {
        private String ocg; // Oncogene
        private String tsg; // Tumor Suppressor Gene

        public String getOcg() {
            return ocg;
        }

        public void setOcg(String ocg) {
            this.ocg = ocg;
        }

        public String getTsg() {
            return tsg;
        }

        public void setTsg(String tsg) {
            this.tsg = tsg;
        }
    }

    public static class MutationDto {
        private String name;
        private MutationEffectDto mutationEffect;
        private String summary;
        private List<TumorDto> tumors;

        // Metadata
        private String summaryUuid;
        private Date summaryLastEdit;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public MutationEffectDto getMutationEffect() {
            return mutationEffect;
        }

        public void setMutationEffect(MutationEffectDto mutationEffect) {
            this.mutationEffect = mutationEffect;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public List<TumorDto> getTumors() {
            return tumors;
        }

        public void setTumors(List<TumorDto> tumors) {
            this.tumors = tumors;
        }

        public String getSummaryUuid() {
            return summaryUuid;
        }

        public void setSummaryUuid(String summaryUuid) {
            this.summaryUuid = summaryUuid;
        }

        public Date getSummaryLastEdit() {
            return summaryLastEdit;
        }

        public void setSummaryLastEdit(Date summaryLastEdit) {
            this.summaryLastEdit = summaryLastEdit;
        }
    }

    public static class MutationEffectDto {
        private String oncogenic;
        private String effect;
        private String description;

        // Metadata
        private String oncogenicUuid;
        private Date oncogenicLastEdit;
        private String effectUuid;
        private Date effectLastEdit;
        private Date descriptionLastEdit;

        public String getOncogenic() {
            return oncogenic;
        }

        public void setOncogenic(String oncogenic) {
            this.oncogenic = oncogenic;
        }

        public String getEffect() {
            return effect;
        }

        public void setEffect(String effect) {
            this.effect = effect;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getOncogenicUuid() {
            return oncogenicUuid;
        }

        public void setOncogenicUuid(String oncogenicUuid) {
            this.oncogenicUuid = oncogenicUuid;
        }

        public Date getOncogenicLastEdit() {
            return oncogenicLastEdit;
        }

        public void setOncogenicLastEdit(Date oncogenicLastEdit) {
            this.oncogenicLastEdit = oncogenicLastEdit;
        }

        public String getEffectUuid() {
            return effectUuid;
        }

        public void setEffectUuid(String effectUuid) {
            this.effectUuid = effectUuid;
        }

        public Date getEffectLastEdit() {
            return effectLastEdit;
        }

        public void setEffectLastEdit(Date effectLastEdit) {
            this.effectLastEdit = effectLastEdit;
        }

        public Date getDescriptionLastEdit() {
            return descriptionLastEdit;
        }

        public void setDescriptionLastEdit(Date descriptionLastEdit) {
            this.descriptionLastEdit = descriptionLastEdit;
        }
    }

    public static class TumorDto {
        private List<TumorTypeDto> cancerTypes;
        private List<TumorTypeDto> excludedCancerTypes;
        private String summary;
        private PrognosticDto prognostic;
        private DiagnosticDto diagnostic;
        private List<TherapeuticImplicationDto> therapeuticImplications;

        // Metadata
        private String summaryUuid;
        private Date summaryLastEdit;

        public List<TumorTypeDto> getCancerTypes() {
            return cancerTypes;
        }

        public void setCancerTypes(List<TumorTypeDto> cancerTypes) {
            this.cancerTypes = cancerTypes;
        }

        public List<TumorTypeDto> getExcludedCancerTypes() {
            return excludedCancerTypes;
        }

        public void setExcludedCancerTypes(List<TumorTypeDto> excludedCancerTypes) {
            this.excludedCancerTypes = excludedCancerTypes;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public PrognosticDto getPrognostic() {
            return prognostic;
        }

        public void setPrognostic(PrognosticDto prognostic) {
            this.prognostic = prognostic;
        }

        public DiagnosticDto getDiagnostic() {
            return diagnostic;
        }

        public void setDiagnostic(DiagnosticDto diagnostic) {
            this.diagnostic = diagnostic;
        }

        public List<TherapeuticImplicationDto> getTherapeuticImplications() {
            return therapeuticImplications;
        }

        public void setTherapeuticImplications(List<TherapeuticImplicationDto> therapeuticImplications) {
            this.therapeuticImplications = therapeuticImplications;
        }

        public String getSummaryUuid() {
            return summaryUuid;
        }

        public void setSummaryUuid(String summaryUuid) {
            this.summaryUuid = summaryUuid;
        }

        public Date getSummaryLastEdit() {
            return summaryLastEdit;
        }

        public void setSummaryLastEdit(Date summaryLastEdit) {
            this.summaryLastEdit = summaryLastEdit;
        }
    }

    public static class TumorTypeDto {
        private String code;
        private String mainType;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMainType() {
            return mainType;
        }

        public void setMainType(String mainType) {
            this.mainType = mainType;
        }
    }

    public static class PrognosticDto {
        private String description;
        private String level;
        private String summary;
        private List<TumorTypeDto> excludedRelevantCancerTypes;

        // Metadata
        private String uuid;
        private Date descriptionLastEdit;
        private Date levelLastEdit;
        private Date summaryLastEdit;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public List<TumorTypeDto> getExcludedRelevantCancerTypes() {
            return excludedRelevantCancerTypes;
        }

        public void setExcludedRelevantCancerTypes(List<TumorTypeDto> excludedRelevantCancerTypes) {
            this.excludedRelevantCancerTypes = excludedRelevantCancerTypes;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public Date getDescriptionLastEdit() {
            return descriptionLastEdit;
        }

        public void setDescriptionLastEdit(Date descriptionLastEdit) {
            this.descriptionLastEdit = descriptionLastEdit;
        }

        public Date getLevelLastEdit() {
            return levelLastEdit;
        }

        public void setLevelLastEdit(Date levelLastEdit) {
            this.levelLastEdit = levelLastEdit;
        }

        public Date getSummaryLastEdit() {
            return summaryLastEdit;
        }

        public void setSummaryLastEdit(Date summaryLastEdit) {
            this.summaryLastEdit = summaryLastEdit;
        }
    }

    public static class DiagnosticDto {
        private String description;
        private String level;
        private String summary;
        private List<TumorTypeDto> excludedRelevantCancerTypes;

        // Metadata
        private String uuid;
        private Date descriptionLastEdit;
        private Date levelLastEdit;
        private Date summaryLastEdit;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public List<TumorTypeDto> getExcludedRelevantCancerTypes() {
            return excludedRelevantCancerTypes;
        }

        public void setExcludedRelevantCancerTypes(List<TumorTypeDto> excludedRelevantCancerTypes) {
            this.excludedRelevantCancerTypes = excludedRelevantCancerTypes;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public Date getDescriptionLastEdit() {
            return descriptionLastEdit;
        }

        public void setDescriptionLastEdit(Date descriptionLastEdit) {
            this.descriptionLastEdit = descriptionLastEdit;
        }

        public Date getLevelLastEdit() {
            return levelLastEdit;
        }

        public void setLevelLastEdit(Date levelLastEdit) {
            this.levelLastEdit = levelLastEdit;
        }

        public Date getSummaryLastEdit() {
            return summaryLastEdit;
        }

        public void setSummaryLastEdit(Date summaryLastEdit) {
            this.summaryLastEdit = summaryLastEdit;
        }
    }

    public static class TherapeuticImplicationDto {
        private String description;
        private List<TreatmentDto> treatments;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<TreatmentDto> getTreatments() {
            return treatments;
        }

        public void setTreatments(List<TreatmentDto> treatments) {
            this.treatments = treatments;
        }
    }

    public static class TreatmentDto {
        private List<List<DrugDto>> name;
        private String level;
        private String description;
        private String indication;
        private String fdaLevel;
        private String solidPropagation;
        private String liquidPropagation;
        private List<TumorTypeDto> excludedRelevantCancerTypes;

        // Metadata
        private String uuid;
        private Date nameLastEdit;
        private Date levelLastEdit;
        private Date descriptionLastEdit;
        private Date indicationLastEdit;

        public List<List<DrugDto>> getName() {
            return name;
        }

        public void setName(List<List<DrugDto>> name) {
            this.name = name;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getIndication() {
            return indication;
        }

        public void setIndication(String indication) {
            this.indication = indication;
        }

        public String getFdaLevel() {
            return fdaLevel;
        }

        public void setFdaLevel(String fdaLevel) {
            this.fdaLevel = fdaLevel;
        }

        public String getSolidPropagation() {
            return solidPropagation;
        }

        public void setSolidPropagation(String solidPropagation) {
            this.solidPropagation = solidPropagation;
        }

        public String getLiquidPropagation() {
            return liquidPropagation;
        }

        public void setLiquidPropagation(String liquidPropagation) {
            this.liquidPropagation = liquidPropagation;
        }

        public List<TumorTypeDto> getExcludedRelevantCancerTypes() {
            return excludedRelevantCancerTypes;
        }

        public void setExcludedRelevantCancerTypes(List<TumorTypeDto> excludedRelevantCancerTypes) {
            this.excludedRelevantCancerTypes = excludedRelevantCancerTypes;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public Date getNameLastEdit() {
            return nameLastEdit;
        }

        public void setNameLastEdit(Date nameLastEdit) {
            this.nameLastEdit = nameLastEdit;
        }

        public Date getLevelLastEdit() {
            return levelLastEdit;
        }

        public void setLevelLastEdit(Date levelLastEdit) {
            this.levelLastEdit = levelLastEdit;
        }

        public Date getDescriptionLastEdit() {
            return descriptionLastEdit;
        }

        public void setDescriptionLastEdit(Date descriptionLastEdit) {
            this.descriptionLastEdit = descriptionLastEdit;
        }

        public Date getIndicationLastEdit() {
            return indicationLastEdit;
        }

        public void setIndicationLastEdit(Date indicationLastEdit) {
            this.indicationLastEdit = indicationLastEdit;
        }
    }

    public static class DrugDto {
        private String ncitCode;
        private String drugName;
        private String uuid;

        public String getNcitCode() {
            return ncitCode;
        }

        public void setNcitCode(String ncitCode) {
            this.ncitCode = ncitCode;
        }

        public String getDrugName() {
            return drugName;
        }

        public void setDrugName(String drugName) {
            this.drugName = drugName;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }
    }

    public static class VusDto {
        private String name;
        private TimeDto time;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public TimeDto getTime() {
            return time;
        }

        public void setTime(TimeDto time) {
            this.time = time;
        }
    }

    public static class TimeDto {
        private Long value;

        public Long getValue() {
            return value;
        }

        public void setValue(Long value) {
            this.value = value;
        }
    }

    // Main class getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public GeneTypeDto getType() {
        return type;
    }

    public void setType(GeneTypeDto type) {
        this.type = type;
    }

    public String getIsoformOverride() {
        return isoformOverride;
    }

    public void setIsoformOverride(String isoformOverride) {
        this.isoformOverride = isoformOverride;
    }

    public String getDmpRefseqId() {
        return dmpRefseqId;
    }

    public void setDmpRefseqId(String dmpRefseqId) {
        this.dmpRefseqId = dmpRefseqId;
    }

    public String getIsoformOverrideGrch38() {
        return isoformOverrideGrch38;
    }

    public void setIsoformOverrideGrch38(String isoformOverrideGrch38) {
        this.isoformOverrideGrch38 = isoformOverrideGrch38;
    }

    public String getDmpRefseqIdGrch38() {
        return dmpRefseqIdGrch38;
    }

    public void setDmpRefseqIdGrch38(String dmpRefseqIdGrch38) {
        this.dmpRefseqIdGrch38 = dmpRefseqIdGrch38;
    }

    public List<MutationDto> getMutations() {
        return mutations;
    }

    public void setMutations(List<MutationDto> mutations) {
        this.mutations = mutations;
    }

    public List<VusDto> getVus() {
        return vus;
    }

    public void setVus(List<VusDto> vus) {
        this.vus = vus;
    }

    public String getSummaryUuid() {
        return summaryUuid;
    }

    public void setSummaryUuid(String summaryUuid) {
        this.summaryUuid = summaryUuid;
    }

    public Date getSummaryLastEdit() {
        return summaryLastEdit;
    }

    public void setSummaryLastEdit(Date summaryLastEdit) {
        this.summaryLastEdit = summaryLastEdit;
    }

    public String getBackgroundUuid() {
        return backgroundUuid;
    }

    public void setBackgroundUuid(String backgroundUuid) {
        this.backgroundUuid = backgroundUuid;
    }

    public Date getBackgroundLastEdit() {
        return backgroundLastEdit;
    }

    public void setBackgroundLastEdit(Date backgroundLastEdit) {
        this.backgroundLastEdit = backgroundLastEdit;
    }
}