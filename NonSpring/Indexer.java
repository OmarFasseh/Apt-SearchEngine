import java.io.File;
import java.util.HashSet;
import java.util.Scanner;

import opennlp.tools.stemmer.PorterStemmer;

public class Indexer {
    public HashSet<String> stopWords;

    public Indexer() {
        stopWords = new HashSet<String>();
    }

    public static void main(String[] args) {

        PorterStemmer porterStemmer = new PorterStemmer();
        for (String word : words) {
            word = porterStemmer.stem(word);
            manageHashTables(hTable, word);
        }
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
}