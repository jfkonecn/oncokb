package org.mskcc.cbio.oncokb.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.dto.GeneImportDto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Utility class to map JSON objects to GeneImportDto
 */
public class GeneImportMapper {

    private static final String LAST_EDIT_EXTENSION = "_review";
    private static final String UUID_EXTENSION = "_uuid";

    public static GeneImportDto mapFromJson(JSONObject geneInfo) throws JSONException {
        GeneImportDto dto = new GeneImportDto();

        // Basic gene info
        dto.setName(getStringValue(geneInfo, "name"));
        dto.setSummary(getStringValue(geneInfo, "summary"));
        dto.setBackground(getStringValue(geneInfo, "background"));
        dto.setIsoformOverride(getStringValue(geneInfo, "isoform_override"));
        dto.setDmpRefseqId(getStringValue(geneInfo, "dmp_refseq_id"));
        dto.setIsoformOverrideGrch38(getStringValue(geneInfo, "isoform_override_grch38"));
        dto.setDmpRefseqIdGrch38(getStringValue(geneInfo, "dmp_refseq_id_grch38"));

        // Gene type
        if (geneInfo.has("type")) {
            JSONObject typeObj = geneInfo.getJSONObject("type");
            GeneImportDto.GeneTypeDto typeDto = new GeneImportDto.GeneTypeDto();
            typeDto.setOcg(getStringValue(typeObj, "ocg"));
            typeDto.setTsg(getStringValue(typeObj, "tsg"));
            dto.setType(typeDto);
        }

        // Metadata
        dto.setSummaryUuid(getUuid(geneInfo, "summary"));
        dto.setSummaryLastEdit(getLastEdit(geneInfo, "summary"));
        dto.setBackgroundUuid(getUuid(geneInfo, "background"));
        dto.setBackgroundLastEdit(getLastEdit(geneInfo, "background"));

        // Mutations
        if (geneInfo.has("mutations")) {
            JSONArray mutationsArray = geneInfo.getJSONArray("mutations");
            List<GeneImportDto.MutationDto> mutations = new ArrayList<>();
            for (int i = 0; i < mutationsArray.length(); i++) {
                mutations.add(mapMutationFromJson(mutationsArray.getJSONObject(i)));
            }
            dto.setMutations(mutations);
        }

        return dto;
    }

    public static List<GeneImportDto.VusDto> mapVusFromJson(JSONArray vusArray) throws JSONException {
        if (vusArray == null) {
            return new ArrayList<>();
        }

        List<GeneImportDto.VusDto> vusList = new ArrayList<>();
        for (int i = 0; i < vusArray.length(); i++) {
            JSONObject vusObj = vusArray.getJSONObject(i);
            GeneImportDto.VusDto vusDto = new GeneImportDto.VusDto();
            vusDto.setName(getStringValue(vusObj, "name"));

            if (vusObj.has("time")) {
                JSONObject timeObj = vusObj.getJSONObject("time");
                GeneImportDto.TimeDto timeDto = new GeneImportDto.TimeDto();
                timeDto.setValue(timeObj.has("value") ? timeObj.getLong("value") : null);
                vusDto.setTime(timeDto);
            }

            vusList.add(vusDto);
        }
        return vusList;
    }

    private static GeneImportDto.MutationDto mapMutationFromJson(JSONObject mutationObj) throws JSONException {
        GeneImportDto.MutationDto mutationDto = new GeneImportDto.MutationDto();
        mutationDto.setName(getStringValue(mutationObj, "name"));
        mutationDto.setSummary(getStringValue(mutationObj, "summary"));

        // Metadata
        mutationDto.setSummaryUuid(getUuid(mutationObj, "summary"));
        mutationDto.setSummaryLastEdit(getLastEdit(mutationObj, "summary"));

        // Mutation effect
        if (mutationObj.has("mutation_effect")) {
            JSONObject effectObj = mutationObj.getJSONObject("mutation_effect");
            GeneImportDto.MutationEffectDto effectDto = new GeneImportDto.MutationEffectDto();
            effectDto.setOncogenic(getStringValue(effectObj, "oncogenic"));
            effectDto.setEffect(getStringValue(effectObj, "effect"));
            effectDto.setDescription(getStringValue(effectObj, "description"));

            // Metadata
            effectDto.setOncogenicUuid(getUuid(effectObj, "oncogenic"));
            effectDto.setOncogenicLastEdit(getLastEdit(effectObj, "oncogenic"));
            effectDto.setEffectUuid(getUuid(effectObj, "effect"));
            effectDto.setEffectLastEdit(getLastEdit(effectObj, "effect"));
            effectDto.setDescriptionLastEdit(getLastEdit(effectObj, "description"));

            mutationDto.setMutationEffect(effectDto);
        }

        // Tumors
        if (mutationObj.has("tumors")) {
            JSONArray tumorsArray = mutationObj.getJSONArray("tumors");
            List<GeneImportDto.TumorDto> tumors = new ArrayList<>();
            for (int i = 0; i < tumorsArray.length(); i++) {
                tumors.add(mapTumorFromJson(tumorsArray.getJSONObject(i)));
            }
            mutationDto.setTumors(tumors);
        }

        return mutationDto;
    }

