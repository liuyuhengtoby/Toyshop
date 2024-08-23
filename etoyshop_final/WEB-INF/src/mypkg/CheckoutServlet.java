package mypkg;
 
import java.io.*;
import java.sql.*;
import java.util.logging.*;
import javax.naming.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import javax.sql.DataSource;
 
public class CheckoutServlet extends HttpServlet {
 
   private DataSource pool;  // Database connection pool
 
   @Override
   public void init(ServletConfig config) throws ServletException {
      try {
         // Create a JNDI Initial context to be able to lookup the DataSource
         InitialContext ctx = new InitialContext();
         // Lookup the DataSource.
         pool = (DataSource)ctx.lookup("java:comp/env/jdbc/mysql_etoyshop");
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
      ResultSet rset = null;
      String sqlStr = null;
      HttpSession session = null;
      Cart cart = null;
      User user = null;
 
      try {
         conn = pool.getConnection();  // Get a connection from the pool
         stmt = conn.createStatement();
 
         out.println("<!DOCTYPE html><html><head>");
         out.println("<link rel='stylesheet' href='css/style.css' type='text/css' />");
         out.println("<title>Jelly Toy</title></head><body>");
         out.println("<h2>Jelly Toy - Checkout</h2>");
 
         // Retrieve the Cart
         session = request.getSession(false);
         if (session == null) {
            out.println("<h3>Please login before checking out</h3></head><body>");
            return;
         }
         synchronized (session) {
            cart = (Cart) session.getAttribute("cart");
            user = (User) session.getAttribute("user");
            if (cart == null) {
               out.println("<h3>Your Shopping cart is empty!</h3></body></html>");
               return;
            }
         }
 
         // Display the name, email and phone (arranged in a table)
         out.println("<table>");
         out.println("<tr>");
         out.println("<td>User Name:</td>");
         out.println("<td>" + user.getUsername() + "</td></tr>");
         out.println("<tr>");
         out.println("<td>Phone Number:</td>");
         out.println("<td>" + user.getPhonenumber() + "</td></tr>");
         out.println("<tr>");
         out.println("<td>Email:</td>");
         out.println("<td>" + user.getEmail() + "</td></tr>");
         out.println("<tr>");
         out.println("<td>Address:</td>");
         out.println("<td>" + user.getAddress() + "</td></tr>");
         out.println("</table>");
 
         // Print toys ordered in a table
         out.println("<br />");
         out.println("<table border='1' cellpadding='6'>");
         out.println("<tr>");
         out.println("<th>CATEGORY</th>");
         out.println("<th>NAME</th>");
         out.println("<th>PRICE</th>");
         out.println("<th>QTY</th></tr>");
 
         float totalPrice = 0f;
         for (CartItem item : cart.getItems()) {
            int id = item.getId();
            String category = item.getCategory();
            String title = item.getTitle();
            int qtyOrdered = item.getQtyOrdered();
            float price = item.getPrice();
 
            // TODO: check for price and qtyAvailable
            // Update the books table and insert an order record
            sqlStr = "UPDATE toys SET qty = qty - " + qtyOrdered + " WHERE id = " + id;
            // System.out.println(sqlStr);  // for debugging
            stmt.executeUpdate(sqlStr);
 
            sqlStr = "INSERT INTO orders values (0, "
                    + id + ", " + qtyOrdered + ", '" + user.getId() + "')";
            // System.out.println(sqlStr);  // for debugging
            stmt.executeUpdate(sqlStr);
 
            // Show the book ordered
            out.println("<tr>");
            out.println("<td>" + category + "</td>");
            out.println("<td>" + title + "</td>");
            out.println("<td>" + price + "</td>");
            out.println("<td>" + qtyOrdered + "</td></tr>");
            totalPrice += price * qtyOrdered;
         }
         out.println("<tr><td colspan='4' align='right'>Total Price: $");
         out.printf("%.2f</td></tr>", totalPrice);
         out.println("</table>");
 
         out.println("<h3 class=msg>Thank you.</h3>");
         out.println("<p class=link><a href='start'>Back to shop</a></p>");
         out.println("</body></html>");
 
         cart.clear();   // empty the cart
      } catch (SQLException ex) {
         cart.clear();   // empty the cart
         out.println("<h3 class=msg>Service not available. Please try again later!</h3></body></html>");
         Logger.getLogger(CheckoutServlet.class.getName()).log(Level.SEVERE, null, ex);
      } finally {
         out.close();
         try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();  // Return the connection to the pool
         } catch (SQLException ex) {
            Logger.getLogger(CheckoutServlet.class.getName()).log(Level.SEVERE, null, ex);
         }
      }
   }
 
   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {
      doGet(request, response);
   }
}