import java.io.IOException;
import java.sql.SQLException;


//Scheduler class to select the next appropriate url to visit
public class Scheduler
{
    
    DatabaseManager dbManager;
    
    Scheduler(DatabaseManager dbManager_)
    {
        dbManager = dbManager_;
    }

    //Get the next scheduled website
    public String GetNextWebsite()
    {
        return dbManager.GetCrawlerTopURL();
    }

    //Gets the time the crawler should sleep, as crawler shouldn't be working forever
    //For now it returns 0
    public float SleepTime()
    {
        return 0;
    }
}