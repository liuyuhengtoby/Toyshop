package mypkg;

import java.io.*;
import java.sql.*;
import java.util.logging.*;
import javax.naming.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import javax.sql.DataSource;

public class EntryServlet extends HttpServlet {

   private DataSource pool; // Database connection pool

   @Override
   public void init(ServletConfig config) throws ServletException {
      try {
         // Create a JNDI Initial context to be able to lookup the DataSource
         InitialContext ctx = new InitialContext();
         // Lookup the DataSource
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
         conn = pool.getConnection(); // Get a connection from the pool
         stmt = conn.createStatement();

         out.println("<!DOCTYPE html><html><head>");
         out.println("<link rel='stylesheet' href='css/style.css' type='text/css' />");
         out.println("<title>Jelly Toy</title></head><body>");
         out.println("<h2>Welcome to Jelly Toy!</h2>");

         // form for item filtering
         out.println("<form id='search' method='get' action='search'>");

         // check box for category search
         out.println("Choose categories: <br/>");
         out.println("<input type='checkbox' name='category' value='Animals' />Animals");
         out.println("<input type='checkbox' name='category' value='Food' />Food");
         out.println("<input type='checkbox' name='category' value='Decorations' />Decorations");
         out.println("<br/><br/>");

         // text box for price range search
         out.println("Choose price range: <br/>");
         out.println("<label for='pr_min'>From $</label>");
         out.println("<input type='text' name='pr_min' value='0' />");
         out.println("<label for='pr_max'>To $</label>");
         out.println("<input type='text' name='pr_max' value='250' />");
         out.println("<br/><br/>");

         // text box for keyword search
         out.println("<label for='keyword'>Search by keyword:</label>");
         out.println("<input type='text' name='keyword' />");

         // Submit and reset buttons
         out.println("<br /><br />");
         out.println("<input type='submit' value='SEARCH' />");
         out.println("<input type='reset' value='CLEAR' />");
         out.println("</form>");

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
                  out.println("<p class=link><a href='cart?todo=view'>View Shopping Cart</a></p>");
               }
               if (user != null) { // have user logined, show log out button
                  out.println("<form class='user' method='get' action='logout' >");
                  out.println("<p>Currently signed in as " + user.getUsername() + "</p>");
                  out.println("<input type='submit' value='Log out' />");
                  out.println("</form>");
               } else { // no user logined, show login button
                  // show login
                  out.println("<form class='user' method='get' action='user.html' >");
                  out.println("<p>Currently not signed in ...</p>");
                  out.println("<input type='submit' value='Sign in' />");
                  out.println("</form>");
               }

            }
         } else { // session dont exist, means user havnt sign in
            // show login
            out.println("<form class='user' method='get' action='user.html' >");
            out.println("<p>Currently not signed in ...</p>");
            out.println("<input type='submit' value='Sign in' />");
            out.println("</form>");
         }

         out.println("</body></html>");
      } catch (SQLException ex) {
         out.println("<h3 class=msg>Service not available. Please try again later!</h3></body></html>");
         Logger.getLogger(EntryServlet.class.getName()).log(Level.SEVERE, null, ex);
      } finally {
         out.close();
         try {
            if (stmt != null)
               stmt.close();
            if (conn != null)
               conn.close(); // Return the connection to the pool
         } catch (SQLException ex) {
            Logger.getLogger(EntryServlet.class.getName()).log(Level.SEVERE, null, ex);
         }
      }
   }

   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException {
      doGet(request, response);
   }
}