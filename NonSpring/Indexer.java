import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.StringTokenizer;

import java.util.Iterator;

import opennlp.tools.stemmer.PorterStemmer;

public class Indexer {
    public HashSet<String> stopWords;
    DatabaseManager dbManager;

    public Indexer(DatabaseManager dbManager_) {
        dbManager = dbManager_;
        stopWords = new HashSet<String>();
        getStopWords();
    }

    public void Index() {

       

        // do {
        System.out.println("Index is called");
        LinkedList<String> fileNames = new LinkedList<String>();
        LinkedList<String> urls = new LinkedList<String>();
        dbManager.GetIndexerTopFileNames(fileNames, urls);
        Iterator it = urls.iterator();
        for (String fileName : fileNames) {
            BufferedReader reader;
            Hashtable<String, Integer> fileWordsFrequency = new Hashtable<String, Integer>();
            try {
                // read file
                reader = new BufferedReader(new FileReader(fileName));
                String strCurrentLine;
                // get every line
                while ((strCurrentLine = reader.readLine()) != null) {
                    StringTokenizer tk = new StringTokenizer(strCurrentLine);
                    String[] words = strCurrentLine.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
                    for (String word : words) {
                    
                    // get every word per line and seperate on spaces
                    //while (tk.hasMoreTokens()) {
                        // String word = tk.nextToken();
                        addWordToTable(fileWordsFrequency, word);
                    }
                }
                reader.close();
                if(!it.hasNext()){
                    throw new IllegalStateException();
                }
                String url = (String) it.next();
                dbManager.InsertIndexerURLS(url, fileWordsFrequency);
            } catch (FileNotFoundException e) {
                System.out.println("An exception occured while opening " + fileName + " to index!");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("An exception occured while reading from " + fileName + " to index!");
                e.printStackTrace();
            }
        }
        dbManager.UpdateIndexerFileNameStatus(fileNames, DatabaseManager.URLState.Indexed);
        // }while(true)
    }

    void getStopWords() {
        try {
            File websitesFile = new File("stop_words_english.txt");
            Scanner reader = new Scanner(websitesFile);
            while (reader.hasNextLine()) {
                String word = reader.nextLine();
                stopWords.add(word);
            }
            reader.close();
        } catch (Exception e) {
            System.out.println("An exception occured while reading the stop words!");
            e.printStackTrace();
        }
    }

    void addWordToTable(Hashtable<String, Integer> table, String word) {
        // add or increment frequency of word IF it's not a stop word
        PorterStemmer porterStemmer = new PorterStemmer();
        word = porterStemmer.stem(word);
        if (!stopWords.contains(word)) {
            if (table.containsKey(word))
                table.replace(word, table.get(word) + 1);
            else
                table.put(word, 1);
        }
    }
}