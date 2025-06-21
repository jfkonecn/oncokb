/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.bo.*;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.mskcc.cbio.oncokb.util.ArticleUtils.getAbstractFromText;
import static org.mskcc.cbio.oncokb.util.ArticleUtils.getPmidsFromText;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.mskcc.cbio.oncokb.dto.GeneImportDto;
import org.mskcc.cbio.oncokb.util.GeneImportMapper;

/**
 * @author jgao
 */
@Controller
public class DriveAnnotationParser {
    OncokbTranscriptService oncokbTranscriptService = new OncokbTranscriptService();

    @RequestMapping(value = "/legacy-api/driveAnnotation", method = POST)
    public @ResponseBody synchronized void getEvidence(
            @RequestParam(value = "gene") String gene,
            @RequestParam(value = "releaseGene", defaultValue = "FALSE") Boolean releaseGene,
            @RequestParam(value = "vus", required = false) String vus) throws Exception {

        if (gene == null) {
            System.out.println("#No gene info available.");
        } else {
            JSONObject jsonObj = new JSONObject(gene);
            JSONArray jsonArray = null;
            if (vus != null) {
                jsonArray = new JSONArray(vus);
            }
            parseGene(jsonObj, releaseGene, jsonArray);
        }
    }

    private static final String LAST_EDIT_EXTENSION = "_review";
    private static final String UUID_EXTENSION = "_uuid";
    private static final String SOLID_PROPAGATION_KEY = "propagation";
    private static final String LIQUID_PROPAGATION_KEY = "propagationLiquid";
    private static final String FDA_LEVEL_KEY = "fdaLevel";
    private static final String EXCLUDED_RCTS_KEY = "excludedRCTs";

    public void parseVUS(Gene gene, JSONArray vus, Integer nestLevel) throws JSONException {
        System.out.println(spaceStrByNestLevel(nestLevel) + "Variants of unknown significance");
        if (vus != null) {
            AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
            EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
            AlterationType type = AlterationType.MUTATION; // TODO: cna and fusion

            System.out.println("\t" + vus.length() + " VUSs");
            for (int i = 0; i < vus.length(); i++) {
                JSONObject variant = vus.getJSONObject(i);
                String mutationStr = variant.has("name") ? variant.getString("name") : null;
                JSONObject time = variant.has("time") ? variant.getJSONObject("time") : null;
                Long lastEdit = null;
                if (time != null) {
                    lastEdit = time.has("value") ? time.getLong("value") : null;
                }
                // JSONArray nameComments = variant.has("nameComments") ?
                // variant.getJSONArray("nameComments") : null;
                if (mutationStr != null) {
                    List<Alteration> mutations = AlterationUtils.parseMutationString(mutationStr, ",");
                    Set<Alteration> alterations = new HashSet<>();
                    for (Alteration mutation : mutations) {
                        Alteration alteration = alterationBo.findAlteration(gene, type, mutation.getAlteration());
                        if (alteration == null) {
                            alteration = new Alteration();
                            alteration.setGene(gene);
                            alteration.setAlterationType(type);
                            alteration.setAlteration(mutation.getAlteration());
                            alteration.setName(mutation.getName());
                            alteration.setReferenceGenomes(mutation.getReferenceGenomes());
                            AlterationUtils.annotateAlteration(alteration, mutation.getAlteration());
                            alterationBo.save(alteration);
                        } else if (!alteration.getReferenceGenomes().equals(mutation.getReferenceGenomes())) {
                            alteration.setReferenceGenomes(mutation.getReferenceGenomes());
                            alterationBo.save(alteration);
                        }
                        alterations.add(alteration);
                    }

                    Evidence evidence = new Evidence();
                    evidence.setEvidenceType(EvidenceType.VUS);
                    evidence.setGene(gene);
                    evidence.setAlterations(alterations);
                    if (lastEdit != null) {
                        Date date = new Date(lastEdit);
                        evidence.setLastEdit(date);
                        // evidence.setLastReview(date);
                    }
                    if (evidence.getLastEdit() == null) {
                        System.out.println(spaceStrByNestLevel(nestLevel + 1) + "WARNING: " + mutationStr
                                + " do not have last update.");
                    }
                    evidenceBo.save(evidence);
                }
                if (i % 10 == 9)
                    System.out.println("\t\tImported " + (i + 1));
            }
        } else {
            if (vus == null) {
                System.out.println(spaceStrByNestLevel(nestLevel) + "No VUS available.");
            }
        }
    }

    private void updateGeneInfo(JSONObject geneInfo, Gene gene) {
        JSONObject geneType = geneInfo.has("type") ? geneInfo.getJSONObject("type") : null;
        String oncogene = geneType == null ? null : (geneType.has("ocg") ? geneType.getString("ocg").trim() : null);
        String tsg = geneType == null ? null : (geneType.has("tsg") ? geneType.getString("tsg").trim() : null);

        if (oncogene != null) {
            if (oncogene.equals("Oncogene")) {
                gene.setOncogene(true);
            } else {
                gene.setOncogene(false);
            }
        }
        if (tsg != null) {
            if (tsg.equals("Tumor Suppressor")) {
                gene.setTSG(true);
            } else {
                gene.setTSG(false);
            }
        }

        String grch37Isoform = geneInfo.has("isoform_override") ? geneInfo.getString("isoform_override") : null;
        String grch37RefSeq = geneInfo.has("dmp_refseq_id") ? geneInfo.getString("dmp_refseq_id") : null;
        String grch38Isoform = geneInfo.has("isoform_override_grch38") ? geneInfo.getString("isoform_override_grch38")
                : null;
        String grch38RefSeq = geneInfo.has("dmp_refseq_id_grch38") ? geneInfo.getString("dmp_refseq_id_grch38") : null;

        if (grch37Isoform != null) {
            gene.setGrch37Isoform(grch37Isoform);
        }
        if (grch37RefSeq != null) {
            gene.setGrch37RefSeq(grch37RefSeq);
        }
        if (grch38Isoform != null) {
            gene.setGrch38Isoform(grch38Isoform);
        }
        if (grch38RefSeq != null) {
            gene.setGrch38RefSeq(grch38RefSeq);
        }
    }

    private Gene parseGene(JSONObject geneInfo, Boolean releaseGene, JSONArray vus) throws Exception {
        // Phase 1: Parse JSON and convert to DTO
        GeneImportDto geneImportDto = GeneImportMapper.mapFromJson(geneInfo);
        List<GeneImportDto.VusDto> vusList = GeneImportMapper.mapVusFromJson(vus);

        // Phase 2: Convert DTO to domain objects
        GeneImportResult importResult = convertDtoToDomainObjects(geneImportDto, releaseGene, vusList);

        // Phase 3: Save all changes in a single transaction
        if (importResult != null) {
            saveAllChanges(importResult);
        }

        return importResult != null ? importResult.getGene() : null;
    }

