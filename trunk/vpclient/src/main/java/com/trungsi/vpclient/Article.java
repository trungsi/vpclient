package com.trungsi.vpclient;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: trungsi
 * Date: 17/08/2014
 * Time: 22:17
 * To change this template use File | Settings | File Templates.
 */
public class Article {
    private String name;
    private String link;

    private SubCategory subCategory;

    public Article(String name, String link, SubCategory subCategory) {
        this.name = name;
        this.link = link;
        this.subCategory = subCategory;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public SubCategory getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(SubCategory subCategory) {
        this.subCategory = subCategory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfo() {
        return subCategory.getInfo() + "|" + getName();
    }
}
