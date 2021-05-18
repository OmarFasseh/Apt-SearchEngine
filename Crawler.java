import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;  
import java.util.Scanner;
import java.io.IOException;
import java.util.*;
//JSoup imports
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler
{
    

    ArrayList<String> crawlList;
    int pagesCount;
    String dbDir;
    public void InitializeCrawler() throws IOException
    {
        //Initialize the directory for the database
        dbDir = System.getProperty("user.dir");
        dbDir = dbDir.concat("/db/");
        crawlList = new ArrayList<String>();
        //Read initial list from websites.txt file
        try {
            File websitesFile = new File("websites.txt");
            Scanner reader = new Scanner(websitesFile);
            while(reader.hasNextLine())
            {
                String website = reader.nextLine();
                crawlList.add(website);
            }
            reader.close();
        } catch (Exception e) {
            System.out.println("An exception occured while reading the initial websites list!");
        }
    }

    
    public void Crawl() throws IOException
    {
        while (crawlList.size()!=0)
        {
            Download(crawlList.remove(0));
        }
    }

    public void Download(String urlString) throws IOException
    {
        System.out.println("Downloading " + urlString);
        Document doc = Jsoup.connect(urlString).get();
        int i = 0;
        try {
            //Download the page
            BufferedWriter writer = new BufferedWriter(new FileWriter("page" + (pagesCount) + ".html"));
            pagesCount++;
            writer.write(doc.toString());
            writer.flush();
            writer.close();
            //Extract URLs
            Elements hrefs = doc.select("a");
            for (Element href : hrefs) {
                String absLink = href.attr("abs:href");
                crawlList.add(absLink);
                i++;
                if (i > 50)
                    break;
            }
            System.out.println(urlString + " Downloaded");
        } catch (Exception e) {
            System.out.println("An exception occured while crawling " + urlString + " web page!");
        }

    }
        
}