import java.io.IOException;
//Sql Imports
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseManager
{
    
    String dbDir;
    Connection conn = null;
    DatabaseManager()
    {
        //Initialize the directory for the database
        dbDir = System.getProperty("user.dir");
        dbDir = dbDir.concat("/db/");
    }
    
    public void main(String args[]) throws IOException
    {
        CreateDB();
    }

    public void CreateDB()
    {
        CreateCrawlerDB();
    }

    public void CreateCrawlerDB() {
        try {
            String url = "jdbc:sqlite:" + dbDir + "crawler.db";
            conn = DriverManager.getConnection(url);
            Statement stmt = null;
            String query = "CREATE TABLE IF NOT EXISTS crawlerURLs (\n"
            + "	url varchar(150) PRIMARY KEY,\n"
            + "	lastCrawled DATETIME NOT NULL\n"
            + ");";
            try {
                stmt = conn.createStatement();
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
    
}