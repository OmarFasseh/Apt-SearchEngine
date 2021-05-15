import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;  
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

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
        URL url = new URL(urlString);
        int i = 0;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            BufferedWriter writer = new BufferedWriter(new FileWriter("page"+(pagesCount)+".html"));
            pagesCount++;
            String line;
            //Check for all urls mentioned in page and add them to the crawl list
            String regex = "\\b((?:https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:, .;]*[-a-zA-Z0-9+&@#/%=~_|])";
            //Compile regex
            Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            while((line = reader.readLine()) != null)
            {
                writer.write(line);   
                //Match between string and regex
                Matcher m = p.matcher(line);
                // Find the next occurence of url in the pattern
                while (m.find()) {
                    crawlList.add(line.substring(m.start(0), m.end(0)));
                    i++;
                    if (i >= 50) //break if at same doc a lot
                        break;
                }
                if (i >= 50)
                    break;
            }
            writer.flush();
            writer.close();
            System.out.println(url + " Downloaded");
        } catch (Exception e) {}
        
    }
    
}
