package mypkg;

import java.io.*;
import java.sql.*;
import java.util.logging.*;
import javax.naming.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import javax.sql.DataSource;

public class LogoutServlet extends HttpServlet {

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

            // end session to log out
            HttpSession session=request.getSession();  
            session.invalidate();  
            
            out.println("<p>You are successfully logged out!</p>"); 
            out.println("<p class=link><a href='start'>Go to EToyShop</a></p>"); 
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