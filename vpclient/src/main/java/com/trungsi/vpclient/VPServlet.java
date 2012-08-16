package com.trungsi.vpclient;
  
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;
  
public class VPServlet extends HttpServlet
{
  private Map<Long, Thread> map = new HashMap<Long, Thread>();
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException ,IOException {
    if (req.getParameter("stop") != null) {
      long id = Long.parseLong(req.getParameter("id"));
      Thread t = map.get(id);
      if (t != null) {
        t.interrupt();
      }
  } else if(req.getParameter("start") != null) {
    final Map<String, String> context = new HashMap<String, String>();
    context.put("driver", "HtmlUnit");
    context.put("selectedSale", req.getParameter("camp"));
    context.put("user", req.getParameter("login"));
    context.put("pwd", req.getParameter("password"));
    String excluded = req.getParameter("excluded");
  if (excluded != null) {
    
  }
    Thread t = new Thread() {
      public void run() {
    try {
        //VPClient.start(context);
  } catch (Exception e) {
      e.printStackTrace();
    }
      }
    };
    t.start();
  
    long id = System.currentTimeMillis();
    map.put(id, t);
  
    req.setAttribute("id", id);
    req.setAttribute("action", "start");
  
    //resp.getWriter().println("Start VPClient " + id);
  } //else {
    req.getRequestDispatcher("/vp.jsp").forward(req, resp);
    //}
  }
}