import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;

//JSoup imports
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler extends Thread {
    // Number of pages downloaded so far
    int pagesCount;
    DatabaseManager dbManager;

    // Constructor
    Crawler(DatabaseManager dbManager_) {
        dbManager = dbManager_;
        pagesCount = dbManager_.getNumberOfCrawledPages();
    }

    // Crawls the given url, downloads the page and adds to the db all urls found
    public void run() {

        do {
            LinkedList<String> urlsToCrawl = dbManager.GetCrawlerTopURLs();
            LinkedList<String> fileNames = new LinkedList<String>();
            LinkedList<String> titles = new LinkedList<String>();
            // Already Reached Crawling limit
            if (urlsToCrawl == null)
                break;
            // No Site to Crawal
            else if (urlsToCrawl.size() == 0) {
                try {
                    Thread.sleep(new Random().nextInt(10000));
                    continue;
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                continue;
            }
            LinkedList<String> urlsCrawled = new LinkedList<String>();
            for (int i = 0; i < urlsToCrawl.size(); i++) {
                LinkedList<String> urlsToAdd = new LinkedList<String>();


                String urlString = urlsToCrawl.get(i);
                try {
                    Document doc = Jsoup.connect(urlString).get();
                    String title = doc.title().substring(0, doc.title().length()>100?100:doc.title().length());
                    titles.add(title);
                    // System.out.println("Downloading "+Thread.currentThread().getName() +
                    // urlString);
                    // Download the page
                    String fileName = "WebPages/" + Thread.currentThread().getName() + "page" + (pagesCount) + ".txt";
                    BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
                    pagesCount++;
                    writer.write(doc.body().text());
                    writer.flush();
                    writer.close();
                    // Extract URLs
                    Elements hrefs = doc.select("a");

                    for (Element href : hrefs) {
                        String absLink = href.attr("abs:href");
                        if (absLink.length() == 0)
                            continue;
                        URL temp = new URL(absLink);

                        if (!temp.getProtocol().equals("http") && !temp.getProtocol().equals("https"))
                            continue;
                        String path = temp.getPath();
                        if (path == null || path.isEmpty() || path.equals("/index.html") || path.equals("/#"))
                            path = "/";
                        URI link = new URI(temp.getProtocol(), temp.getUserInfo(), temp.getHost().toLowerCase(), temp.getPort(), path,
                                temp.getQuery(), temp.getRef());
                        if (!RobotManager.isAllowed(link.toURL())) {
                            continue;
                        }
                        // System.out.println(absLink+ " Allowed");
                        String url = link.toASCIIString();
                        urlsToAdd.add(url);
                        if(urlsToAdd.size()>=400)
                            break;
                    }
                    fileNames.add(fileName);
                    urlsCrawled.add(urlString);
                    dbManager.InsertCrawlerURLS(urlsToAdd);

                    // System.out.println(urlString + " Downloaded");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("An exception occured while crawling " + urlString + " web page!");
                   
                }
            }
            try {
                dbManager.UpdateCrawlerURLSStatus(urlsCrawled, DatabaseManager.URLState.Crawled,fileNames,titles);

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } while (true);
    }
}