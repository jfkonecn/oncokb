package org.mskcc.cbio.oncokb.bo.impl;

import org.mskcc.cbio.oncokb.bo.ArticleBo;
import org.mskcc.cbio.oncokb.dao.ArticleDao;
import org.mskcc.cbio.oncokb.model.Article;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author jgao
 */
public class ArticleBoImpl extends GenericBoImpl<Article, ArticleDao> implements ArticleBo {

    @Override
    public Article findArticleByPmid(String pmid) {
        return getDao().findArticleByPmid(pmid);
    }

    @Override
    public Article findArticleByAbstract(String abstractContent) {
        return getDao().findArticleByAbstract(abstractContent);
    }

    @Override
    public List<Article> findArticlesByPmids(Set<String> pmids) {
        List<Article> result = new ArrayList<>();
        if (pmids != null && !pmids.isEmpty()) {
            for (String pmid : pmids) {
                Article article = findArticleByPmid(pmid);
                if (article != null) {
                    result.add(article);
                }
            }
        }
        return result;
    }
}
