/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo;

import java.util.List;

/**
 *
 * @author jgao
 * @param <T>
 */
public interface GenericBo<T> {

    void save(T t);

    void update(T t);

    void saveOrUpdate(T t);

    void delete(T t);

    void deleteAll(List<T> ts);

    List<T> findAll();

    Integer countAll();

    /**
     * Save all entities in a batch operation
     * 
     * @param entities list of entities to save
     */
    void saveAll(List<T> entities);
}