    /**
     * Phase 2: Convert DTO to domain objects without saving to database
     */
    private GeneImportResult convertDtoToDomainObjects(GeneImportDto geneImportDto, Boolean releaseGene,
            List<GeneImportDto.VusDto> vusList) throws Exception {
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        Integer nestLevel = 1;

        if (geneImportDto.getName() == null || geneImportDto.getName().trim().isEmpty()) {
            return null;
        }

        String hugo = geneImportDto.getName().trim();
        Gene gene = geneBo.findGeneByHugoSymbol(hugo);

        if (gene == null) {
            System.out.println(spaceStrByNestLevel(nestLevel) + "Gene " + hugo + " is not in the released list.");
            if (releaseGene) {
                OncokbTranscriptService oncokbTranscriptService = new OncokbTranscriptService();
                gene = oncokbTranscriptService.findGeneBySymbol(hugo);
                if (gene == null) {
                    System.out.println("!!!!!!!!!Could not find gene " + hugo + " either.");
                    throw new IOException("!!!!!!!!!Could not find gene " + hugo + ".");
                }
            } else {
                return null;
            }
        }

        if (gene == null) {
            System.out.print(spaceStrByNestLevel(nestLevel) + "No info about " + hugo);
            return null;
        }

        System.out.println(spaceStrByNestLevel(nestLevel) + "Gene: " + gene.getHugoSymbol());

        // Create result container
        GeneImportResult result = new GeneImportResult();
        result.setGene(gene);
        result.setGeneImportDto(geneImportDto);
        result.setVusList(vusList);

        // Update gene info
        updateGeneInfoFromDto(geneImportDto, gene);

        // Phase 2: Convert all DTOs to domain objects
        convertSummaryToDomainObjects(gene, geneImportDto, result, nestLevel + 1);
        convertBackgroundToDomainObjects(gene, geneImportDto, result, nestLevel + 1);
        convertMutationsToDomainObjects(gene, geneImportDto.getMutations(), result, nestLevel + 1);
        convertVusToDomainObjects(gene, vusList, result, nestLevel + 1);

        return result;
    }

    /**
     * Phase 3: Save all changes in a single transaction
     */
    private void saveAllChanges(GeneImportResult result) throws Exception {
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
        DrugBo drugBo = ApplicationContextSingleton.getDrugBo();
        ArticleBo articleBo = ApplicationContextSingleton.getArticleBo();

        // Delete existing data first
        Gene gene = result.getGene();
        List<Evidence> existingEvidences = evidenceBo.findEvidencesByGene(Collections.singleton(gene));
        List<Alteration> existingAlterations = alterationBo.findAlterationsByGene(Collections.singleton(gene));

        for (Evidence evidence : existingEvidences) {
            evidenceBo.delete(evidence);
        }
        for (Alteration alteration : existingAlterations) {
            alterationBo.delete(alteration);
        }

        // Update cache before saving new data
        CacheUtils.updateGene(Collections.singleton(gene.getEntrezGeneId()), false);

        // Save all new data in batches
        if (!result.getAlterationsToSave().isEmpty()) {
            alterationBo.saveAll(result.getAlterationsToSave());
        }

        if (!result.getDrugsToSave().isEmpty()) {
            drugBo.saveAll(result.getDrugsToSave());
        }

        if (!result.getArticlesToSave().isEmpty()) {
            articleBo.saveAll(result.getArticlesToSave());
        }

        if (!result.getEvidencesToSave().isEmpty()) {
            evidenceBo.saveAll(result.getEvidencesToSave());
        }

        // Update gene
        geneBo.update(gene);

        // Update cache after saving
        CacheUtils.updateGene(Collections.singleton(gene.getEntrezGeneId()), true);
    }

    private void convertSummaryToDomainObjects(Gene gene, GeneImportDto geneImportDto,
            GeneImportResult result, Integer nestLevel) {
        System.out.println(spaceStrByNestLevel(nestLevel) + "Summary");
        String geneSummary = geneImportDto.getSummary();

        if (geneSummary != null && !geneSummary.isEmpty()) {
            Evidence evidence = new Evidence();
            evidence.setEvidenceType(EvidenceType.GENE_SUMMARY);
            evidence.setGene(gene);
            evidence.setDescription(geneSummary);
            evidence.setUuid(geneImportDto.getSummaryUuid());
            evidence.setLastEdit(geneImportDto.getSummaryLastEdit());

            if (geneImportDto.getSummaryLastEdit() != null) {
                System.out.println(spaceStrByNestLevel(nestLevel + 1) +
                        "Last update on: " + MainUtils.getTimeByDate(geneImportDto.getSummaryLastEdit()));
            }

            convertDocumentsToDomainObjects(geneSummary, evidence, result);
            result.getEvidencesToSave().add(evidence);
            System.out.println(spaceStrByNestLevel(nestLevel + 1) + "Has description");
        }
    }

    private void convertBackgroundToDomainObjects(Gene gene, GeneImportDto geneImportDto,
            GeneImportResult result, Integer nestLevel) {
        System.out.println(spaceStrByNestLevel(nestLevel) + "Background");
        String bg = geneImportDto.getBackground();

        if (bg != null && !bg.isEmpty()) {
            Evidence evidence = new Evidence();
            evidence.setEvidenceType(EvidenceType.GENE_BACKGROUND);
            evidence.setGene(gene);
            evidence.setDescription(bg);
            evidence.setUuid(geneImportDto.getBackgroundUuid());
            evidence.setLastEdit(geneImportDto.getBackgroundLastEdit());

            if (geneImportDto.getBackgroundLastEdit() != null) {
                System.out.println(spaceStrByNestLevel(nestLevel + 1) +
                        "Last update on: " + MainUtils.getTimeByDate(geneImportDto.getBackgroundLastEdit()));
            }

            convertDocumentsToDomainObjects(bg, evidence, result);
            result.getEvidencesToSave().add(evidence);
            System.out.println(spaceStrByNestLevel(nestLevel + 1) + "Has description");
        }
    }

    private void convertMutationsToDomainObjects(Gene gene, List<GeneImportDto.MutationDto> mutations,
            GeneImportResult result, Integer nestLevel) throws Exception {
        if (mutations != null) {
            System.out.println(spaceStrByNestLevel(nestLevel) + mutations.size() + " mutations.");
            for (GeneImportDto.MutationDto mutationDto : mutations) {
                convertMutationToDomainObjects(gene, mutationDto, result, nestLevel + 1);
            }
        } else {
            System.out.println(spaceStrByNestLevel(nestLevel) + "No mutation.");
        }
    }