    private static GeneImportDto.TumorDto mapTumorFromJson(JSONObject tumorObj) throws JSONException {
        GeneImportDto.TumorDto tumorDto = new GeneImportDto.TumorDto();

        // Cancer types
        if (tumorObj.has("cancerTypes")) {
            JSONArray cancerTypesArray = tumorObj.getJSONArray("cancerTypes");
            tumorDto.setCancerTypes(mapTumorTypesFromJson(cancerTypesArray));
        }

        // Excluded cancer types
        if (tumorObj.has("excludedCancerTypes")) {
            JSONArray excludedArray = tumorObj.getJSONArray("excludedCancerTypes");
            tumorDto.setExcludedCancerTypes(mapTumorTypesFromJson(excludedArray));
        }

        tumorDto.setSummary(getStringValue(tumorObj, "summary"));

        // Metadata
        tumorDto.setSummaryUuid(getUuid(tumorObj, "summary"));
        tumorDto.setSummaryLastEdit(getLastEdit(tumorObj, "summary"));

        // Prognostic
        if (tumorObj.has("prognostic")) {
            tumorDto.setPrognostic(mapPrognosticFromJson(tumorObj.getJSONObject("prognostic")));
        }

        // Diagnostic
        if (tumorObj.has("diagnostic")) {
            tumorDto.setDiagnostic(mapDiagnosticFromJson(tumorObj.getJSONObject("diagnostic")));
        }

        // Therapeutic implications
        if (tumorObj.has("TIs")) {
            JSONArray tiArray = tumorObj.getJSONArray("TIs");
            List<GeneImportDto.TherapeuticImplicationDto> tiList = new ArrayList<>();
            for (int i = 0; i < tiArray.length(); i++) {
                tiList.add(mapTherapeuticImplicationFromJson(tiArray.getJSONObject(i)));
            }
            tumorDto.setTherapeuticImplications(tiList);
        }

        return tumorDto;
    }

    private static List<GeneImportDto.TumorTypeDto> mapTumorTypesFromJson(JSONArray tumorTypesArray)
            throws JSONException {
        List<GeneImportDto.TumorTypeDto> tumorTypes = new ArrayList<>();
        for (int i = 0; i < tumorTypesArray.length(); i++) {
            JSONObject ttObj = tumorTypesArray.getJSONObject(i);
            GeneImportDto.TumorTypeDto ttDto = new GeneImportDto.TumorTypeDto();
            ttDto.setCode(getStringValue(ttObj, "code"));
            ttDto.setMainType(getStringValue(ttObj, "mainType"));
            tumorTypes.add(ttDto);
        }
        return tumorTypes;
    }

    private static GeneImportDto.PrognosticDto mapPrognosticFromJson(JSONObject prognosticObj) throws JSONException {
        GeneImportDto.PrognosticDto prognosticDto = new GeneImportDto.PrognosticDto();
        prognosticDto.setDescription(getStringValue(prognosticObj, "description"));
        prognosticDto.setLevel(getStringValue(prognosticObj, "level"));
        prognosticDto.setSummary(getStringValue(prognosticObj, "summary"));

        // Metadata
        prognosticDto.setUuid(getUuid(prognosticObj, null));
        prognosticDto.setDescriptionLastEdit(getLastEdit(prognosticObj, "description"));
        prognosticDto.setLevelLastEdit(getLastEdit(prognosticObj, "level"));
        prognosticDto.setSummaryLastEdit(getLastEdit(prognosticObj, "summary"));

        // Excluded relevant cancer types
        if (prognosticObj.has("excludedRCTs")) {
            JSONArray excludedArray = prognosticObj.getJSONArray("excludedRCTs");
            prognosticDto.setExcludedRelevantCancerTypes(mapTumorTypesFromJson(excludedArray));
        }

        return prognosticDto;
    }

    private static GeneImportDto.DiagnosticDto mapDiagnosticFromJson(JSONObject diagnosticObj) throws JSONException {
        GeneImportDto.DiagnosticDto diagnosticDto = new GeneImportDto.DiagnosticDto();
        diagnosticDto.setDescription(getStringValue(diagnosticObj, "description"));
        diagnosticDto.setLevel(getStringValue(diagnosticObj, "level"));
        diagnosticDto.setSummary(getStringValue(diagnosticObj, "summary"));

        // Metadata
        diagnosticDto.setUuid(getUuid(diagnosticObj, null));
        diagnosticDto.setDescriptionLastEdit(getLastEdit(diagnosticObj, "description"));
        diagnosticDto.setLevelLastEdit(getLastEdit(diagnosticObj, "level"));
        diagnosticDto.setSummaryLastEdit(getLastEdit(diagnosticObj, "summary"));

        // Excluded relevant cancer types
        if (diagnosticObj.has("excludedRCTs")) {
            JSONArray excludedArray = diagnosticObj.getJSONArray("excludedRCTs");
            diagnosticDto.setExcludedRelevantCancerTypes(mapTumorTypesFromJson(excludedArray));
        }

        return diagnosticDto;
    }

