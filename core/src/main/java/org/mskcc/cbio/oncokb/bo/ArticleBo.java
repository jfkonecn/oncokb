package org.mskcc.cbio.oncokb.bo;

import org.mskcc.cbio.oncokb.model.Article;
import java.util.List;
import java.util.Set;

/**
 *
 * @author jgao
 */
public interface ArticleBo extends GenericBo<Article> {

    /**
     * 
     * @param pmid
     * @return
     */
    Article findArticleByPmid(String pmid);

    /**
     * 
     * @param abstractContent
     * @return
     */
    Article findArticleByAbstract(String abstractContent);

    /**
     * Find articles by PMIDs in a batch operation
     * 
     * @param pmids set of PMIDs to search for
     * @return list of found articles
     */
    List<Article> findArticlesByPmids(Set<String> pmids);
}