    private void convertMutationToDomainObjects(Gene gene, GeneImportDto.MutationDto mutationDto,
            GeneImportResult result, Integer nestLevel) throws Exception {
        String mutationStr = mutationDto.getName();

        if (mutationStr != null && !mutationStr.isEmpty() && !mutationStr.contains("?")) {
            System.out.println(spaceStrByNestLevel(nestLevel) + "Mutation: " + mutationStr);

            AlterationType type = AlterationType.MUTATION; // TODO: cna and fusion
            Set<Alteration> alterations = new HashSet<>();

            GeneImportDto.MutationEffectDto mutationEffect = mutationDto.getMutationEffect();
            Oncogenicity oncogenic = getOncogenicityFromDto(mutationEffect);
            String oncogenic_uuid = mutationEffect != null ? mutationEffect.getOncogenicUuid() : null;
            Date oncogenic_lastEdit = mutationEffect != null ? mutationEffect.getOncogenicLastEdit() : null;

            Set<Date> lastEditDatesEffect = new HashSet<>();
            String effect = mutationEffect != null ? mutationEffect.getEffect() : null;
            if (mutationEffect != null) {
                addDateToSet(lastEditDatesEffect, mutationEffect.getEffectLastEdit());
            }
            String effect_uuid = mutationEffect != null ? mutationEffect.getEffectUuid() : null;

            List<Alteration> mutations = AlterationUtils.parseMutationString(mutationStr, ",");
            for (Alteration mutation : mutations) {
                Alteration alteration = findOrCreateAlteration(gene, type, mutation, result);
                alterations.add(alteration);
                convertOncogenicToDomainObjects(gene, alteration, oncogenic, oncogenic_uuid, oncogenic_lastEdit,
                        result);
            }

            // Convert mutation effect
            String effectDesc = mutationEffect != null ? mutationEffect.getDescription() : null;
            if (mutationEffect != null) {
                addDateToSet(lastEditDatesEffect, mutationEffect.getDescriptionLastEdit());
            }

            if (!com.mysql.jdbc.StringUtils.isNullOrEmpty(effect)
                    || !com.mysql.jdbc.StringUtils.isNullOrEmpty(effectDesc)) {
                Evidence evidence = new Evidence();
                evidence.setEvidenceType(EvidenceType.MUTATION_EFFECT);
                evidence.setAlterations(alterations);
                evidence.setGene(gene);

                if ((effectDesc != null && !effectDesc.trim().isEmpty())) {
                    evidence.setDescription(effectDesc);
                    convertDocumentsToDomainObjects(effectDesc, evidence, result);
                }
                evidence.setKnownEffect(effect);
                evidence.setUuid(effect_uuid);

                Date effect_lastEdit = getMostRecentDate(lastEditDatesEffect);
                evidence.setLastEdit(effect_lastEdit);
                result.getEvidencesToSave().add(evidence);
            }

            // Convert mutation summary
            String mutationSummary = mutationDto.getSummary();
            if (StringUtils.isNotEmpty(mutationSummary)) {
                Evidence evidence = new Evidence();
                evidence.setEvidenceType(EvidenceType.MUTATION_SUMMARY);
                evidence.setAlterations(alterations);
                evidence.setGene(gene);
                evidence.setDescription(mutationSummary);
                convertDocumentsToDomainObjects(mutationSummary, evidence, result);
                evidence.setUuid(mutationDto.getSummaryUuid());
                evidence.setLastEdit(mutationDto.getSummaryLastEdit());
                result.getEvidencesToSave().add(evidence);
            }

            // Convert cancers
            if (mutationDto.getTumors() != null && !mutationDto.getTumors().isEmpty()) {
                System.out.println(spaceStrByNestLevel(nestLevel) + "Tumor Types");
                for (GeneImportDto.TumorDto tumorDto : mutationDto.getTumors()) {
                    List<TumorType> tumorTypes = getTumorTypesFromDto(tumorDto.getCancerTypes());
                    List<TumorType> excludedCancerTypes = getTumorTypesFromDto(tumorDto.getExcludedCancerTypes());
                    List<TumorType> relevantCancerTypes = getRelevantCancerTypesIfExistsFromDto(tumorDto, tumorTypes,
                            excludedCancerTypes, null);

                    convertCancerToDomainObjects(gene, alterations, tumorDto, tumorTypes, excludedCancerTypes,
                            relevantCancerTypes, result, nestLevel + 1);
                }
            }
        } else {
            System.out.println(spaceStrByNestLevel(nestLevel) + "Mutation does not have name skip...");
        }
    }

    private void convertVusToDomainObjects(Gene gene, List<GeneImportDto.VusDto> vusList,
            GeneImportResult result, Integer nestLevel) throws JSONException {
        System.out.println(spaceStrByNestLevel(nestLevel) + "Variants of unknown significance");
        if (vusList != null && !vusList.isEmpty()) {
            AlterationType type = AlterationType.MUTATION; // TODO: cna and fusion

            System.out.println("\t" + vusList.size() + " VUSs");
            for (int i = 0; i < vusList.size(); i++) {
                GeneImportDto.VusDto vusDto = vusList.get(i);
                String mutationStr = vusDto.getName();
                Long lastEdit = vusDto.getTime() != null ? vusDto.getTime().getValue() : null;

                if (mutationStr != null) {
                    List<Alteration> mutations = AlterationUtils.parseMutationString(mutationStr, ",");
                    Set<Alteration> alterations = new HashSet<>();
                    for (Alteration mutation : mutations) {
                        Alteration alteration = findOrCreateAlteration(gene, type, mutation, result);
                        alterations.add(alteration);
                    }

                    Evidence evidence = new Evidence();
                    evidence.setEvidenceType(EvidenceType.VUS);
                    evidence.setGene(gene);
                    evidence.setAlterations(alterations);
                    if (lastEdit != null) {
                        Date date = new Date(lastEdit);
                        evidence.setLastEdit(date);
                    }
                    if (evidence.getLastEdit() == null) {
                        System.out.println(spaceStrByNestLevel(nestLevel + 1) + "WARNING: " + mutationStr
                                + " do not have last update.");
                    }
                    result.getEvidencesToSave().add(evidence);
                }
                if (i % 10 == 9)
                    System.out.println("\t\tImported " + (i + 1));
            }
        } else {
            System.out.println(spaceStrByNestLevel(nestLevel) + "No VUS available.");
        }
    }

