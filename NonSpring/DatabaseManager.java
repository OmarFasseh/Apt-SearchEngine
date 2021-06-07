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
    private int LIMIT = 5000;
    LinkedList<String> intialSeeds = new LinkedList<String>();
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
        this.initNumberOfPages();
    }
    private void initNumberOfCrawledPages() {
        try {
            String query = "SELECT COUNT(*) As rowCount FROM crawlerURLs WHERE selectStatus >= 2";
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
            while (reader.hasNextLine()) {
                String website = reader.nextLine();
                LinkedList<String> urlsToAdd = new LinkedList<String>();
                urlsToAdd.push(website);
                this.InsertCrawlerURLS(urlsToAdd);
                Thread.sleep(1000);
            }
            
            reader.close();
        } catch (Exception e) {
            System.out.println("An exception occured while reading the initial websites list!");
            e.printStackTrace();
        }
    }

    public int InsertCrawlerURLS(LinkedList<String> urls) throws SQLException {
        try {
            if (urls.size() == 0)
                return 0;
            synchronized(this){
                if(this.numberOfPages >= LIMIT+LIMIT) return 0;
            }
            String query = "INSERT IGNORE INTO crawlerURLs (url, selectStatus, lastCrawled) VALUES"
                    + "(?,?,?),".repeat(urls.size() - 1) + "(?,?,?)";
            PreparedStatement ps = conn.prepareStatement(query);
            Date date = new Date();
            java.sql.Timestamp sqlTime = new java.sql.Timestamp(date.getTime() - 100);
            for (int i = 0; i < 3 * urls.size(); i += 3) {
                String url = urls.get(i / 3);

                ps.setString(i + 1, url);
                ps.setInt(i + 2, URLState.NotCrawled.ordinal());
                ps.setTimestamp(i + 3, sqlTime); // We never crawled it, so we set the crawling time to oldest date so
                                                 // that we make sure to crawl, for now
            }
            try {
                int rowsInserted = ps.executeUpdate();
                synchronized(this){
                    this.numberOfPages+= rowsInserted;
                }
                return rowsInserted;
                // this.notifyAll();
            } catch (SQLException e) {
                System.out.println("Error in Insert");
            }

        } catch (SQLException e) {
            System.out.println("Error in Insert");
            
        }
        return 0;
    }

    public void UpdateCrawlerURLSStatus(LinkedList<String> urls, URLState state, LinkedList<String> fileNames, LinkedList<String> titles)
            throws SQLException {
        try {
            if (urls.size() == 0)
                return;
            String query = "INSERT INTO crawlerURLs (url, selectStatus, lastCrawled,fileName,title) VALUES"
                    + "(?,?,?,?,?),".repeat(urls.size() - 1) + "(?,?,?,?,?)"
                    + "ON DUPLICATE KEY UPDATE selectStatus=VALUES(selectStatus),fileName=VALUES(fileName), title = VALUES(title)";
            // set selectStatus = ? " + "WHERE" + " url = ? or".repeat(urls.size() - 1)
            // + " url = ? ";
            PreparedStatement ps = conn.prepareStatement(query);

            Date date = new Date();
            java.sql.Timestamp sqlTime = new java.sql.Timestamp(date.getTime() - 100);
            for (int i = 0; i <5 * urls.size(); i += 5) {
                String url = urls.get(i / 5);

                ps.setString(i + 1, url);
                ps.setInt(i + 2, state.ordinal());
                ps.setTimestamp(i + 3, sqlTime);

                String fileName = "";
                String title = "";
                if (fileNames != null) {
                    fileName = fileNames.get(i / 5);
                }
                if(titles != null){
                    title = titles.get(i/5);
                }
                ps.setString(i + 4, fileName);
                ps.setString(i + 5, title);
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
                if(state == URLState.Crawled){
                    synchronized(this){
                        this.numberOfCrawledPages+= urls.size();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in Update: "+ urls.size());
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
                this.UpdateCrawlerURLSStatus(urls, URLState.Crawling, null,null);
               

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

    public void GetIndexerTopFileNames(LinkedList<String> fileNames, LinkedList<String> urls,LinkedList<String> titles) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT url,fileName,title FROM crawlerURLs WHERE crawlerURLs.selectStatus = 2 OR crawlerURLs.selectStatus =3 ORDER BY lastCrawled ASC ");
            try {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    if(rs.getString("fileName").equals("")){
                        continue;
                     }
                    urls.add(rs.getString("url"));
                    fileNames.add(rs.getString("fileName"));
                    titles.add(rs.getString("title")); 
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

    public void InsertIndexerURLS(String url, Hashtable<String, Integer> fileWordsFrequency,
            Hashtable<String, String> snipTable,String title) {
        try {
            int wordsCount = fileWordsFrequency.size();
            if (wordsCount == 0)
                return;
            String query = "INSERT IGNORE INTO indexedUrls (url, word, tf , snippet, title) VALUES"
                    + "(?,?,?,?,?),".repeat(fileWordsFrequency.size() - 1) + "(?,?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(query);

            int i = 0;
            for (Map.Entry<String, Integer> e : fileWordsFrequency.entrySet()) {
                ps.setString(i + 1, url);
                ps.setString(i + 2, e.getKey());
                ps.setFloat(i + 3, (float)e.getValue() / wordsCount);
                // if((float)e.getValue() / wordsCount==1 ){
                //     System.out.println("count is "+wordsCount);
                // }
                ps.setString(i + 4, snipTable.get(e.getKey()));
                ps.setString(i+5, title);
                i += 5;
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

    public void UpdateIndexerIDF(Hashtable<String, Integer> wordFoundCount, int docCount) {
        try {
            if (wordFoundCount.size() == 0)
                return;
            String query = "INSERT INTO idftable    (word, idf) VALUES" + "(?,?),".repeat(wordFoundCount.size() - 1)
                    + "(?,?)" + "ON DUPLICATE KEY UPDATE word=VALUES(word),idf=VALUES(idf)";
            PreparedStatement ps = conn.prepareStatement(query);
            int i=0;
            for (Map.Entry<String, Integer> e : wordFoundCount.entrySet()) {
                ps.setString(i + 1, e.getKey());
                double idf =  Math.log(docCount/(double)e.getValue());
                ps.setDouble(i + 2, idf);
                i+=2;
            }
            try {
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (

        SQLException e) {
            e.printStackTrace();
        }
    }
}
