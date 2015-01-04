package com.trungsi.vpclient;

/**
 * Created with IntelliJ IDEA.
 * User: trungsi
 * Date: 17/08/2014
 * Time: 22:59
 * To change this template use File | Settings | File Templates.
 */
public class Category {

    private String name;
    private String link;

    public Category(String name, String link) {
        this.name = name;
        this.link = link;
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

    @Override
    public String toString() {
        return name;
    }
}