    private Alteration findOrCreateAlteration(Gene gene, AlterationType type, Alteration mutation,
            GeneImportResult result) {
        AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
        Alteration alteration = alterationBo.findAlteration(gene, type, mutation.getAlteration());

        if (alteration == null) {
            alteration = new Alteration();
            alteration.setGene(gene);
            alteration.setAlterationType(type);
            alteration.setAlteration(mutation.getAlteration());
            alteration.setName(mutation.getName());
            alteration.setReferenceGenomes(mutation.getReferenceGenomes());
            AlterationUtils.annotateAlteration(alteration, mutation.getAlteration());
            result.getAlterationsToSave().add(alteration);
        } else if (!alteration.getReferenceGenomes().equals(mutation.getReferenceGenomes())) {
            alteration.setReferenceGenomes(mutation.getReferenceGenomes());
            result.getAlterationsToSave().add(alteration);
        }

        return alteration;
    }

    private void convertOncogenicToDomainObjects(Gene gene, Alteration alteration, Oncogenicity oncogenic,
            String uuid, Date lastEdit, GeneImportResult result) {
        if (alteration != null && gene != null && oncogenic != null) {
            EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
            List<Evidence> evidences = evidenceBo.findEvidencesByAlteration(Collections.singleton(alteration),
                    Collections.singleton(EvidenceType.ONCOGENIC));
            if (evidences.isEmpty()) {
                Evidence evidence = new Evidence();
                evidence.setGene(gene);
                evidence.setAlterations(Collections.singleton(alteration));
                evidence.setEvidenceType(EvidenceType.ONCOGENIC);
                evidence.setKnownEffect(oncogenic.getOncogenic());
                evidence.setUuid(uuid);
                evidence.setLastEdit(lastEdit);
                result.getEvidencesToSave().add(evidence);
            } else if (Oncogenicity.compare(oncogenic, Oncogenicity.getByEvidence(evidences.get(0))) > 0) {
                evidences.get(0).setKnownEffect(oncogenic.getOncogenic());
                evidences.get(0).setLastEdit(lastEdit);
                result.getEvidencesToSave().add(evidences.get(0));
            }
        }
    }

    private void convertCancerToDomainObjects(Gene gene, Set<Alteration> alterations, GeneImportDto.TumorDto tumorDto,
            List<TumorType> tumorTypes, List<TumorType> excludedCancerTypes, List<TumorType> relevantCancerTypes,
            GeneImportResult result, Integer nestLevel) throws Exception {
        if (tumorTypes.isEmpty()) {
            return;
        }

        System.out.println(spaceStrByNestLevel(nestLevel) + "Tumor types: "
                + tumorTypes.stream().map(TumorTypeUtils::getTumorTypeName).collect(Collectors.joining(", ")));

        // Convert cancer type summary
        convertTumorLevelSummariesToDomainObjects(tumorDto, "summary", gene, alterations, tumorTypes,
                excludedCancerTypes, relevantCancerTypes, EvidenceType.TUMOR_TYPE_SUMMARY, result, nestLevel);

        // Convert prognostic implications
        Evidence prognosticEvidence = convertImplicationToDomainObjects(gene, alterations, tumorTypes,
                excludedCancerTypes, relevantCancerTypes, tumorDto.getPrognostic(),
                tumorDto.getPrognostic() != null ? tumorDto.getPrognostic().getUuid() : null,
                EvidenceType.PROGNOSTIC_IMPLICATION, result, nestLevel + 1);

        // Convert diagnostic implications
        Evidence diagnosticEvidence = convertImplicationToDomainObjects(gene, alterations, tumorTypes,
                excludedCancerTypes, relevantCancerTypes, tumorDto.getDiagnostic(),
                tumorDto.getDiagnostic() != null ? tumorDto.getDiagnostic().getUuid() : null,
                EvidenceType.DIAGNOSTIC_IMPLICATION, result, nestLevel + 1);

        // Convert diagnostic summary
        List<TumorType> diagnosticRCT = getRelevantCancerTypesIfExistsFromDto(tumorDto, tumorTypes, excludedCancerTypes,
                diagnosticEvidence == null ? null : diagnosticEvidence.getLevelOfEvidence());
        convertDxPxSummariesToDomainObjects(tumorDto, "diagnosticSummary", gene, alterations, tumorTypes,
                excludedCancerTypes, tumorDto.getDiagnostic() != null ? diagnosticRCT : relevantCancerTypes,
                EvidenceType.DIAGNOSTIC_SUMMARY, result, nestLevel,
                diagnosticEvidence == null ? null : diagnosticEvidence.getLevelOfEvidence());

        // Convert prognostic summary
        List<TumorType> prognosticRCT = getRelevantCancerTypesIfExistsFromDto(tumorDto, tumorTypes, excludedCancerTypes,
                prognosticEvidence == null ? null : prognosticEvidence.getLevelOfEvidence());
        convertDxPxSummariesToDomainObjects(tumorDto, "prognosticSummary", gene, alterations, tumorTypes,
                excludedCancerTypes, tumorDto.getPrognostic() != null ? prognosticRCT : relevantCancerTypes,
                EvidenceType.PROGNOSTIC_SUMMARY, result, nestLevel,
                prognosticEvidence == null ? null : prognosticEvidence.getLevelOfEvidence());

        // Convert therapeutic implications
        if (tumorDto.getTherapeuticImplications() != null) {
            for (GeneImportDto.TherapeuticImplicationDto implicationDto : tumorDto.getTherapeuticImplications()) {
                if ((implicationDto.getDescription() != null && !implicationDto.getDescription().trim().isEmpty()) ||
                        (implicationDto.getTreatments() != null && !implicationDto.getTreatments().isEmpty())) {
                    convertTherapeuticImplicationsToDomainObjects(gene, alterations, tumorTypes, excludedCancerTypes,
                            relevantCancerTypes, implicationDto, result, nestLevel + 1);
                }
            }
        }
    }

    private void convertTumorLevelSummariesToDomainObjects(GeneImportDto.TumorDto tumorDto, String summaryKey,
            Gene gene, Set<Alteration> alterations, List<TumorType> tumorTypes, List<TumorType> excludedCancerTypes,
            List<TumorType> relevantCancerTypes, EvidenceType evidenceType, GeneImportResult result,
            Integer nestLevel) {
        String summary = null;
        String uuid = null;
        Date lastEdit = null;

        if ("summary".equals(summaryKey)) {
            summary = tumorDto.getSummary();
            uuid = tumorDto.getSummaryUuid();
            lastEdit = tumorDto.getSummaryLastEdit();
        }

        if (summary != null && !summary.isEmpty()) {
            System.out.println(spaceStrByNestLevel(nestLevel + 1) + " " + summaryKey);
            Evidence evidence = new Evidence();
            evidence.setEvidenceType(evidenceType);
            evidence.setGene(gene);
            evidence.setDescription(summary);
            evidence.setUuid(uuid);
            evidence.setAlterations(alterations);
            evidence.setLastEdit(lastEdit);
            if (excludedCancerTypes != null && !excludedCancerTypes.isEmpty()) {
                evidence.setExcludedCancerTypes(new HashSet<>(excludedCancerTypes));
            }
            if (relevantCancerTypes != null && !relevantCancerTypes.isEmpty()) {
                evidence.setRelevantCancerTypes(new HashSet<>(relevantCancerTypes));
            }
            if (lastEdit != null) {
                System.out.println(spaceStrByNestLevel(nestLevel + 2) +
                        "Last update on: " + MainUtils.getTimeByDate(lastEdit));
            }
            if (!tumorTypes.isEmpty()) {
                evidence.setCancerTypes(new HashSet<>(tumorTypes));
            }
            convertDocumentsToDomainObjects(summary, evidence, result);
            System.out.println(spaceStrByNestLevel(nestLevel + 2) + "Has description.");
            result.getEvidencesToSave().add(evidence);
        }
    }

