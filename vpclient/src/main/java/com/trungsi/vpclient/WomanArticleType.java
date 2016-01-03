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
public class WomanArticleType extends ArticleType {

	@Override
	List<String> getPreferredSizes(String articleInfo, Context context) {
		if (isJean(articleInfo)) {
		    return getWomanJeanSizes(context);
		} else if (isShoes(articleInfo)) {
		    return getWomanShoesSizes(context) ;
		} else if (isSoutienGorge(articleInfo)) {
		    return getWomanLingerieSizes(context);
		} else if (isShirt(articleInfo)) {
		    return getWomanShirtSizes(context);
		} else {
		    return getWomanClothingSizes(context);
		}
	}
	
	List<String> getWomanJeanSizes(Context context) {
		return list(getDefault(Context.WOMAN_JEAN_SIZES, context, " 26 |W26|T.36|T. 36").split("\\|"));
	}
	
	List<String> getWomanShoesSizes(Context context) {
		return list(getDefault(Context.WOMAN_SHOES_SIZES, context, " 37 |T.37").split("\\|"));
	}
	
	List<String> getWomanLingerieSizes(Context context) {
		return list(getDefault(Context.WOMAN_LINGERIE_SIZES, context, "90A").split("\\|"));
	}
	
	List<String> getWomanClothingSizes(Context context) {
		return list(getDefault(Context.WOMAN_CLOTHING_SIZES, context, " 36 |T.36 (FR)|T.36 |T. 36|34/36| S |.S ").split("\\|"));
	}
	
	List<String> getWomanShirtSizes(Context context) {
		return list(getDefault(Context.WOMAN_SHIRT_SIZES, context, "T. 36|T.36| XS ").split("\\|"));
	}

	static List<String> womanSizes = list("T. 25 (US)", "T. 26 (US)", "T. 27 (US)", "T. 34 (FR)", "T. 32/34", "T. 34/36", "T. 36 (FR) - T. XS (US)", "T. 36 (FR)");
	
	@Override
	boolean accept(String articleInfo, List<String> availableSizes) {
		return /*isSaleForWomanOnly() ||*/ articleInfo.contains("farrutx") ||
				(articleInfo.contains("femme") && !articleInfo.contains("homme") ||
                listContains(list(
                                    "soutien", "lingerie",
                                    "chemisier", "escarpin",
                                    "compens", "sandale", " talon",
                                    "cuissard", "pantacourt", "7/8"), articleInfo) ||
                containsSize(availableSizes, womanSizes) ||                   
                (listContains(list("ballerine", "robe", "jupe", "top", "culotte"), articleInfo) 
                		&& !containsSize(availableSizes, list(" ans", " mois")))
                );
	}
}
