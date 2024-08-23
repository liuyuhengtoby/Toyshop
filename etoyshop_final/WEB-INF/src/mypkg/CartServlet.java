package mypkg;

import java.io.*;
import java.sql.*;
import java.util.logging.*;
import javax.naming.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import javax.sql.DataSource;

public class CartServlet extends HttpServlet {

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

      // Retrieve current HTTPSession object. If none, create one.
      HttpSession session = request.getSession(true);
      Cart cart;
      synchronized (session) { // synchronized to prevent concurrent updates
         // Retrieve the shopping cart for this session, if any. Otherwise, create one.
         cart = (Cart) session.getAttribute("cart");
         if (cart == null) { // No cart, create one.
            cart = new Cart();
            session.setAttribute("cart", cart); // Save it into session
         }
      }

      Connection conn = null;
      Statement stmt = null;
      ResultSet rset = null;
      String sqlStr = null;

      try {
         conn = pool.getConnection(); // Get a connection from the pool
         stmt = conn.createStatement();

         out.println("<!DOCTYPE html><html><head>");
         out.println("<link rel='stylesheet' href='css/style.css' type='text/css' />");
         out.println("<title>Jelly Toy</title></head><body>");
         out.println("<h2>Jelly Toy - Your Shopping Cart</h2>");

         // This servlet handles 4 cases:
         // (1) todo=add id=1001 qty1001=5 [id=1002 qty1002=1 ...]
         // (2) todo=update id=1001 qty1001=5
         // (3) todo=remove id=1001
         // (4) todo=view

         String todo = request.getParameter("todo");
         if (todo == null)
            todo = "view"; // to prevent null pointer

         if (todo.equals("add") || todo.equals("update")) {
            // (1) todo=add id=1001 qty1001=5 [id=1002 qty1002=1 ...]
            // (2) todo=update id=1001 qty1001=5
            String[] ids = request.getParameterValues("id");
            if (ids == null) {
               out.println("<h3 class=msg>Please Select at least one Toy to add!</h3></body></html>");
               return;
            }
            for (String id : ids) {
               sqlStr = "SELECT * FROM toys WHERE id = " + id;
               // System.out.println(sqlStr); // for debugging
               rset = stmt.executeQuery(sqlStr);
               rset.next(); // Expect only one row in ResultSet
               String title = rset.getString("title");
               String category = rset.getString("category");
               float price = rset.getFloat("price");
               String img_url = rset.getString("image_url");

               // TODO: input validation for qty ordered
               int qtyOrdered = Integer.parseInt(request.getParameter("qty" + id));
               int idInt = Integer.parseInt(id);
               if (todo.equals("add")) {
                  cart.add(idInt, title, category, price, qtyOrdered, img_url);
               } else if (todo.equals("update")) {
                  cart.update(idInt, qtyOrdered);
               }
            }

         } else if (todo.equals("remove")) {
            String id = request.getParameter("id"); // Only one id for remove case
            cart.removeById(Integer.parseInt(id));
         }

         // All cases - Always display the shopping cart
         if (cart.isEmpty()) {
            out.println("<p class=msg>Your shopping cart is empty</p>");
         } else {
            out.println("<table border='1' cellpadding='6'>");
            out.println("<tr>");
            out.println("<th>CATEGORY</th>");
            out.println("<th>NAME</th>");
            out.println("<th>IMAGE</th>");
            out.println("<th>PRICE</th>");
            out.println("<th>QTY</th>");
            out.println("<th>REMOVE</th></tr>");

            float totalPrice = 0f;
            for (CartItem item : cart.getItems()) {
               int id = item.getId();
               String category = item.getCategory();
               String title = item.getTitle();
               float price = item.getPrice();
               int qtyOrdered = item.getQtyOrdered();
               String img_url = item.getImg_url();

               out.println("<tr>");
               out.println("<td>" + category + "</td>");
               out.println("<td>" + title + "</td>");
               out.println("<td><img src='" + img_url + "' alt='Toy Image' height='100' width='100'/></td>"); // Displaying
                                                                                                              // the
                                                                                                              // image
               out.println("<td>$" + price + "</td>");

               // update button
               out.println("<td><form method='get'>");
               out.println("<input type='hidden' name='todo' value='update' />");
               out.println("<input type='hidden' name='id' value='" + id + "' />");
               out.println("<input type='text' size='3' name='qty" + id + "' value='" + qtyOrdered + "' />");
               out.println("<input id=add type='submit' value='Update' />");
               out.println("</form></td>");

               // remove button
               out.println("<td><form method='get'>");
               out.println("<input type='hidden' name='todo' value='remove' />");
               out.println("<input type='hidden' name='id' value='" + id + "' />");
               out.println("<input id=remove type='submit' value='Remove' />");

               out.println("</form></td>");
               out.println("</tr>");
               totalPrice += price * qtyOrdered;
            }
            out.println("<tr><td colspan='6' align='right'>Total Price: $");
            out.printf("%.2f</td></tr>", totalPrice);
            out.println("</table>");
         }

         out.println("<p class=link><a href='start'>Select More Toys...</a></p>");

         // session must exist, as it is created above
         User user;
         synchronized (session) {
            user = (User) session.getAttribute("user");
            // Display the Checkout
            if (user == null) { // if not login
               if (!cart.isEmpty()) {
                  out.println("<p class=link>Please login before checking out</p>");
               }
            } else { // if logined in show checkout button
               out.println("<form method='get' action='checkout'>");
               out.println("<input type='submit' value='CHECK OUT'>");
               out.println("</form>");
            }

            if (user != null) { // have user logined, show log out button
               out.println("<form class='user' method='get' action='logout' >");
               out.println("<p>Currently signed in as " + user.getUsername() + "</p>");
               out.println("<input type='submit' value='Log out' />");
               out.println("</form>");
            } else { // no user logined, show login button
               out.println("<form class='user' method='get' action='user.html' >");
               out.println("<p>Currently not signed in ...</p>");
               out.println("<input type='submit' value='Sign in' />");
               out.println("</form>");
            }
         }

         // Display the Checkout

         out.println("</body></html>");

      } catch (SQLException ex) {
         out.println("<h3 class=msg>Service not available. Please try again later!</h3></body></html>");
         Logger.getLogger(CartServlet.class.getName()).log(Level.SEVERE, null, ex);
      } finally {
         out.close();
         try {
            if (stmt != null)
               stmt.close();
            if (conn != null)
               conn.close(); // return the connection to the pool
         } catch (SQLException ex) {
            Logger.getLogger(CartServlet.class.getName()).log(Level.SEVERE, null, ex);
         }
      }
   }

   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException {
      doGet(request, response);
   }
}