    private void convertDxPxSummariesToDomainObjects(GeneImportDto.TumorDto tumorDto, String summaryKey,
            Gene gene, Set<Alteration> alterations, List<TumorType> tumorTypes, List<TumorType> excludedCancerTypes,
            List<TumorType> relevantCancerTypes, EvidenceType evidenceType, GeneImportResult result,
            Integer nestLevel, LevelOfEvidence level) {
        List<TumorType> rcts = new ArrayList<>(relevantCancerTypes);
        if ((rcts == null || rcts.size() == 0) && LevelOfEvidence.LEVEL_Dx1.equals(level)) {
            rcts.addAll(TumorTypeUtils.getDxOneRelevantCancerTypes(new HashSet<>(tumorTypes)));
        }
        convertTumorLevelSummariesToDomainObjects(tumorDto, summaryKey, gene, alterations, tumorTypes,
                excludedCancerTypes, rcts, evidenceType, result, nestLevel);
    }

    private Evidence convertImplicationToDomainObjects(Gene gene, Set<Alteration> alterations,
            List<TumorType> tumorTypes,
            List<TumorType> excludedCancerTypes, List<TumorType> relevantCancerTypes, Object implicationDto,
            String uuid, EvidenceType evidenceType, GeneImportResult result, Integer nestLevel) throws Exception {
        if (evidenceType != null && implicationDto != null) {
            String description = null;
            String level = null;
            Date descriptionLastEdit = null;
            Date levelLastEdit = null;

            if (implicationDto instanceof GeneImportDto.PrognosticDto) {
                GeneImportDto.PrognosticDto prognosticDto = (GeneImportDto.PrognosticDto) implicationDto;
                description = prognosticDto.getDescription();
                level = prognosticDto.getLevel();
                descriptionLastEdit = prognosticDto.getDescriptionLastEdit();
                levelLastEdit = prognosticDto.getLevelLastEdit();
            } else if (implicationDto instanceof GeneImportDto.DiagnosticDto) {
                GeneImportDto.DiagnosticDto diagnosticDto = (GeneImportDto.DiagnosticDto) implicationDto;
                description = diagnosticDto.getDescription();
                level = diagnosticDto.getLevel();
                descriptionLastEdit = diagnosticDto.getDescriptionLastEdit();
                levelLastEdit = diagnosticDto.getLevelLastEdit();
            }

            if ((description != null && !description.trim().isEmpty()) || (level != null && !level.trim().isEmpty())) {
                System.out.println(spaceStrByNestLevel(nestLevel) + evidenceType.name() + ":");
                Set<Date> lastEditDates = new HashSet<>();
                Evidence evidence = new Evidence();

                evidence.setEvidenceType(evidenceType);
                evidence.setAlterations(alterations);
                evidence.setGene(gene);
                evidence.setUuid(uuid);
                evidence.setCancerTypes(new HashSet<>(tumorTypes));

                if (excludedCancerTypes != null) {
                    evidence.setExcludedCancerTypes(new HashSet<>(excludedCancerTypes));
                }

                if (level != null && !level.trim().isEmpty()) {
                    LevelOfEvidence levelOfEvidence = LevelOfEvidence.getByLevel(level.trim());
                    System.out.println(
                            spaceStrByNestLevel(nestLevel + 1) + "Level of the implication: " + levelOfEvidence);
                    evidence.setLevelOfEvidence(levelOfEvidence);
                    addDateToSet(lastEditDates, levelLastEdit);
                }

                List<TumorType> implicationRCT = getRelevantCancerTypesIfExistsFromDto(null, tumorTypes,
                        excludedCancerTypes, evidence.getLevelOfEvidence());
                if (implicationRCT.size() > 0) {
                    evidence.setRelevantCancerTypes(new HashSet<>(implicationRCT));
                } else if (relevantCancerTypes != null && relevantCancerTypes.size() > 0) {
                    evidence.setRelevantCancerTypes(new HashSet<>(relevantCancerTypes));
                } else if (LevelOfEvidence.LEVEL_Dx1.equals(evidence.getLevelOfEvidence())) {
                    evidence.setRelevantCancerTypes(
                            TumorTypeUtils.getDxOneRelevantCancerTypes(new HashSet<>(tumorTypes)));
                }

                if (description != null && !description.trim().isEmpty()) {
                    System.out.println(spaceStrByNestLevel(nestLevel + 1) + "Has description.");
                    evidence.setDescription(description);
                    addDateToSet(lastEditDates, descriptionLastEdit);
                    convertDocumentsToDomainObjects(description, evidence, result);
                }

                Date lastEdit = getMostRecentDate(lastEditDates);
                evidence.setLastEdit(lastEdit);
                if (lastEdit != null) {
                    System.out.println(spaceStrByNestLevel(nestLevel + 1) +
                            "Last update on: " + MainUtils.getTimeByDate(lastEdit));
                }
                result.getEvidencesToSave().add(evidence);
                return evidence;
            }
        }
        return null;
    }

