import java.io.IOException;
import java.util.Date;
//Sql Imports
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseManager {

    //The directory of the databse folder
    String dbDir;
    //The different database connections
    Connection crawlerConnection = null;

    DatabaseManager()

    {
        //Initialize the directory for the database
        dbDir = System.getProperty("user.dir");
        dbDir = dbDir.concat("/db/");
    }

    public void main(String args[]) throws IOException, SQLException {
        CreateDB();
    }

    public void InitializeConnection(Connection conn, String dbName) throws SQLException {
        try {
            String url = "jdbc:sqlite:" + dbDir + dbName + ".db";
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new Error("Problem", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public void InitializeConnections() throws SQLException {
        InitializeConnection(crawlerConnection, "crawler");
    }

    public void CreateDB() throws SQLException {
        CreateCrawlerDB();
    }

    public void CreateCrawlerDB() throws SQLException {
        try {
            String url = "jdbc:sqlite:" + dbDir + "crawler.db";
            crawlerConnection = DriverManager.getConnection(url);
            Statement stmt = null;
            String query = "CREATE TABLE IF NOT EXISTS crawlerURLs (\n" + "	url VARCHAR(150) PRIMARY KEY,\n"
                    + "	selectStatus BOOL NOT NULL,\n" + "	lastCrawled DATETIME NOT NULL\n" + ");";
            try {
                stmt = crawlerConnection.createStatement();
                stmt.execute(query);
            } catch (SQLException e) {
                throw new Error("Problem", e);
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }

        } catch (SQLException e) {
            throw new Error("Problem", e);
        }
    }

    public void InsertCrawlerURL(String url) throws SQLException {
        try {
            if(url.length()==0) return;
            Statement stmt = null;

            PreparedStatement ps = crawlerConnection.prepareStatement(
                    "INSERT INTO crawlerURLs (url, selectStatus, lastCrawled) VALUES(?,?,?) ON CONFLICT DO NOTHING");
            Date date = new Date();
            java.sql.Timestamp sqlTime = new java.sql.Timestamp(date.getTime()-100);
            ps.setString(1, url);
            ps.setBoolean(2, Scheduler.schedulingStatus); 
            ps.setTimestamp(3, sqlTime); //We never crawled it, so we set the crawling time to oldest date so that we make sure to crawl, for now
            try {
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new Error("Problem", e);
            }

        } catch (SQLException e) {
            throw new Error("Problem", e);
        }
    }

    public String GetCrawlerTopURL() {
        try {
            PreparedStatement ps = crawlerConnection
                    .prepareStatement("SELECT url FROM crawlerURLs WHERE crawlerURLs.selectStatus = ? ORDER BY lastCrawled DESC LIMIT 1");
            ps.setBoolean(1, Scheduler.schedulingStatus);
            try {
                ResultSet rs = ps.executeQuery();
                String url = rs.getString("url");
                ps = crawlerConnection
                        .prepareStatement("UPDATE crawlerURLs SET selectStatus = ?, lastCrawled = ? WHERE url = ?");
                Date date = new Date();
                java.sql.Timestamp sqlTime = new java.sql.Timestamp(date.getTime());
                ps.setBoolean(1, !Scheduler.schedulingStatus);
                ps.setTimestamp(2, sqlTime);
                ps.setString(3, url);
                ps.executeUpdate();

                return url;
            } catch (SQLException e) {
                throw new Error("Problem", e);
            }

        } catch (SQLException e) {
            throw new Error("Problem", e);
        }

    }
}