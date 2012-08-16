<%@page contentType="text/html" import="java.util.Date"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
  <body>
    <div align="center">
      <center>
       <table border="0" cellpadding="0" cellspacing="0" width="460"   bgcolor="#EEFFCA">
        <tr><td width="100%"><font size="6" color="#008000">&nbsp;Date example</font></td></tr>
        <tr><td width="100%"><b>&nbsp;Current Date and time is:&nbsp;<font color="#FF0000"><%=new Date().toString()%> </font></b></td></tr>
     </table>
  </center>
 </div>
    <c:choose>
      <c:when test="${request.action == null}">  
 <div>
   <form action="/vp">
     <input type="hidden" name="start" value=""/>
     <table>
       <tr><td>Login</td><td><input type="text" name="login"/></td></tr>
       <tr><td>Mot de passe</td><td><input type="password" name="password"/></td></tr>
       <tr><td>Camp</td><td><input type="text" name="marque"/></td></tr>
       <tr><td></td><td><input type="submit" value="Start VP"/></td></tr>
       
     </table>  
   </form>  
 </div>
  </c:when>
    <c:otherwise>
      VPClient started ${request.id}
    </c:otherwise>
  </c:choose>
 </body>
</html>