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
public class ManArticleType extends ArticleType {

	@Override
	List<String> getPreferredSizes(String articleInfo, Context context) {
		if (isJean(articleInfo)) {
		    if (articleInfo.contains("edwin")) {
		        return list("29|30 (US)|W30|T.30 (US)|40 (FR)|T.40|T. 40| 30".split("\\|"));
		    }
		    return getManJeanSizes(context);
		} else if (isShoes(articleInfo)) {
		    return getManShoesSizes(context);
		} else if (isCostume(articleInfo)) {
		    if (articleInfo.contains("windsor")) { // special size for Windsor
		        return list("T. 46 (FR)|Veste T. 46|T. 46|T. 46".split("\\|"));
		    }

		    if (articleInfo.contains("hackett")) {
		    	return list("36 (UK)|38 (UK)|46 (FR)|48 (FR)|36|38|46|48");
		    }
		    
		    if (!(articleInfo.contains("slim") || articleInfo.contains("cintré") || articleInfo.contains("ajusté"))) { // T. 46 for coupe droite
		        return list("Veste T. 46|Pantalon T. 38|T. 46|T. 38".split("\\|"));
		    }

		    // T. 48 for slim or cintrée
		    return getManCostumeSizes(context);
		} else if (isShirt(articleInfo)) { // chemise
		    return getManShirtSizes(context);
		} else if (isPants(articleInfo)) {
		    if (articleInfo.contains("hackett")) {
		        return list("T. 31|31|T. 32|30|40 (FR)|40/42");
		    }
		    return getManPantsSize(context);
		} else {
		    List<String> sizes = getManClothingClothingSizes(context);
		    if(articleInfo.contains("hackett")) {
		        List<String> newSizes = list(" S ", "T. S", "38");
		        newSizes.addAll(sizes);
		        return newSizes;
		    }
		    return sizes;
		}
	}

	List<String> getManJeanSizes(Context context) {
		return list(getDefault(Context.MAN_JEAN_SIZES, context, " 30 |W30|T.30|T.40|T. 40").split("\\|"));
	}
	
	List<String> getManShoesSizes(Context context) {
		return list(getDefault(Context.MAN_SHOES_SIZES, context, "40.5| 41 |T.41|T. 41").split("\\|"));
	}
	
	List<String> getManCostumeSizes(Context context) {
		return list(getDefault(Context.MAN_COSTUME_SIZES, context, " M |.M |T.40|T. 40").split("\\|"));
	}
	
	List<String> getManClothingClothingSizes(Context context) {
		return list(getDefault(Context.MAN_CLOTHING_SIZES, context, " M |.M | 38 | 40 |T.40|T. 40").split("\\|"));
	}
	
	List<String> getManPantsSize(Context context) {
        return list("T. 40 (FR)|T. 30 (US)|T. 40|T. 30".split("\\|"));
    }

	static List<String> manSizes = list("T. 42", "T. 44", "T. 46", "T. 48", "T. 50", "T. 34 (US)");
    
    
	List<String> getManShirtSizes(Context context) {
		return list(getDefault(Context.MAN_SHIRT_SIZES, context, "T. 39|T.39| M ").split("\\|"));
	}

	@Override
	boolean accept(String articleInfo, List<String> availableSizes) {
		return (articleInfo.contains("homme") && !articleInfo.contains("femme")) ||
				listContains(list("calçon", "boxer", "costume"), articleInfo) ||
				(listContains(list("veste", "blouson", "manteau"), articleInfo) && 
						containsSize(availableSizes, list("T. 44", "T. 46", "T. 48", "T. 50", "T. 52", "T. 54"))) ||
				(listContains(list("pantalon", "short", "jean"), articleInfo) &&
						containsSize(availableSizes, list("T. 42", "T. 44", "T. 34 (US)")))		
                ;
	}
}
