package com.trungsi.vpclient;

import com.google.common.eventbus.EventBus;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created with IntelliJ IDEA.
 * User: trungsi
 * Date: 04/01/15
 * Time: 15:04
 * To change this template use File | Settings | File Templates.
 */
public class Basket {
    static final ConcurrentHashMap<String, Basket> baskets = new ConcurrentHashMap<>();

    private ConcurrentLinkedQueue<Article> articles = new ConcurrentLinkedQueue<>();

    private EventBus eventBus = new EventBus();

    public static Basket get(String basketName) {
        Basket basket = baskets.putIfAbsent(basketName, new Basket());
        return basket != null ? basket : baskets.get(basketName);
    }

    public void addArticle(Article article) {
        articles.add(article);
        eventBus.post(new BasketUpdateEvent(this));
    }

    public int getBasketSize() {
        return articles.size();
    }

    public void addUpdateListener(Object obj) {
        eventBus.register(obj);
    }
}

