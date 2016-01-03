/**
 * 
 */
package com.trungsi.vpclient;

import static com.trungsi.vpclient.utils.CollectionUtils.listContains;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author trungsi
 *
 */
public class SizeHelper {

	static final Logger LOG = Logger.getLogger(SizeHelper.class);
	
	static List<ArticleType> articleTypes = Arrays.asList(new ManArticleType(), new WomanArticleType(), new GirlArticleType(), new BoyArticleType());
	static ArticleType defaultArticleType = new ManArticleType();
	
	
	static boolean selectSize(Article article, Context context, Sizes sizes) {
		List<String> preferredSizes = getPreferredSizes(article, context, sizes);
		
		for (Size size : sizes) {
			if (isPreferredSize(preferredSizes, size)) {
				LOG.debug("article " + article.getInfo() + " has selected size " + size.getValue());
				size.select();
				return true;
			}
		}
		LOG.debug("No size in " + sizes.getSizeValues() + " could be selected for " + article.getInfo());
		return false;
	}
	
	
	private static List<String> getPreferredSizes(Article article,	Context context, Sizes sizes) {
		String articleInfo = article.getInfo().toLowerCase();
		
		ArticleType articleType = determineArticleType(articleInfo, sizes);
		LOG.info("Article type " + articleType + " found for article " + articleInfo);
		return articleType.getPreferredSizes(articleInfo, context);
	}

	private static ArticleType determineArticleType(String articleInfo, Sizes sizes) {
		for (ArticleType articleType : articleTypes) {
			if (articleType.accept(articleInfo, sizes.getSizeValues())) {
				return articleType;
			}
		}
		
		LOG.warn("Cannot determine article type from " + articleInfo + " so use default one");
		return defaultArticleType;
		
	}
	
	private static boolean isPreferredSize(List<String> preferredSizes, Size size) {
		return listContains(preferredSizes, size.getValue());
	}    
    
    /*private static boolean isKidArticle(String articleInfo, Select select) {
        return articleInfo.contains("enfant") || articleInfo.contains("kid") ||
                /*
                if size contains 'ans', it's sure for kids
                 *
                containsSize(select, list(" ans"));
    }*/
	
}
