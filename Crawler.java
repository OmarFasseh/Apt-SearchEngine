import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;  
import java.util.Scanner;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler
{
    

    static ArrayList<String> crawlList;
    static int pagesCount;
    public static void InitializeCrawler() throws IOException
    {
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

    
    public static void main(String args[]) throws IOException
    {
        Crawler.InitializeCrawler();
        while (Crawler.crawlList.size()!=0)
        {
            Download(crawlList.remove(0));
        }
    }

    public static void Download(String urlString) throws IOException
    {
        System.out.println("Downloading " + urlString);
        Document doc = Jsoup.connect(urlString).get();
        int i = 0;
        try {
            //Download the page
            BufferedWriter writer = new BufferedWriter(new FileWriter("page"+(pagesCount)+".html"));
            pagesCount++;
            writer.write(doc.toString());
            writer.flush();
            writer.close();
            //Extract URLs
            Elements hrefs = doc.select("a");
            for (Element href : hrefs)
            {
                String absLink = href.attr("abs:href");
                crawlList.add(absLink);
                i++;
                if(i > 50)
                    break;
            }
            System.out.println(urlString + " Downloaded");
        } catch (Exception e) {
            System.out.println("An exception occured while crawling " + urlString + " web page!");
        }
        
    }
    
} 