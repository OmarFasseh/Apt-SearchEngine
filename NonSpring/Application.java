import java.io.IOException;
import java.sql.SQLException;


//Main application class
public class Application
{
    
    public static void main(String args[]) throws IOException, SQLException
    {
        //Creaate the database manager, crawler, scheduler, indexer, etc
        DatabaseManager dbManager = new DatabaseManager();
        Crawler crawler = new Crawler(dbManager);
        Scheduler scheduler = new Scheduler(dbManager);

        dbManager.InitializeConnection();

        //Let the crawler begin
        crawler.InitializeCrawler();
        while(scheduler.SleepTime()==0)
        {
            String nextWebsite = scheduler.GetNextWebsite();
            if (nextWebsite.length()==0)
                break;            
            crawler.Crawl(nextWebsite);
        }
    }

    
}