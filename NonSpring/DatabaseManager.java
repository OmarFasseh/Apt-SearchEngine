import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Date;
//Sql Imports
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseManager {


    enum CrawlerState { 
        NotCrawled,
        Crawling,
        Crawled
        }
    //The different database connections
    Connection conn = null;

    public void InitializeConnection() {
        try {
            String url = "jdbc:mysql://localhost/noodle?user=noodleadmin&password=noodle123456789";
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void InsertCrawlerURLS(LinkedList<String> urls) throws SQLException {
        try {
            String query = "INSERT IGNORE INTO crawlerURLs (url, selectStatus, lastCrawled) VALUES" + "(?,?,?),".repeat(urls.size()-1) + "(?,?,?)"; 
            PreparedStatement ps = conn.prepareStatement(query);
            Date date = new Date();
            java.sql.Timestamp sqlTime = new java.sql.Timestamp(date.getTime()-100);
            for (int i = 0; i < 3*urls.size(); i+=3) {
                String url = urls.get(i/3).toLowerCase();
                ps.setString(i+1, url);
                ps.setInt(i+2, CrawlerState.NotCrawled.ordinal()); 
                ps.setTimestamp(i+3, sqlTime); //We never crawled it, so we set the crawling time to oldest date so that we make sure to crawl, for now                    
            }
            try {
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void UpdateCrawlerURLSStatus(LinkedList<String> urls) throws SQLException {
        try {
            String query = "UPDATE crawlerURLs set selectStatus = 2 " + "WHERE" + " url = ? or".repeat(urls.size()-1) + " url = ? "; 
            PreparedStatement ps = conn.prepareStatement(query);
            for (int i = 0; i < urls.size(); i++) {
                String url = urls.get(i).toLowerCase();
                ps.setString(i+1, url);
            }
            try {
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void ResetCrawlerURLSStatus() throws SQLException {
        try {
            String query = "UPDATE crawlerURLs set selectStatus 0 where selectStatus = 1"; 
            PreparedStatement ps = conn.prepareStatement(query);
            try {
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String GetCrawlerTopURL() {
        try {
            PreparedStatement ps = conn
                    .prepareStatement("SELECT url FROM crawlerURLs WHERE crawlerURLs.selectStatus = ? ORDER BY lastCrawled ASC LIMIT 1");
            ps.setInt(1, 0);
            try {
                ResultSet rs = ps.executeQuery();
                if (rs.next() == false)
                    return "";
                String url = rs.getString("url");
                ps = conn
                        .prepareStatement("UPDATE crawlerURLs SET selectStatus = ?, lastCrawled = ? WHERE url = ?");
                Date date = new Date();
                java.sql.Timestamp sqlTime = new java.sql.Timestamp(date.getTime());
                ps.setInt(1, CrawlerState.Crawling.ordinal());
                ps.setTimestamp(2, sqlTime);
                ps.setString(3, url);
                ps.executeUpdate();
                return url;
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "";
        }
        return "";
    }
}