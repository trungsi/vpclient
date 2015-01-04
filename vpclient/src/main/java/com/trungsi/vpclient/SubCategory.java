package com.trungsi.vpclient;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: trungsi
 * Date: 17/08/2014
 * Time: 22:35
 * To change this template use File | Settings | File Templates.
 */
public class SubCategory {
    private static final String FAKE_SUBCATEGORY = "FAKE_SUB";

    private String name;
    private String link;

    private Category category;

    private Map<String, String> attributes = new HashMap<>();

    public SubCategory(String name, String link, Category category) {
        this.name = name;
        this.link = link;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    /*public boolean isEmpty() {
        return link == null;
    }*/

    public void addAttribute(String name, String value) {
        this.attributes.put(name, value);
    }

    public String getAttribute(String name) {
        return attributes.get(name);
    }

    // fake SubCategory is used for categories which have no sub category
    // so the link to access SubCategory is *the link* of the category itself
    // fakeSubCategory.link == fakeSubCategory.category.link
    public static SubCategory fake(Category category) {
        return new SubCategory(FAKE_SUBCATEGORY, category.getLink(), category);
    }

    @Override
    public String toString() {
        return name;
    }

    public String getInfo() {
        return category.getName() + "|" + getName();
    }
}