    private void convertTherapeuticImplicationsToDomainObjects(Gene gene, Set<Alteration> alterations,
            List<TumorType> tumorTypes, List<TumorType> excludedCancerTypes, List<TumorType> relevantCancerTypes,
            GeneImportDto.TherapeuticImplicationDto implicationDto, GeneImportResult result, Integer nestLevel)
            throws Exception {
        DrugBo drugBo = ApplicationContextSingleton.getDrugBo();
        List<GeneImportDto.TreatmentDto> treatmentsList = implicationDto.getTreatments() != null
                ? implicationDto.getTreatments()
                : new ArrayList<>();
        int priorityCount = 1;

        for (GeneImportDto.TreatmentDto treatmentDto : treatmentsList) {
            if (treatmentDto.getName() == null || treatmentDto.getName().isEmpty()) {
                System.out.println(spaceStrByNestLevel(nestLevel + 1) + "Drug does not have name, skip...");
                continue;
            }

            System.out.println(spaceStrByNestLevel(nestLevel + 1) + "Drug(s): " + treatmentDto.getName().size());

            Set<Date> lastEditDates = new HashSet<>();
            addDateToSet(lastEditDates, treatmentDto.getNameLastEdit());

            ImmutablePair<EvidenceType, String> evidenceTypeAndKnownEffect = getEvidenceTypeAndKnownEffectFromTreatmentDto(
                    treatmentDto);
            EvidenceType evidenceType = evidenceTypeAndKnownEffect.getLeft();
            String knownEffect = evidenceTypeAndKnownEffect.getRight();
            if (evidenceType == null) {
                System.err.println(spaceStrByNestLevel(nestLevel + 1) + "Could not get evidence type");
                continue;
            }
            if (knownEffect == null) {
                System.err.println(spaceStrByNestLevel(nestLevel + 1) + "Could not get known effect");
            }

            Evidence evidence = new Evidence();
            evidence.setEvidenceType(evidenceType);
            evidence.setAlterations(alterations);
            evidence.setGene(gene);
            evidence.setCancerTypes(new HashSet<>(tumorTypes));
            evidence.setKnownEffect(knownEffect);
            evidence.setUuid(treatmentDto.getUuid());

            // Convert approved indications
            Set<String> approvedIndications = new HashSet<>();
            if (treatmentDto.getIndication() != null && !treatmentDto.getIndication().trim().isEmpty()) {
                approvedIndications = new HashSet<>(Arrays.asList(treatmentDto.getIndication().split(";")));
                addDateToSet(lastEditDates, treatmentDto.getIndicationLastEdit());
            }

            List<Treatment> treatments = new ArrayList<>();
            for (List<GeneImportDto.DrugDto> drugsList : treatmentDto.getName()) {
                List<Drug> drugs = new ArrayList<>();
                for (GeneImportDto.DrugDto drugDto : drugsList) {
                    String ncitCode = drugDto.getNcitCode();
                    if (ncitCode != null && ncitCode.isEmpty()) {
                        ncitCode = null;
                    }
                    String drugName = drugDto.getDrugName();
                    if (drugName != null && drugName.isEmpty()) {
                        drugName = null;
                    }
                    String drugUuid = drugDto.getUuid();
                    Drug drug = null;
                    if (ncitCode != null) {
                        drug = drugBo.findDrugsByNcitCode(ncitCode);
                    }
                    if (drug == null && drugName != null) {
                        drug = drugBo.findDrugByName(drugName);
                    }
                    if (drug == null) {
                        if (ncitCode != null) {
                            org.oncokb.oncokb_transcript.client.Drug ncitDrug = oncokbTranscriptService
                                    .findDrugByNcitCode(ncitCode);
                            if (ncitDrug == null) {
                                System.out.println("ERROR: the NCIT code cannot be found... Code:" + ncitCode);
                            } else {
                                drug = new Drug();
                                drug.setDrugName(ncitDrug.getName());
                                drug.setSynonyms(ncitDrug.getSynonyms().stream().map(synonym -> synonym.getName())
                                        .collect(Collectors.toSet()));
                                drug.setNcitCode(ncitDrug.getCode());

                                if (drugName != null) {
                                    DrugUtils.updateDrugName(drug, drugName);
                                }
                            }
                        }
                        if (drug == null) {
                            drug = new Drug();
                            drug.setNcitCode(ncitCode);
                            drug.setDrugName(drugName);
                        }
                        if (drugUuid != null) {
                            drug.setUuid(drugUuid);
                        }
                        result.getDrugsToSave().add(drug);
                    }
                    drugs.add(drug);
                }

                Treatment treatment = new Treatment();
                treatment.setDrugs(drugs);
                treatment.setPriority(priorityCount);
                treatment.setApprovedIndications(approvedIndications);
                treatment.setEvidence(evidence);

                treatments.add(treatment);
                priorityCount++;
            }
            evidence.setTreatments(treatments);

            // Convert level of evidence
            if (treatmentDto.getLevel() == null || treatmentDto.getLevel().trim().isEmpty()) {
                System.err.println(spaceStrByNestLevel(nestLevel + 2) + "Error: no level of evidence");
                continue;
            } else {
                String level = treatmentDto.getLevel().trim();
                addDateToSet(lastEditDates, treatmentDto.getLevelLastEdit());

                LevelOfEvidence levelOfEvidence = LevelOfEvidence.getByLevel(level.toUpperCase());
                if (levelOfEvidence == null) {
                    System.err.println(spaceStrByNestLevel(nestLevel + 2) + "Error: wrong level of evidence: " + level);
                    continue;
                } else if (LevelUtils.getAllowedCurationLevels().contains(levelOfEvidence)) {
                    System.out.println(spaceStrByNestLevel(nestLevel + 2) +
                            "Level: " + levelOfEvidence.getLevel());
                } else {
                    System.err.println(spaceStrByNestLevel(nestLevel + 2) +
                            "Level not allowed: " + levelOfEvidence.getLevel());
                    continue;
                }
                evidence.setLevelOfEvidence(levelOfEvidence);

                LevelOfEvidence fdaLevel;
                if (treatmentDto.getFdaLevel() != null) {
                    String fdaLevelStr = treatmentDto.getFdaLevel();
                    fdaLevel = LevelOfEvidence.getByLevel(fdaLevelStr);
                    System.out.println(spaceStrByNestLevel(nestLevel + 2) + "Manual FDA level: " + fdaLevel);
                } else {
                    fdaLevel = FdaAlterationUtils.convertToFdaLevel(evidence.getLevelOfEvidence());
                    if (fdaLevel != null) {
                        System.out.println(spaceStrByNestLevel(nestLevel + 2) + "Default FDA level: " + fdaLevel);
                    }
                }
                if (fdaLevel != null && LevelUtils.getAllowedFdaLevels().contains(fdaLevel)) {
                    evidence.setFdaLevel(fdaLevel);
                }

                if (treatmentDto.getSolidPropagation() != null) {
                    String definedPropagation = treatmentDto.getSolidPropagation();
                    LevelOfEvidence definedLevel = LevelOfEvidence.getByLevel(definedPropagation.toUpperCase());

                    if (definedLevel != null && LevelUtils.getAllowedPropagationLevels().contains(definedLevel)) {
                        evidence.setSolidPropagationLevel(definedLevel);
                    }
                    if (evidence.getSolidPropagationLevel() != null) {
                        System.out.println(spaceStrByNestLevel(nestLevel + 2) +
                                "Manual solid propagation level: " + evidence.getSolidPropagationLevel());
                    }
                } else {
                    evidence.setSolidPropagationLevel(
                            LevelUtils.getDefaultPropagationLevelByTumorForm(evidence, TumorForm.SOLID));
                }

                if (treatmentDto.getLiquidPropagation() != null) {
                    String definedPropagation = treatmentDto.getLiquidPropagation();
                    LevelOfEvidence definedLevel = LevelOfEvidence.getByLevel(definedPropagation.toUpperCase());

                    if (definedLevel != null && LevelUtils.getAllowedPropagationLevels().contains(definedLevel)) {
                        evidence.setLiquidPropagationLevel(definedLevel);
                    }
                    if (evidence.getLiquidPropagationLevel() != null) {
                        System.out.println(spaceStrByNestLevel(nestLevel + 2) +
                                "Manual liquid propagation level: " + evidence.getLiquidPropagationLevel());
                    }
                } else {
                    evidence.setLiquidPropagationLevel(
                            LevelUtils.getDefaultPropagationLevelByTumorForm(evidence, TumorForm.LIQUID));
                }
            }

            // Convert description
            if (treatmentDto.getDescription() != null && !treatmentDto.getDescription().trim().isEmpty()) {
                String desc = treatmentDto.getDescription().trim();
                addDateToSet(lastEditDates, treatmentDto.getDescriptionLastEdit());
                evidence.setDescription(desc);
                System.out.println(spaceStrByNestLevel(nestLevel + 2) + "Has description.");
                convertDocumentsToDomainObjects(desc, evidence, result);
            }

            Date lastEdit = getMostRecentDate(lastEditDates);
            if (lastEdit != null) {
                System.out.println(spaceStrByNestLevel(nestLevel + 2) +
                        "Last update on: " + MainUtils.getTimeByDate(lastEdit));
            }
            evidence.setLastEdit(lastEdit);

            if (excludedCancerTypes != null) {
                evidence.setExcludedCancerTypes(new HashSet<>(excludedCancerTypes));
            }

            List<TumorType> drugRCT = getRelevantCancerTypesIfExistsFromDto(null, tumorTypes, excludedCancerTypes,
                    evidence.getLevelOfEvidence());
            if (drugRCT.size() > 0) {
                evidence.setRelevantCancerTypes(new HashSet<>(drugRCT));
            } else if (relevantCancerTypes != null) {
                evidence.setRelevantCancerTypes(new HashSet<>(relevantCancerTypes));
            }

            result.getEvidencesToSave().add(evidence);
        }
    }

