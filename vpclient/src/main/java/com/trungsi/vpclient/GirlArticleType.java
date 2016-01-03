/**
 * 
 */
package com.trungsi.vpclient;

import static com.trungsi.vpclient.utils.CollectionUtils.list;
import static com.trungsi.vpclient.utils.CollectionUtils.listContains;

import java.util.List;

/**
 * @author trungsi
 *
 */
public class GirlArticleType extends ArticleType {

	@Override
	List<String> getPreferredSizes(String articleInfo, Context context) {
		if (isShoes(articleInfo)) {
			return getGirlShoesSizes(context);
		} else {
			return getGirlClothingSizes(context);
		}
	}

	List<String> getGirlShoesSizes(Context context) {
		return list(getDefault(Context.GIRL_SHOES_SIZES, context, " 23 |T.23|T. 23").split("\\|"));
	}
	
	List<String> getGirlClothingSizes(Context context) {
		return list(getDefault(Context.GIRL_CLOTHING_SIZES, context, "3 ans").split("\\|"));
	}

	@Override
	boolean accept(String articleInfo, List<String> sizeValues) {
		return articleInfo.contains("fille") ||
				(listContains(list("jupe", "robe",
		                "body", "bodies",
		                "collant", "legging"), articleInfo) &&
		                containsSize(sizeValues, list("ans", "mois")));
	}
}
