import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Date;

//Sql Imports
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;


public class DatabaseManager {


    public enum CrawlerState { 
        NotCrawled,
        Crawling,
        Crawled
        }
    //The different database connections
    Connection conn = null;
    private int numberOfCrawledPages = 0;
    private int numberOfPages = 0;
    private  int LIMIT = 100;
    public int  getNumberOfCrawledPages(){
        return numberOfCrawledPages;
    }
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
    public void init() throws SQLException{
        this.ResetCrawlerURLSStatus();
        this.loadSeeds();
        this.initNumberOfCrawledPages();
    }
    private void initNumberOfCrawledPages(){
        try {
            String query = "SELECT COUNT(*) As rowCount FROM crawlerURLs WHERE selectStatus = 2"; 
            PreparedStatement ps = conn.prepareStatement(query);
            try {
                
                ResultSet rs = ps.executeQuery(query);
                rs.next();
                numberOfCrawledPages = rs.getInt("rowCount");
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void initNumberOfPages(){
        try {
            String query = "SELECT COUNT(*) As rowCount FROM crawlerURLs"; 
            PreparedStatement ps = conn.prepareStatement(query);
            try {
                
                ResultSet rs = ps.executeQuery(query);
                rs.next();
                numberOfPages = rs.getInt("rowCount");
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void loadSeeds(){
        try {
            File websitesFile = new File("websites.txt");
            Scanner reader = new Scanner(websitesFile);
            LinkedList<String> urlsToAdd = new LinkedList<String>();
            while(reader.hasNextLine())
            {
                String website = reader.nextLine();
                urlsToAdd.push(website);
            }
            this.InsertCrawlerURLS(urlsToAdd);
            reader.close();
        } catch (Exception e) {
            System.out.println("An exception occured while reading the initial websites list!");
            e.printStackTrace();
        }
    }
    public  void InsertCrawlerURLS(LinkedList<String> urls) throws SQLException {
        try {
            if(urls.size() ==0) return;
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
                // this.notifyAll();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public  void UpdateCrawlerURLSStatus(LinkedList<String> urls,CrawlerState state) throws SQLException {
        try {
            if(urls.size() == 0) return;
            String query = "UPDATE crawlerURLs set selectStatus = ? " + "WHERE" + " url = ? or".repeat(urls.size()-1) + " url = ? "; 
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, state.ordinal());
            System.out.println("Thread "+Thread.currentThread().getName()+" Updating " +urls.size()+" pages");
            for (int i = 0; i < urls.size(); i++) {
                String url = urls.get(i);
                ps.setString(i+2, url);
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

    private void ResetCrawlerURLSStatus() throws SQLException {
        try {
            String query = "UPDATE crawlerURLs set selectStatus= 0 where selectStatus = 1"; 
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

    public synchronized LinkedList<String> GetCrawlerTopURLs() {
        try {
            if(numberOfCrawledPages >= LIMIT)
            {
                System.out.println(Thread.currentThread().getName()+" Will exit ");
                
                return null;
            }
            PreparedStatement ps = conn
                    .prepareStatement("SELECT url FROM crawlerURLs WHERE crawlerURLs.selectStatus = ? ORDER BY lastCrawled ASC LIMIT 10");
            ps.setInt(1, CrawlerState.NotCrawled.ordinal());
            try {
                ResultSet rs = ps.executeQuery();
                LinkedList<String> urls = new LinkedList<String>();
                while(rs.next()){
                    urls.add(rs.getString("url"));
                }
                this.UpdateCrawlerURLSStatus(urls, CrawlerState.Crawling);
                numberOfCrawledPages += urls.size();
                
                System.out.println("Thread "+Thread.currentThread().getName()+" Fetched " +urls.size()+" pages");
                return urls;
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            LinkedList<String> urls = new LinkedList<String>();
            return urls;
        }
        LinkedList<String> urls = new LinkedList<String>();
        return urls;
        
    }
}