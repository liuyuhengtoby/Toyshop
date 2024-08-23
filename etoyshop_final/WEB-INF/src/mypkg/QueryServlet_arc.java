package mypkg;

import java.io.*;
import java.sql.*;
import java.util.logging.*;
import javax.naming.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import javax.sql.*;

public class QueryServlet extends HttpServlet {

   private DataSource pool; // Database connection pool

   @Override
   public void init(ServletConfig config) throws ServletException {
      try {
         // Create a JNDI Initial context to be able to lookup the DataSource
         InitialContext ctx = new InitialContext();
         // Lookup the DataSource.
         pool = (DataSource) ctx.lookup("java:comp/env/jdbc/mysql_etoyshop");
         if (pool == null)
            throw new ServletException("Unknown DataSource 'jdbc/mysql_etoyshop'");
      } catch (NamingException ex) {
         Logger.getLogger(EntryServlet.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException {
      response.setContentType("text/html;charset=UTF-8");
      PrintWriter out = response.getWriter();

      Connection conn = null;
      Statement stmt = null;

      try {
         // Retrieve and process request parameters: "author" and "search"
         String[] categories = request.getParameterValues("category");
         boolean hasCategoryParam = categories != null && !categories.equals("Select...");
         String keyword = request.getParameter("keyword").trim();
         boolean hasKeywordParam = keyword != null && (keyword.length() > 0);
         int pr_min = Integer.parseInt(request.getParameter("pr_min"));
         int pr_max = Integer.parseInt(request.getParameter("pr_max"));
         // TODO: input validation: pr_min, pr_max

         out.println("<!DOCTYPE html><html><head>");
         out.println("<link rel='stylesheet' href='css/style.css' type='text/css' />");
         out.println("<title>EToyShop</title></head><body>");
         out.println("<h2>EToyShop Search Results</h2>");

         if (!hasCategoryParam && !hasKeywordParam) { // No params present
            out.println("<h3>Please select at least one category or enter a search term!</h3>");
            out.println("<p><a href='start'>Back to Select Menu</a></p>");
         } else {
            conn = pool.getConnection();
            stmt = conn.createStatement();

            // Form a SQL command
            StringBuilder sqlStr = new StringBuilder(); // more efficient than String
            sqlStr.append("SELECT * FROM toys WHERE qty > 0 AND (");
            if (hasCategoryParam) {
               sqlStr.append("category IN (");
               for (int i = 0; i < categories.length; ++i) {
                  sqlStr.append("'" + categories[i] + (i < categories.length - 1 ? "', " : "'))"));
               }
            }
            if (hasKeywordParam) {
               if (hasCategoryParam) {
                  sqlStr.append(" OR ");
               }
               sqlStr.append("category LIKE '%").append(keyword)
                     .append("%' OR title LIKE '%").append(keyword).append("%'");
            }
            sqlStr.append(" AND (price BETWEEN ").append(pr_min).append(" AND ").append(pr_max).append(")");
            sqlStr.append(" ORDER BY category, title");
            // System.out.println(sqlStr); // check tomcat console for debugging
            ResultSet rset = stmt.executeQuery(sqlStr.toString());

            if (!rset.next()) { // Check for empty ResultSet (no toy found)
               out.println("<h3>No toy found. Please try again!</h3>");
               out.println("<p><a href='start'>Back to Select Menu</a></p>");
            } else {

               // Print the result in an HTML form inside a table
               out.println("<form method='get' action='cart'>");
               out.println("<input type='hidden' name='todo' value='add' />");
               out.println("<table border='1' cellpadding='6'>");
               out.println("<tr>");
               out.println("<th>&nbsp;</th>");
               out.println("<th>CATEGORY</th>");
               out.println("<th>TITLE</th>");
               out.println("<th>PRICE</th>");
               out.println("<th>QTY</th>");
               out.println("</tr>");

               // ResultSet's cursor now pointing at first row
               do {
                  // Print each row with a checkbox identified by book's id
                  String id = rset.getString("id");
                  out.println("<tr>");
                  out.println("<td><input type='checkbox' name='id' value='" + id + "' /></td>");
                  out.println("<td>" + rset.getString("category") + "</td>");
                  out.println("<td>" + rset.getString("title") + "</td>");
                  out.println("<td>$" + rset.getString("price") + "</td>");
                  out.println("<td><input type='text' size='3' value='1' name='qty" + id + "' /></td>");
                  out.println("</tr>");
               } while (rset.next());
               out.println("</table><br />");

               // Submit and reset buttons
               out.println("<input type='submit' value='Add to My Shopping Cart' />");
               out.println("<input type='reset' value='CLEAR' /></form>");

               // Hyperlink to go back to search menu
               out.println("<p><a href='start'>Back to Select Menu</a></p>");

               // Show "View Shopping Cart" if the cart is not empty
               HttpSession session = request.getSession(false); // check if session exists
               if (session != null) { // session exists
                  Cart cart;
                  User user;
                  synchronized (session) {
                     // Retrieve the shopping cart for this session, if any. Otherwise, create one.
                     cart = (Cart) session.getAttribute("cart");
                     user = (User) session.getAttribute("user");
                     if (cart != null && !cart.isEmpty()) {
                        out.println("<p><a href='cart?todo=view'>View Shopping Cart</a></p>");
                     }
                     if (user != null) { // have user logined, show log out button
                        out.println("<p>logined as " + user.getUsername() + "</p>");
                        out.println("<form method='get' action='logout' >");
                        out.println("<input type='submit' value='Log out' />");
                        out.println("</form>");
                     } else { // no user logined, show login button
                        out.println("<form method='get' action='user.html' >");
                        out.println("<input type='submit' value='Sign in' />");
                        out.println("</form>");
                     }

                  }
               } else { // session dont exist, means user havnt sign in
                  // show login
                  out.println("<form method='get' action='user.html' >");
                  out.println("<input type='submit' value='Sign in' />");
                  out.println("</form>");
               }

               out.println("</body></html>");
            }
         }
      } catch (SQLException ex) {
         out.println("<h3>Service not available. Please try again later!</h3></body></html>");
         Logger.getLogger(QueryServlet.class.getName()).log(Level.SEVERE, null, ex);
      } finally {
         out.close();
         try {
            if (stmt != null)
               stmt.close();
            if (conn != null)
               conn.close(); // Return the connection to the pool
         } catch (SQLException ex) {
            Logger.getLogger(QueryServlet.class.getName()).log(Level.SEVERE, null, ex);
         }
      }
   }

   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException {
      doGet(request, response);
   }
}