    private void convertDocumentsToDomainObjects(String str, Evidence evidence, GeneImportResult result) {
        if (str == null)
            return;
        Set<Article> docs = new HashSet<>();
        ArticleBo articleBo = ApplicationContextSingleton.getArticleBo();

        Set<String> pmidToSearch = new HashSet<>();
        getPmidsFromText(evidence.getDescription()).forEach(pmid -> {
            Article doc = articleBo.findArticleByPmid(pmid);
            if (doc != null) {
                docs.add(doc);
            } else {
                pmidToSearch.add(pmid);
            }
        });

        if (!pmidToSearch.isEmpty()) {
            for (Article article : NcbiEUtils.readPubmedArticles(pmidToSearch)) {
                result.getArticlesToSave().add(article);
                docs.add(article);
            }
        }

        getAbstractFromText(evidence.getDescription()).stream().forEach(article -> {
            Article dbArticle = articleBo.findArticleByAbstract(article.getAbstractContent());
            if (dbArticle == null) {
                result.getArticlesToSave().add(article);
                docs.add(article);
            } else {
                docs.add(dbArticle);
            }
        });

        evidence.addArticles(docs);
    }

    /**
     * Container class to hold all domain objects that need to be saved
     */
    private static class GeneImportResult {
        private Gene gene;
        private GeneImportDto geneImportDto;
        private List<GeneImportDto.VusDto> vusList;
        private List<Alteration> alterationsToSave = new ArrayList<>();
        private List<Evidence> evidencesToSave = new ArrayList<>();
        private List<Drug> drugsToSave = new ArrayList<>();
        private List<Article> articlesToSave = new ArrayList<>();

        public Gene getGene() {
            return gene;
        }

        public void setGene(Gene gene) {
            this.gene = gene;
        }

        public GeneImportDto getGeneImportDto() {
            return geneImportDto;
        }

        public void setGeneImportDto(GeneImportDto geneImportDto) {
            this.geneImportDto = geneImportDto;
        }

        public List<GeneImportDto.VusDto> getVusList() {
            return vusList;
        }

        public void setVusList(List<GeneImportDto.VusDto> vusList) {
            this.vusList = vusList;
        }

        public List<Alteration> getAlterationsToSave() {
            return alterationsToSave;
        }

        public List<Evidence> getEvidencesToSave() {
            return evidencesToSave;
        }

        public List<Drug> getDrugsToSave() {
            return drugsToSave;
        }

        public List<Article> getArticlesToSave() {
            return articlesToSave;
        }
    }

    private void updateGeneInfoFromDto(GeneImportDto geneImportDto, Gene gene) {
        GeneImportDto.GeneTypeDto geneType = geneImportDto.getType();
        String oncogene = geneType != null ? geneType.getOcg() : null;
        String tsg = geneType != null ? geneType.getTsg() : null;

        if (oncogene != null) {
            if (oncogene.equals("Oncogene")) {
                gene.setOncogene(true);
            } else {
                gene.setOncogene(false);
            }
        }
        if (tsg != null) {
            if (tsg.equals("Tumor Suppressor")) {
                gene.setTSG(true);
            } else {
                gene.setTSG(false);
            }
        }

        String grch37Isoform = geneImportDto.getIsoformOverride();
        String grch37RefSeq = geneImportDto.getDmpRefseqId();
        String grch38Isoform = geneImportDto.getIsoformOverrideGrch38();
        String grch38RefSeq = geneImportDto.getDmpRefseqIdGrch38();

        if (grch37Isoform != null) {
            gene.setGrch37Isoform(grch37Isoform);
        }
        if (grch37RefSeq != null) {
            gene.setGrch37RefSeq(grch37RefSeq);
        }
        if (grch38Isoform != null) {
            gene.setGrch38Isoform(grch38Isoform);
        }
        if (grch38RefSeq != null) {
            gene.setGrch38RefSeq(grch38RefSeq);
        }
    }

    private Oncogenicity getOncogenicityFromDto(GeneImportDto.MutationEffectDto mutationEffect) {
        Oncogenicity oncogenic = null;
        if (mutationEffect != null && mutationEffect.getOncogenic() != null
                && !mutationEffect.getOncogenic().isEmpty()) {
            oncogenic = getOncogenicityByString(mutationEffect.getOncogenic());
        }
        return oncogenic;
    }

