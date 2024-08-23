package mypkg;

import java.io.*;
import java.sql.*;
import java.util.logging.*;
import javax.naming.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import javax.sql.DataSource;

public class RegisterServlet extends HttpServlet {

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
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Set the MIME type for the response message
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Connection conn = null;
        Statement stmt = null;
        try {
            conn = pool.getConnection(); // Get a connection from the pool
            stmt = conn.createStatement();

            out.println("<!DOCTYPE html><html><head>");
            out.println("<link rel='stylesheet' href='css/style.css' type='text/css' />");
            out.println("<title>User Registered</title></head><body>");

            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String phonenum = request.getParameter("phonenum");
            String email = request.getParameter("email");
            String address = request.getParameter("address");
            String sqlStr = "INSERT INTO users VALUES(0, '";

            // TODO: input validation
            sqlStr = sqlStr + username + "', '" + password + "', '" + phonenum + "', '" + email + "', '"+ address + "')";

            // out.println("<p>Your SQL statement is: " + sqlStr + "</p>"); // Echo for debugging
            out.println("<p class=msg>User Registered Successfully</p>");
            out.println("<p class=link><a href='user.html'>Go to Login</a></p>");
            stmt.execute(sqlStr); // Send the query to the server

        } catch (Exception ex) {
            out.println("<p>Error: " + ex.getMessage() + "</p>");
            out.println("<p>Check Tomcat console for details.</p>");
            ex.printStackTrace();
        } // Step 5: Close conn and stmt - Done automatically by try-with-resources (JDK
          // 7)

        out.println("</body></html>");
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}