package com.trungsi.vpclient;

import org.junit.Test;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.trungsi.vpclient.VPClient.*;
import static org.junit.Assert.*;

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

        StringBuilder builder = new StringBuilder();
        double totalAmount = 0;
        for (Map<String, String> order : orders) {
            double amount = getTotalAmount(order);
            totalAmount += amount;

            builder.append(order.get("name") + ";" + order.get("date") + ";" + amount + "\n");
        }

        System.out.println( builder);
        System.out.println(totalAmount);
    }

    private double getTotalAmount(Map<String, String> order) throws Exception {
        List<Map<String, String>> orderDetail = getOrderDetail(driver, order);
        return getTotalAmountFromOrderDetail(orderDetail.get(0));
    }


    private double toDouble(String strAmount) throws Exception {
        NumberFormat format = //new DecimalFormat("###,##");
                NumberFormat.getNumberInstance(Locale.FRANCE);
        return format.parse(strAmount.split(" ")[0]).doubleValue();
    }

    @Test
    public void testGoToViewOrders() {
        setUpViewOrders();

        assertTrue(driver.getPageSource(), driver.getPageSource().contains("Liste de mes commandes"));
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
        assertEquals(driver.getPageSource(), 17, orders.size());

        System.out.println(orders);

    }

    @Test
    public void testGetOrderDetail() throws Exception {
        setUpViewOrders();

        List<Map<String, String>> orders = getOrdersInPage(driver);

        for (Map<String, String> order : orders) {
            printOrderDetail(order);
        }

    }

    private void printOrderDetail(Map<String, String> order) throws Exception {
        System.out.println(order.get("name"));
        System.out.println(order.get("orderDetailLink"));

        List<Map<String, String>> orderDetail = getOrderDetail(driver, order);

        System.out.println(orderDetail);
        System.out.println(getTotalAmountFromOrderDetail(orderDetail.get(0)));
    }

    private double getTotalAmountFromOrderDetail(Map<String, String> orderDetail) throws Exception {
        return toDouble(orderDetail.get("totalAmount")) - toDouble(orderDetail.get("returnAmount"));
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

        List<Map<String, String>> orders1 = getOrdersInPage(driver);
        assertFalse(orders1.isEmpty());
        //System.out.println(orders1);

        boolean nextPageLink = nextOrderPage(driver);
        assertTrue(nextPageLink);

        List<Map<String, String>> orders2 = getOrdersInPage(driver);
        assertFalse(orders2.isEmpty());
        //System.out.println(orders2);


        assertFalse(orders1.get(0).equals(orders2.get(0)));



        nextPageLink = nextOrderPage(driver);
        assertTrue(nextPageLink);

        List<Map<String, String>> orders3 = getOrdersInPage(driver);
        assertFalse(orders3.isEmpty());
        //System.out.println(orders2);


        assertFalse(orders2.get(0).equals(orders3.get(0)));

    }

}
