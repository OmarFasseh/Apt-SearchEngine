package com.noodle.search_engine;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;  
import java.util.Scanner;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
//JSoup imports
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler
{
    //Number of pages downloaded so far
    int pagesCount;
    DatabaseManager dbManager;
    //Constructor
    Crawler(DatabaseManager dbManager_)
    {
        dbManager = dbManager_;
    }

    //Adds the initial crawling list to the database
    public void InitializeCrawler()
    {
        //Read initial list from websites.txt file
        try {
            File websitesFile = new File("websites.txt");
            Scanner reader = new Scanner(websitesFile);
            while(reader.hasNextLine())
            {
                String website = reader.nextLine();
                dbManager.InsertCrawlerURL(website);
            }
            reader.close();
        } catch (Exception e) {
            System.out.println("An exception occured while reading the initial websites list!");
            e.printStackTrace();
        }
        
    }


    //Crawls the given url, downloads the page and adds to the db all urls found
    public void Crawl(String urlString)
    {
        try {
            Document doc = Jsoup.connect(urlString).get();
            System.out.println("Downloading " + urlString);
            int i = 0;
            //Download the page
            BufferedWriter writer = new BufferedWriter(new FileWriter("WebPages/page" + (pagesCount) + ".html"));
            pagesCount++;
            writer.write(doc.toString());
            writer.flush();
            writer.close();
            //Extract URLs
            Elements hrefs = doc.select("a");
            for (Element href : hrefs) {
                String absLink = href.attr("abs:href");
                dbManager.InsertCrawlerURL(absLink);
                i++;
                if (i > 50)
                    break;
            }
            System.out.println(urlString + " Downloaded");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("An exception occured while crawling " + urlString + " web page!");
            e.printStackTrace();
        }
    }
}