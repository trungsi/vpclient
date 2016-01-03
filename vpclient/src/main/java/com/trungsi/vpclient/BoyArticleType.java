/**
 * 
 */
package com.trungsi.vpclient;

import static com.trungsi.vpclient.utils.CollectionUtils.list;

import java.util.List;

/**
 * @author trungsi
 *
 */
public class BoyArticleType extends ArticleType {

	@Override
	List<String> getPreferredSizes(String articleInfo, Context context) {
		if (isShoes(articleInfo)) {
			return getBoyShoesSizes(context);
		} else {
			return getBoyClothingSizes(context);
		}
	}
	
	List<String> getBoyShoesSizes(Context context) {
		return list(getDefault(Context.BOY_SHOES_SIZES, context, " 23 |T.23|T. 23").split("\\|"));
	}
	
	List<String> getBoyClothingSizes(Context context) {
		return list(getDefault(Context.BOY_CLOTHING_SIZES, context, "6 mois|12 mois|3 mois").split("\\|"));
	}

	@Override
	boolean accept(String articleInfo, List<String> sizeValues) {
		return articleInfo.contains("garçon") || 
				containsSize(sizeValues, list("mois", "ans"));
	}
}
