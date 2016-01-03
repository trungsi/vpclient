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
public abstract class ArticleType {

	boolean isPants(String articleInfo) {
        return articleInfo.contains("pantalon");
    }
	
	boolean isShirt(String articleInfo) {
		return articleInfo.contains("chemis");
	}
	
	boolean isSoutienGorge(String articleInfo) {
		return articleInfo.contains("soutien");
	}

	boolean isCostume(String articleInfo) {
		return listContains(list("costume", "veste"), articleInfo);
	}
	
	boolean isJean (String articleInfo) {
		return articleInfo.contains("jean");
	}

	boolean isShoes (String articleInfo) {
		return listContains(
				list("chaussure", "basket", "sneaker", "derbie", 
					"richelieu", "moscassin", "botte", "bottine", "sandale", 
					"ballerine", "escarpin", "tong", "mule"), 
				articleInfo);
	}
	
	String getDefault(String name, Context context, String defaultValue) {
		String value = context.get(name);
		return (value == null || value.equals("")) ? defaultValue : value;
	}

	protected boolean containsSize(List<String> availableSizes, List<String> sizes) {
        if (availableSizes != null) {
            for (String size : availableSizes) {
                if (listContains(sizes, size))  {
                    return true;
                }
            }
        }
        return false;
    }
	
	abstract List<String> getPreferredSizes(String articleInfo, Context context);

	abstract boolean accept(String articleInfo, List<String> availableSizes);

}
