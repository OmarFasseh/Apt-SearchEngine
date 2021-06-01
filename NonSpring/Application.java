import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;


//Main application class
public class Application
{
    
    public static void main(String args[]) throws IOException, SQLException
    {

        
        //Creaate the database manager, crawler, scheduler, indexer, etc
        DatabaseManager dbManager = new DatabaseManager();

        
        dbManager.InitializeConnection();
        dbManager.init();

        //Get Number of Threads
        System.out.println("Enter Number of Threads:");
        BufferedReader in = new BufferedReader (new InputStreamReader(System.in));
        int numberOfThreads= Integer.parseInt(in.readLine()); // for taking a number as an input 
        
        for( int i=0;i<numberOfThreads;i++){
            Crawler thread = new Crawler(dbManager);
            thread.setName(Integer.toString(i));
            thread.start();
        }
        try {
            Thread.sleep(40000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Indexer indexer = new Indexer(dbManager);
        indexer.Index();
        
        //Let the crawler begin
        // while(scheduler.SleepTime()==0)
        // {
        //     String nextWebsite = scheduler.GetNextWebsite();
        //     if (nextWebsite.length()==0)
        //         break;            
        //     cra-wler.Crawl(nextWebsite);
        // }
    }

    
}