/**
 * 
 */
package com.trungsi.vpclient;

import static com.trungsi.vpclient.utils.CollectionUtils.list;
import static com.trungsi.vpclient.utils.CollectionUtils.listContains;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * @author trungsi
 *
 */
public class SizeHelper {

	static final Logger LOG = Logger.getLogger(SizeHelper.class);
	
	static List<String> getPreferedSize(Article article, Context context, List<String> availableSizes) {
		String articleInfo = article.getInfo().toLowerCase();
	
	    if (isWomanArticle(articleInfo)) {
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
		} else if (isManArticle(articleInfo)) {
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
	
	            if (!(articleInfo.contains("slim") || articleInfo.contains("cintré") || articleInfo.contains("ajusté"))) { // T. 46 for coupe droite
	                return list("Veste T. 46|Pantalon T. 38|T. 46|T. 38".split("\\|"));
	            }
	
	            return getManCostumeSizes(context);
	        } else if (isShirt(articleInfo)) { // chemise
	            return getManShirtSizes(context);
	        } else if (isPants(articleInfo)) {
	            if (articleInfo.contains("hackett")) {
	                return list("T. 32 (UK)|T. 31|31");
	            }
	            return getManPantsSize(context);
	        } else {
	            List<String> sizes = getManClothingClothingSizes(context);
	            if(articleInfo.contains("hackett")) {
	                List<String> newSizes = list(" S ", "T. S");
	                newSizes.addAll(sizes);
	                return newSizes;
	            }
	            return sizes;
	        }
		} else if (isGirlArticle(articleInfo)) {
			if (isShoes(articleInfo)) {
				return getGirlShoesSizes(context);
			} else {
				return getGirlClothingSizes(context);
			}
		} else if (isBoyArticle(articleInfo)) {
			// ne fait rien
			return new ArrayList<String>();
		} else { // by default, man too :)
	
	        if (containsManSize(availableSizes)) {
	            if (isJean(articleInfo)) {
	                return getManJeanSizes(context);
	            } else if (isShoes(articleInfo)) {
	                return getManShoesSizes(context);
	            } else if (isCostume(articleInfo)) {
	                if (articleInfo.contains("windsor")) { // special size for Windsor
	                    return list("T. 46 (FR)|Veste T. 46|T. 46|T. 46".split("\\|"));
	                }
	
	                if (!(articleInfo.contains("slim") || articleInfo.contains("cintré") || articleInfo.contains("ajusté"))) { // T. 46 for coupe droite
	                    return list("Veste T. 46|Pantalon T. 38|T. 46|T. 38".split("\\|"));
	                }
	
	                // T. 48 for slim or cintrée
	                return getManCostumeSizes(context);
	            } else {
	                return getManClothingClothingSizes(context);
	            }
	        } else if (/*
	            for jupe, robe,... it's not sure if it's about woman or girl.
	            So have to check further
	             */
	                (listContains(list("jupe", "robe",
	                        "body", "bodies",
	                        "collant", "legging"), articleInfo)) ||
	                        containsWomanSize(availableSizes)) {
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
	        } else {
	            LOG.error("Cannot determine article type from " + articleInfo);
	
	            if (isJean(articleInfo)) {
	                return getManJeanSizes(context);
	            } else if (isShoes(articleInfo)) {
	                return getManShoesSizes(context);
	            } else if (isCostume(articleInfo)) {
	                return getManCostumeSizes(context);
	            } else if (isShirt(articleInfo)) { // chemise
		            return getManShirtSizes(context);
		        } else {
	                return getManClothingClothingSizes(context);
	            } 
	        }
	
		}
	}

	static List<String> getWomanJeanSizes(Context context) {
		return list(getDefault(Context.WOMAN_JEAN_SIZES, context, " 26 |W26|T.36|T. 36").split("\\|"));
	}
	
	static List<String> getWomanShoesSizes(Context context) {
		return list(getDefault(Context.WOMAN_SHOES_SIZES, context, " 37 |T.37").split("\\|"));
	}
	
	static List<String> getWomanLingerieSizes(Context context) {
		return list(getDefault(Context.WOMAN_LINGERIE_SIZES, context, "90A").split("\\|"));
	}
	
	static List<String> getWomanClothingSizes(Context context) {
		return list(getDefault(Context.WOMAN_CLOTHING_SIZES, context, " 36 |T.36 (FR)|T.36 |T. 36|34/36| S |.S ").split("\\|"));
	}
	
	static List<String> getGirlShoesSizes(Context context) {
		return list(getDefault(Context.GIRL_SHOES_SIZES, context, " 23 |T.23|T. 23").split("\\|"));
	}
	
	static List<String> getGirlClothingSizes(Context context) {
		return list(getDefault(Context.GIRL_CLOTHING_SIZES, context, "3 ans").split("\\|"));
	}
	
	static List<String> getManJeanSizes(Context context) {
		return list(getDefault(Context.MAN_JEAN_SIZES, context, " 30 |W30|T.30|T.40|T. 40").split("\\|"));
	}
	
	static List<String> getManShoesSizes(Context context) {
		return list(getDefault(Context.MAN_SHOES_SIZES, context, "40.5| 41 |T.41|T. 41").split("\\|"));
	}
	
	static List<String> getManCostumeSizes(Context context) {
		return list(getDefault(Context.MAN_COSTUME_SIZES, context, " M |.M |T.40|T. 40").split("\\|"));
	}
	
	static List<String> getManClothingClothingSizes(Context context) {
		return list(getDefault(Context.MAN_CLOTHING_SIZES, context, " M |.M | 38 | 40 |T.40|T. 40").split("\\|"));
	}
	
	static List<String> getManPantsSize(Context context) {
        return list("T. 40 (FR)|T. 30 (US)|T. 40|T. 30".split("\\|"));
    }

    static boolean isPants(String articleInfo) {
        return articleInfo.contains("pantalon");
    }

    static List<String> getWomanShirtSizes(Context context) {
		return list(getDefault(Context.WOMAN_SHIRT_SIZES, context, "T. 36|T.36| XS ").split("\\|"));
	}

	static List<String> getManShirtSizes(Context context) {
		return list(getDefault(Context.MAN_SHIRT_SIZES, context, "T. 39|T.39| M ").split("\\|"));
	}

	static boolean isShirt(String articleInfo) {
		return articleInfo.contains("chemis");
	}

	static boolean isManArticle(String articleInfo) {
        return listContains(list("calçon", "boxer", "costume"), articleInfo) ||
                (articleInfo.contains("homme") && !articleInfo.contains("femme"));
	}

    static boolean containsManSize(List<String> availableSizes) {
        List<String> manSizes = list("T. 42", "T. 44", "T. 46", "T. 48", "T. 50", "T. 34 (US)");
        return containsSize(availableSizes, manSizes);
    }

    private static boolean containsSize(List<String> availableSizes, List<String> sizes) {
        if (availableSizes != null) {
            for (String size : availableSizes) {
                if (listContains(sizes, size))  {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean isSoutienGorge(String articleInfo) {
		return articleInfo.contains("soutien");
	}

	static boolean isCostume(String articleInfo) {
		return listContains(list("costume", "veste"), articleInfo);
	}

	static boolean isWomanArticle(String articleInfo) {
		return /*isSaleForWomanOnly() ||*/ articleInfo.contains("farrutx") ||
                listContains(list(
                                    "soutien", "lingerie",
                                    "chemisier", "ballerine", "robe", "jupe", "escarpin",
                                    "compens", "sandale", " talon", "cuissard", "culotte", "top", "pantacourt"), articleInfo) ||
                (articleInfo.contains("femme") && !articleInfo.contains("homme"));
	}

    static boolean containsWomanSize(List<String> availableSizes) {
        List<String> womanSizes = list("T. 25 (US)", "T. 26 (US)", "T. 27 (US)", "T. 34 (FR)", "T. 32/34", "T. 34/36");
        return containsSize(availableSizes, womanSizes);
    }

    /*private static boolean isKidArticle(String articleInfo, Select select) {
        return articleInfo.contains("enfant") || articleInfo.contains("kid") ||
                /*
                if size contains 'ans', it's sure for kids
                 *
                containsSize(select, list(" ans"));
    }*/

	static boolean isGirlArticle(String articleInfo) {
		return articleInfo.contains("fille");
	}

	static boolean isBoyArticle(String articleInfo) {
		return articleInfo.contains("garçon");
	}

	static boolean isJean (String articleInfo) {
		return articleInfo.contains("jean");
	}

	static boolean isShoes (String articleInfo) {
		return listContains(
				list("chaussure", "basket", "sneaker", "derbie", 
					"richelieu", "moscassin", "botte", "bottine", "sandale", 
					"ballerine", "escarpin", "tong", "mule"), 
				articleInfo);
	}

	private static String getDefault(String name, Context context, String defaultValue) {
		String value = context.get(name);
		return (value == null || value.equals("")) ? defaultValue : value;
	}
	
}
