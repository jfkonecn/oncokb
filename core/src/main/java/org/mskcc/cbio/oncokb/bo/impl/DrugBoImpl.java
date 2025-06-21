/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo.impl;

import org.mskcc.cbio.oncokb.bo.DrugBo;
import org.mskcc.cbio.oncokb.dao.DrugDao;
import org.mskcc.cbio.oncokb.model.Drug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author jgao
 */
public class DrugBoImpl extends GenericBoImpl<Drug, DrugDao> implements DrugBo {
    @Override
    public Drug findDrugById(Integer id) {
        return getDao().findDrugById(id);
    }

    @Override
    public Drug findDrugByName(String drugName) {
        return getDao().findDrugByName(drugName);
    }

    @Override
    public List<Drug> findDrugsBySynonym(String synonym) {
        return getDao().findDrugBySynonym(synonym);
    }

    @Override
    public List<Drug> guessDrugs(String drugNameOrSynonym) {
        Drug drug = findDrugByName(drugNameOrSynonym);
        if (drug != null) {
            return Collections.singletonList(drug);
        }

        return findDrugsBySynonym(drugNameOrSynonym);
    }

    @Override
    public Drug guessUnambiguousDrug(String drugNameOrSynonym) {
        List<Drug> drugs = guessDrugs(drugNameOrSynonym);
        if (drugs.size() == 1) {
            return drugs.get(0);
        }

        return null;
    }

    @Override
    public Drug findDrugsByNcitCode(String ncit) {
        return getDao().findDrugByNcitCode(ncit);
    }

    @Override
    public List<Drug> findDrugsByNames(Set<String> drugNames) {
        List<Drug> result = new ArrayList<>();
        if (drugNames != null && !drugNames.isEmpty()) {
            for (String drugName : drugNames) {
                Drug drug = findDrugByName(drugName);
                if (drug != null) {
                    result.add(drug);
                }
            }
        }
        return result;
    }

    @Override
    public List<Drug> findDrugsByNcitCodes(Set<String> ncitCodes) {
        List<Drug> result = new ArrayList<>();
        if (ncitCodes != null && !ncitCodes.isEmpty()) {
            for (String ncitCode : ncitCodes) {
                Drug drug = findDrugsByNcitCode(ncitCode);
                if (drug != null) {
                    result.add(drug);
                }
            }
        }
        return result;
    }

    @Override
    public void update(Drug drug) {
        if (drug != null) {
            drug.setDrugName(drug.getDrugName());
        }
        super.update(drug);
    }

    @Override
    public void save(Drug drug) {
        if (drug != null) {
            drug.setDrugName(drug.getDrugName());
        }
        super.save(drug);
    }

    @Override
    public void saveOrUpdate(Drug drug) {
        if (drug != null) {
            drug.setDrugName(drug.getDrugName());
        }
        super.saveOrUpdate(drug);
    }
}
