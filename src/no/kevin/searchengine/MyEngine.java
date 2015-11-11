package no.kevin.searchengine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class MyEngine implements SearchEngine {

    private static final int DEFAULT_SIZE = 5000;

    private Webcrawler webcrawler;

    public MyEngine(){ // DONE
        this(DEFAULT_SIZE);
    }

    public MyEngine(int theMax) { // DONE
        webcrawler = new Webcrawler(theMax);
        readWordsToIndex();
    }

    public void setMax(int theMax){ // DONE
        webcrawler.resize(theMax);
    }
    
    public boolean setBreadthFirst(){
        webcrawler.setEngine(Webcrawler.WidthFirstCrawlerEngine.class);
        return true;
    }

    public boolean setDepthFirst(){
        webcrawler.setEngine(Webcrawler.DepthFirstCrawlerEngine.class);
        return true;
    }

    public void crawlFrom(String webAdress){ // TODO
        webcrawler.crawl(webAdress);
    }

    public String[] searchHits(String target){ // TODO
        return webcrawler.searchHits(target);
    }

    public int size(){ // DONE
        return webcrawler.getSize();
    }


    private void readWordsToIndex()
    {
        try
        {
            List<String> blacklist = Files.readAllLines(Paths.get("stopwords.txt"));
            webcrawler.addWordsToIndex(Files.lines(Paths.get("words.txt")).filter(word -> !blacklist.contains(word)).toArray(String[]::new));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
