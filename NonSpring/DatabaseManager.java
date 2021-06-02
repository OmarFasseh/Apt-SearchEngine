import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

//Sql Imports
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class DatabaseManager {

    public enum URLState {
        NotCrawled, Crawling, Crawled, Indexing, Indexed
    }

    // The different database connections
    Connection conn = null;
    private int numberOfCrawledPages = 0;
    private int numberOfPages = 0;
    private int LIMIT = 100;

    public int getNumberOfCrawledPages() {
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

    public void init() throws SQLException {
        this.ResetCrawlerURLSStatus();
        this.loadSeeds();
        this.initNumberOfCrawledPages();
    }

    private void initNumberOfCrawledPages() {
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

    private void initNumberOfPages() {
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

    private void loadSeeds() {
        try {
            File websitesFile = new File("websites.txt");
            Scanner reader = new Scanner(websitesFile);
            LinkedList<String> urlsToAdd = new LinkedList<String>();
            while (reader.hasNextLine()) {
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

    public void InsertCrawlerURLS(LinkedList<String> urls) throws SQLException {
        try {
            if (urls.size() == 0)
                return;
            String query = "INSERT IGNORE INTO crawlerURLs (url, selectStatus, lastCrawled) VALUES"
                    + "(?,?,?),".repeat(urls.size() - 1) + "(?,?,?)";
            PreparedStatement ps = conn.prepareStatement(query);
            Date date = new Date();
            java.sql.Timestamp sqlTime = new java.sql.Timestamp(date.getTime() - 100);
            for (int i = 0; i < 3 * urls.size(); i += 3) {
                String url = urls.get(i / 3).toLowerCase();

                ps.setString(i + 1, url);
                ps.setInt(i + 2, URLState.NotCrawled.ordinal());
                ps.setTimestamp(i + 3, sqlTime); // We never crawled it, so we set the crawling time to oldest date so
                                                 // that we make sure to crawl, for now
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

    public void UpdateCrawlerURLSStatus(LinkedList<String> urls, URLState state, LinkedList<String> fileNames)
            throws SQLException {
        try {
            if (urls.size() == 0)
                return;
            String query = "INSERT INTO crawlerURLs (url, selectStatus, lastCrawled,fileName) VALUES"
                    + "(?,?,?,?),".repeat(urls.size() - 1) + "(?,?,?,?)"
                    + "ON DUPLICATE KEY UPDATE selectStatus=VALUES(selectStatus),fileName=VALUES(fileName)";
            // set selectStatus = ? " + "WHERE" + " url = ? or".repeat(urls.size() - 1)
            // + " url = ? ";
            PreparedStatement ps = conn.prepareStatement(query);

            Date date = new Date();
            java.sql.Timestamp sqlTime = new java.sql.Timestamp(date.getTime() - 100);
            for (int i = 0; i < 4 * urls.size(); i += 4) {
                String url = urls.get(i / 4).toLowerCase();

                ps.setString(i + 1, url);
                ps.setInt(i + 2, state.ordinal());
                ps.setTimestamp(i + 3, sqlTime);

                String fileName = "";
                if (fileNames != null) {
                    fileName = fileNames.get(i / 4);
                }
                ps.setString(i + 4, fileName);
            }
            // ps.setInt(1, state.ordinal());
            // System.out.println("Thread " + Thread.currentThread().getName() + " Updating
            // " + urls.size() + " pages");
            // for (int i = 0; i < urls.size(); i++) {
            // String url = urls.get(i);

            // ps.setString(i + 2, url , fileNames);
            // }
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
            if (numberOfCrawledPages >= LIMIT) {
                System.out.println(Thread.currentThread().getName() + " Will exit ");

                return null;
            }
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT url FROM crawlerURLs WHERE crawlerURLs.selectStatus = ? ORDER BY lastCrawled ASC LIMIT 10");
            ps.setInt(1, URLState.NotCrawled.ordinal());
            try {
                ResultSet rs = ps.executeQuery();
                LinkedList<String> urls = new LinkedList<String>();
                while (rs.next()) {
                    urls.add(rs.getString("url"));
                }
                this.UpdateCrawlerURLSStatus(urls, URLState.Crawling, null);
                numberOfCrawledPages += urls.size();

                System.out.println("Thread " + Thread.currentThread().getName() + " Fetched " + urls.size() + " pages");
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

    public void UpdateIndexerFileNameStatus(LinkedList<String> fileNames, URLState state) {
        try {
            if (fileNames.size() == 0)
                return;
            String query = "UPDATE crawlerURLs set selectStatus = ? " + "WHERE"
                    + " fileName = ? or".repeat(fileNames.size() - 1) + " fileName = ? ";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, state.ordinal());
            String stateString;
            if (state == URLState.Indexing) {
                stateString = "Indexing";
            } else if (state == URLState.Indexed) {
                stateString = "Indexed";
            } else {
                stateString = "Unknown";
            }
            System.out.println("Indexer updating " + fileNames.size() + " pages to state " + stateString);
            for (int i = 0; i < fileNames.size(); i++) {
                String file = fileNames.get(i);
                ps.setString(i + 2, file);
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

    public void GetIndexerTopFileNames(LinkedList<String> fileNames, LinkedList<String> urls) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT url,fileName FROM crawlerURLs WHERE crawlerURLs.selectStatus = 2 ORDER BY lastCrawled ASC ");
            try {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    urls.add(rs.getString("url"));
                    fileNames.add(rs.getString("fileName"));
                }
                this.UpdateIndexerFileNameStatus(fileNames, URLState.Indexing);
                System.out.println("Indexer Fetched " + fileNames.size() + " files");
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void InsertIndexerURLS(String url, Hashtable<String, Integer> fileWordsFrequency) {
        try {
            if (fileWordsFrequency.size() == 0)
                return;
            String query = "INSERT IGNORE INTO indexedUrls (url, word, count) VALUES"
                    + "(?,?,?),".repeat(fileWordsFrequency.size() - 1) + "(?,?,?)";
            PreparedStatement ps = conn.prepareStatement(query);

            int i = 0;
            for (Map.Entry<String, Integer> e : fileWordsFrequency.entrySet()) {
                ps.setString(i + 1, url);
                ps.setString(i + 2, e.getKey());
                ps.setInt(i + 3, e.getValue());
                i += 3;
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
}