    private List<TumorType> getTumorTypesFromDto(List<GeneImportDto.TumorTypeDto> tumorTypeDtos) throws Exception {
        if (tumorTypeDtos == null) {
            return new ArrayList<>();
        }

        List<TumorType> tumorTypes = new ArrayList<>();
        for (GeneImportDto.TumorTypeDto ttDto : tumorTypeDtos) {
            String code = ttDto.getCode();
            String mainType = ttDto.getMainType();
            if (code != null) {
                TumorType matchedTumorType = ApplicationContextSingleton.getTumorTypeBo().getByCode(code);
                if (matchedTumorType == null) {
                    throw new Exception("The tumor type code does not exist: " + code);
                } else {
                    tumorTypes.add(matchedTumorType);
                }
            } else if (mainType != null) {
                TumorType matchedTumorType = ApplicationContextSingleton.getTumorTypeBo().getByMainType(mainType);
                if (matchedTumorType == null) {
                    throw new Exception("The tumor main type does not exist: " + mainType);
                } else {
                    tumorTypes.add(matchedTumorType);
                }
            } else {
                throw new Exception("The tumor type does not exist. Maintype: " + mainType + ". Subtype: " + code);
            }
        }
        return tumorTypes;
    }

    private List<TumorType> getRelevantCancerTypesIfExistsFromDto(GeneImportDto.TumorDto tumorDto,
            List<TumorType> tumorTypes, List<TumorType> excludedCancerTypes, LevelOfEvidence level)
            throws JSONException, Exception {
        List<TumorType> relevantCancerTypes = new ArrayList<>();
        if (tumorDto.getPrognostic() != null && tumorDto.getPrognostic().getExcludedRelevantCancerTypes() != null) {
            List<TumorType> excludedRCT = getTumorTypesFromDto(
                    tumorDto.getPrognostic().getExcludedRelevantCancerTypes());
            relevantCancerTypes = getRelevantCancerTypes(tumorTypes, excludedCancerTypes, level, excludedRCT);
        }
        return relevantCancerTypes;
    }

    private List<TumorType> getRelevantCancerTypes(List<TumorType> tumorTypes, List<TumorType> excludedTumorTypes,
            LevelOfEvidence level, List<TumorType> excludedRelevantCancerTypes) {
        RelevantTumorTypeDirection direction = level != null && LevelOfEvidence.LEVEL_Dx1.equals(level)
                ? RelevantTumorTypeDirection.UPWARD
                : RelevantTumorTypeDirection.DOWNWARD;

        Set<TumorType> queriedTumorTypes = tumorTypes.stream().map(tt -> {
            return TumorTypeUtils.findRelevantTumorTypes(TumorTypeUtils.getTumorTypeName(tt),
                    StringUtils.isEmpty(tt.getSubtype()), direction, false);
        })
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        Set<TumorType> queriedExcludedTumorTypes = excludedTumorTypes.stream().map(ett -> {
            return TumorTypeUtils.findRelevantTumorTypes(TumorTypeUtils.getTumorTypeName(ett),
                    StringUtils.isEmpty(ett.getSubtype()), direction, false);
        })
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        queriedTumorTypes.removeAll(queriedExcludedTumorTypes);
        queriedTumorTypes.removeAll(excludedRelevantCancerTypes);

        return new ArrayList<>(queriedTumorTypes);
    }

    private Date getUpdateTime(Object obj) throws JSONException {
        if (obj == null)
            return null;
        JSONObject reviewObj = new JSONObject(obj.toString());
        if (reviewObj.has("updateTime") && StringUtils.isNumeric(reviewObj.get("updateTime").toString())) {
            return new Date(reviewObj.getLong("updateTime"));
        }
        return null;
    }

    protected Oncogenicity getOncogenicityByString(String oncogenicStr) {
        Oncogenicity oncogenic = null;
        if (oncogenicStr != null) {
            oncogenicStr = oncogenicStr.toLowerCase();
            switch (oncogenicStr) {
                case "yes":
                    oncogenic = Oncogenicity.YES;
                    break;
                case "likely":
                    oncogenic = Oncogenicity.LIKELY;
                    break;
                case "likely neutral":
                    oncogenic = Oncogenicity.LIKELY_NEUTRAL;
                    break;
                case "resistance":
                    oncogenic = Oncogenicity.RESISTANCE;
                    break;
                case "inconclusive":
                    oncogenic = Oncogenicity.INCONCLUSIVE;
                    break;
                default:
                    break;
            }
        }
        return oncogenic;
    }

    private String spaceStrByNestLevel(Integer nestLevel) {
        if (nestLevel == null || nestLevel < 1)
            nestLevel = 1;
        return StringUtils.repeat("    ", nestLevel - 1);
    }

    private void addDateToSet(Set<Date> set, Date date) {
        if (date != null) {
            set.add(date);
        }
    }

    private Date getMostRecentDate(Set<Date> dates) {
        if (dates == null || dates.size() == 0)
            return null;
        return Collections.max(dates);
    }

    private ImmutablePair<EvidenceType, String> getEvidenceTypeAndKnownEffectFromTreatmentDto(
            GeneImportDto.TreatmentDto treatmentDto) {
        ImmutablePair<EvidenceType, String> emptyPair = new ImmutablePair<EvidenceType, String>(null, null);
        if (treatmentDto.getLevel() == null || treatmentDto.getLevel().trim().isEmpty()) {
            return emptyPair;
        }
        String level = treatmentDto.getLevel().trim();
        LevelOfEvidence levelOfEvidence = LevelOfEvidence.getByLevel(level.toUpperCase());

        EvidenceType evidenceType = EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY;
        String type = "";
        if (LevelOfEvidence.LEVEL_1.equals(levelOfEvidence) || LevelOfEvidence.LEVEL_2.equals(levelOfEvidence)) {
            evidenceType = EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY;
            type = "Sensitive";
        } else if (LevelOfEvidence.LEVEL_R1.equals(levelOfEvidence)) {
            evidenceType = EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE;
            type = "Resistant";
        } else if (LevelOfEvidence.LEVEL_3A.equals(levelOfEvidence)
                || LevelOfEvidence.LEVEL_4.equals(levelOfEvidence)) {
            evidenceType = EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY;
            type = "Sensitive";
        } else if (LevelOfEvidence.LEVEL_R2.equals(levelOfEvidence)) {
            evidenceType = EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE;
            type = "Resistant";
        } else {
            return emptyPair;
        }

        return new ImmutablePair<EvidenceType, String>(evidenceType, type);
    }
}
