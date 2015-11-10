package no.kevin.searchengine;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class Webcrawler
{
    private CrawlerEngine engine;
    private HashSet<String> visitedLinks = new HashSet<>();
    private HashMap<String, String[]> index = new HashMap<>();
    private int words;
    private int maxSize;

    public static void main(String[] args)
    {
        new Webcrawler().crawl("http://aftenposten.no/");
    }

    public Webcrawler()
    {
        this(Short.MAX_VALUE);
    }

    public Webcrawler(int maxSize)
    {
        resize(maxSize);
        engine = new WidthFirstCrawlerEngine();
    }

    public void addWordsToIndex(String... words)
    {
        Arrays.stream(words).forEach(str -> index.put(str, null));
    }

    public void resize(int newSize)
    {
        this.maxSize = newSize;
    }

    public void crawl(String url)
    {
        WebPageReader reader = new WebPageReader(url);
        engine.addAll(reader.getLinks());
        while (engine.hasNext() && words < maxSize)
        {
            readAllLinks(engine.next());
        }
        if (words >= maxSize)
        {
            engine = null;
            visitedLinks = null;
        }
    }

    private void readAllLinks(String url)
    {
        System.out.printf("(%5d/%5d) ", words, maxSize);
        System.out.printf("crawling: %s%n", url);

        WebPageReader reader = new WebPageReader(url);
        reader.run();
        visitedLinks.add(url);

        engine.addAll(reader.getLinks().stream()
                .filter(s -> !visitedLinks.contains(s))
                .collect(Collectors.toList()));

        for (String word : reader.getWords())
        {
            if (words >= maxSize)
                break;
            if (!index.containsKey(word))
                continue;
            String[] list = index.get(word);
            if (list == null)
            {
                list = new String[1];
            }
            else
            {
                list = Arrays.copyOf(list, list.length + 1);
                list[list.length - 1] = url;
            }
            words++;
            index.put(word, list);
        }
    }

    public void setEngine(Class<? extends CrawlerEngine> clazz)
    {
        if (clazz.isAssignableFrom(engine.getClass()))
            return;
        try
        {
            Constructor<? extends CrawlerEngine> engineClass = clazz.getDeclaredConstructor(Webcrawler.class);
            CrawlerEngine newEngine = engineClass.newInstance(this);
            while (engine.hasNext())
            {
                newEngine.add(engine.next());
            }
            this.engine = newEngine;
        }
        catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }

    public String[] searchHits(String searchTerm)
    {
        if (index.get(searchTerm) == null)
            return new String[0];
        return index.get(searchTerm);
    }

    public int getSize()
    {
        return words;
    }

    public interface CrawlerEngine
    {
        boolean hasNext();
        void clear();
        String next();
        void add(String value);

        default void addAll(Iterable<String> iterable)
        {
            iterable.forEach(this::add);
        }
    }

    public class DepthFirstCrawlerEngine implements CrawlerEngine
    {
        private final Stack<String> urlStack;

        public DepthFirstCrawlerEngine()
        {
            urlStack = new Stack<>();
        }

        @Override
        public String next()
        {
            return urlStack.pop();
        }

        @Override
        public void add(String url)
        {
            urlStack.push(url);
        }

        @Override
        public boolean hasNext()
        {
            return !urlStack.empty();
        }

        @Override
        public void clear()
        {
            this.urlStack.clear();
        }
    }

    public class WidthFirstCrawlerEngine implements CrawlerEngine
    {
        private Queue<String> urlQueue;

        public WidthFirstCrawlerEngine()
        {
            urlQueue = new LinkedBlockingQueue<>();
        }

        @Override
        public String next()
        {
            return urlQueue.poll();
        }

        @Override
        public void add(String url)
        {
            urlQueue.add(url);
        }

        @Override
        public boolean hasNext()
        {
            return !urlQueue.isEmpty();
        }

        @Override
        public void clear()
        {
            urlQueue.clear();
        }
    }

    public class ByteUrl
    {
        private byte[] bytes;

        public ByteUrl(String original)
        {

        }

        public String value()
        {
            return null;
        }
    }
}
