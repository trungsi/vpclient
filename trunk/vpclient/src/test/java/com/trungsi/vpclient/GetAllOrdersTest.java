package com.trungsi.vpclient;

import org.junit.*;
import org.junit.Test;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.trungsi.vpclient.VPClient.*;
import static com.trungsi.vpclient.VPClient.getViewOrdersNextPageLink;
import static com.trungsi.vpclient.VPClient.goToLink;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: trungsi
 * Date: 28/10/2013
 * Time: 15:50
 * To change this template use File | Settings | File Templates.
 */
public class GetAllOrdersTest extends AbstractVPClientTestCase {

    @Test
    public void testGetAllOrders() throws Exception {
        loadDriverForTest("trungsi@hotmail.com", "trungsi");

        List<Map<String, String>> orders = getAllOrders(driver);
        System.out.println(orders.size());

        double totalAmount = 0;
        for (Map<String, String> order : orders) {
            String amountAsString = order.get("amount");
            totalAmount += toDouble(amountAsString);

            System.out.println(order.get("name") + ";" + order.get("date") + ";" + order.get("amount"));
        }

        System.out.println(totalAmount);
    }


    private double toDouble(String strAmount) throws Exception {
        NumberFormat format = //new DecimalFormat("###,##");
                NumberFormat.getNumberInstance(Locale.FRANCE);
        return format.parse(strAmount.split(" ")[0]).doubleValue();
    }

    @Test
    public void testGoToViewOrders() {
        setUpViewOrders();

        assertTrue(driver.getPageSource().contains("Vous trouverez ci-dessous le descriptif de vos commandes."));
    }

    private void setUpViewOrders() {
        loadDriverForTest("trungsi@hotmail.com", "trungsi");
        goToViewOrders(driver);
    }

    @Test
    public void testGetOrdersInPage() {
        setUpViewOrders();

        List<Map<String, String>> orders = getOrdersInPage(driver);
        assertNotNull(orders);
        assertEquals(15, orders.size());

        System.out.println(orders);

    }

    @Test
    public void testGetOrderDetail() {
        setUpViewOrders();

        List<Map<String, String>> orders = getOrdersInPage(driver);

        Map<String, String> order = orders.get(0);
        System.out.println(order.get("name"));
        System.out.println(order.get("orderDetailLink"));

        List<Map<String, String>> orderDetail = getOrderDetail(driver, order);

        System.out.println(orderDetail);
    }

    @Test
    public void testGetOrderDetailRochas () {
        loadDriverForTest("trungsi@hotmail.com", "trungsi");

        List<Map<String, String>> orders = getAllOrders(driver);
        ArrayList<List<Map<String, String>>> orderDetails = new ArrayList<>();
        for (Map<String, String> order : orders) {
            if (order.get("name").contains("Rochas")) {
                List<Map<String, String>> orderDetail = getOrderDetail(driver, order);
                orderDetails.add(orderDetail);
            }
        }

        for (List<Map<String, String>> detail : orderDetails) {
            System.out.println(detail);
        }

    }
    @Test
    public void testGetViewOrdersNextPageLink() {
        setUpViewOrders();

        List<String> nextPageLinks = new ArrayList<>();

        String nextPageLink = getViewOrdersNextPageLink(driver);
        while (nextPageLink != null) {
            nextPageLinks.add(nextPageLink);
            goToLink(driver, nextPageLink);
            nextPageLink = getViewOrdersNextPageLink(driver);
        }

        System.out.println(nextPageLinks);
    }

}
