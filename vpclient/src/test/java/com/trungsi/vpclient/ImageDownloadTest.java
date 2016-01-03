package com.trungsi.vpclient;

import org.junit.Test;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: trungsi
 * Date: 28/10/2013
 * Time: 15:37
 * To change this template use File | Settings | File Templates.
 */
public class ImageDownloadTest extends AbstractVPClientTestCase {


    @Test
    public void testDownload() {
        List<Article> articles = findAllArticles("lee", "homme");

    }
}
