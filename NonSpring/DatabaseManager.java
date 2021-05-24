import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
//Sql Imports
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseManager {

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


    public void InsertCrawlerURL(String url) throws SQLException {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT IGNORE INTO crawlerURLs (url, selectStatus, lastCrawled) VALUES(?,?,?)");
            Date date = new Date();
            java.sql.Timestamp sqlTime = new java.sql.Timestamp(date.getTime()-100);
            ps.setString(1, url);
            ps.setBoolean(2, Scheduler.schedulingStatus); 
            ps.setTimestamp(3, sqlTime); //We never crawled it, so we set the crawling time to oldest date so that we make sure to crawl, for now
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
                    .prepareStatement("SELECT url FROM crawlerURLs WHERE crawlerURLs.selectStatus = ? ORDER BY lastCrawled DESC LIMIT 1");
            ps.setBoolean(1, Scheduler.schedulingStatus);
            try {
                ResultSet rs = ps.executeQuery();
                if (rs.next() == false)
                    return "";
                String url = rs.getString("url");
                ps = conn
                        .prepareStatement("UPDATE crawlerURLs SET selectStatus = ?, lastCrawled = ? WHERE url = ?");
                Date date = new Date();
                java.sql.Timestamp sqlTime = new java.sql.Timestamp(date.getTime());
                ps.setBoolean(1, !Scheduler.schedulingStatus);
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