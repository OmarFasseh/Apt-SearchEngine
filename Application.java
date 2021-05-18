import java.io.IOException;

public class Application
{
    
    public static void main(String args[]) throws IOException
    {
        DatabaseManager dbManager = new DatabaseManager();
        Crawler crawler = new Crawler();
      
        dbManager.CreateDB();
        crawler.InitializeCrawler();
        crawler.Crawl();
    }

    
}