    private static GeneImportDto.TherapeuticImplicationDto mapTherapeuticImplicationFromJson(JSONObject tiObj)
            throws JSONException {
        GeneImportDto.TherapeuticImplicationDto tiDto = new GeneImportDto.TherapeuticImplicationDto();
        tiDto.setDescription(getStringValue(tiObj, "description"));

        // Treatments
        if (tiObj.has("treatments")) {
            JSONArray treatmentsArray = tiObj.getJSONArray("treatments");
            List<GeneImportDto.TreatmentDto> treatments = new ArrayList<>();
            for (int i = 0; i < treatmentsArray.length(); i++) {
                treatments.add(mapTreatmentFromJson(treatmentsArray.getJSONObject(i)));
            }
            tiDto.setTreatments(treatments);
        }

        return tiDto;
    }

    private static GeneImportDto.TreatmentDto mapTreatmentFromJson(JSONObject treatmentObj) throws JSONException {
        GeneImportDto.TreatmentDto treatmentDto = new GeneImportDto.TreatmentDto();
        treatmentDto.setLevel(getStringValue(treatmentObj, "level"));
        treatmentDto.setDescription(getStringValue(treatmentObj, "description"));
        treatmentDto.setIndication(getStringValue(treatmentObj, "indication"));
        treatmentDto.setFdaLevel(getStringValue(treatmentObj, "fdaLevel"));
        treatmentDto.setSolidPropagation(getStringValue(treatmentObj, "propagation"));
        treatmentDto.setLiquidPropagation(getStringValue(treatmentObj, "propagationLiquid"));

        // Metadata
        treatmentDto.setUuid(getUuid(treatmentObj, "name"));
        treatmentDto.setNameLastEdit(getLastEdit(treatmentObj, "name"));
        treatmentDto.setLevelLastEdit(getLastEdit(treatmentObj, "level"));
        treatmentDto.setDescriptionLastEdit(getLastEdit(treatmentObj, "description"));
        treatmentDto.setIndicationLastEdit(getLastEdit(treatmentObj, "indication"));

        // Drug names (complex nested structure)
        if (treatmentObj.has("name")) {
            JSONArray nameArray = treatmentObj.getJSONArray("name");
            List<List<GeneImportDto.DrugDto>> drugGroups = new ArrayList<>();
            for (int i = 0; i < nameArray.length(); i++) {
                JSONArray drugArray = nameArray.getJSONArray(i);
                List<GeneImportDto.DrugDto> drugs = new ArrayList<>();
                for (int j = 0; j < drugArray.length(); j++) {
                    JSONObject drugObj = drugArray.getJSONObject(j);
                    GeneImportDto.DrugDto drugDto = new GeneImportDto.DrugDto();
                    drugDto.setNcitCode(getStringValue(drugObj, "ncitCode"));
                    drugDto.setDrugName(getStringValue(drugObj, "drugName"));
                    drugDto.setUuid(getStringValue(drugObj, "uuid"));
                    drugs.add(drugDto);
                }
                drugGroups.add(drugs);
            }
            treatmentDto.setName(drugGroups);
        }

        // Excluded relevant cancer types
        if (treatmentObj.has("excludedRCTs")) {
            JSONArray excludedArray = treatmentObj.getJSONArray("excludedRCTs");
            treatmentDto.setExcludedRelevantCancerTypes(mapTumorTypesFromJson(excludedArray));
        }

        return treatmentDto;
    }

    // Helper methods
    private static String getStringValue(JSONObject obj, String key) {
        if (key == null || !obj.has(key)) {
            return null;
        }
        String value = obj.getString(key);
        return value != null && !value.trim().isEmpty() ? value.trim() : null;
    }

    private static String getUuid(JSONObject obj, String key) {
        String uuidKey = key != null ? key + UUID_EXTENSION : UUID_EXTENSION;
        return obj.has(uuidKey) ? obj.getString(uuidKey) : "";
    }

    private static Date getLastEdit(JSONObject obj, String key) {
        if (key == null) {
            return null;
        }
        String lastEditKey = key + LAST_EDIT_EXTENSION;
        if (!obj.has(lastEditKey)) {
            return null;
        }

        try {
            Object lastEditObj = obj.get(lastEditKey);
            if (lastEditObj instanceof JSONObject) {
                JSONObject reviewObj = (JSONObject) lastEditObj;
                if (reviewObj.has("updateTime")
                        && org.apache.commons.lang3.StringUtils.isNumeric(reviewObj.get("updateTime").toString())) {
                    return new Date(reviewObj.getLong("updateTime"));
                }
            }
        } catch (JSONException e) {
            // Ignore parsing errors for dates
        }
        return null;
    }
}