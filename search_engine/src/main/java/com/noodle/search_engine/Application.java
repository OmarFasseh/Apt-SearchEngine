package com.noodle.search_engine;

import java.io.IOException;
import java.sql.SQLException;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

//Main application class
@Component
public class Application implements CommandLineRunner 
{
    
    @Override
    public void run(String... args) throws IOException, SQLException
    {
        main(args);
    }

    public static void main(String[] args)
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