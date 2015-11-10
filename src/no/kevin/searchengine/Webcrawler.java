package no.kevin.searchengine;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

public class Webcrawler
{
    private CrawlerEngine engine;
    private Set<String> visitedLinks = new HashSet<>();
    private HashMap<String, ArrayList<String>> index = new HashMap<>();

    public static void main(String[] args)
    {
        new Webcrawler().crawl("http://aftenposten.no/");
    }

    public Webcrawler()
    {
        this(Short.MAX_VALUE);
    }

    public Webcrawler(int maxValue)
    {
        engine = new WidthFirstCrawlerEngine(maxValue);
    }

    public void addWordsToIndex(String... words)
    {
        Arrays.stream(words).forEach(str -> index.put(str, new ArrayList<>()));
    }

    public void resize(int newSize)
    {
        engine.resize(newSize);
    }

    public void crawl(String url)
    {
        WebPageReader reader = new WebPageReader(url);
        engine.addAll(reader.getLinks());
        while (engine.hasNext() && visitedLinks.size() <= engine.getMaxSize())
        {
            readAllLinks(engine.nextUrl());
        }
        System.out.println(visitedLinks);
        System.out.println(visitedLinks.size());
    }

    private void readAllLinks(String url)
    {
        System.out.printf("(%5d/%5d) ", visitedLinks.size(), engine.getMaxSize());
        System.out.printf("crawling: %s%n", url);

        long current = System.currentTimeMillis();
        WebPageReader reader = new WebPageReader(url);
        reader.run();
        System.out.printf("page download: %dms ", System.currentTimeMillis() - current);

        current = System.currentTimeMillis();
        visitedLinks.add(url);

        engine.addAll(reader.getLinks().stream()
                .filter(s -> !visitedLinks.contains(s))
                .collect(Collectors.toList()));

        reader.getWords().stream()
                .map(index::get)
                .filter(list -> list != null)
                .filter(list -> !list.contains(url))
                .forEach(list -> list.add(url));
        System.out.printf("logic: %dms%n", System.currentTimeMillis() - current);
    }

    public void setEngine(Class<? extends CrawlerEngine> clazz)
    {
        if (clazz.isAssignableFrom(engine.getClass()))
            return;
        try
        {
            Constructor<? extends CrawlerEngine> engineClass = clazz.getDeclaredConstructor(Webcrawler.class, int.class);
            CrawlerEngine newEngine = engineClass.newInstance(this, engine.maxSize);
            while (engine.hasNext())
            {
                newEngine.add(engine.nextUrlBytes());
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
        if (!index.containsKey(searchTerm))
            return new String[0];
        return index.get(searchTerm).stream().toArray(String[]::new);
    }

    public int getSize()
    {
        return engine.getSize();
    }

    public static String toString(byte[] bytes)
    {
        try
        {
            return new String(bytes, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            return new String(bytes);
        }
    }

    public static byte[] toBytes(String str)
    {
        try
        {
            return str.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return str.getBytes();
        }
    }

    public abstract class CrawlerEngine
    {
        int maxSize;
        private int curr = 0;
        public CrawlerEngine(int maxSize)
        {
            this.maxSize = maxSize;
        }

        public abstract boolean hasNext();
        public abstract void resize(int newSize);
        abstract void addInternal(byte[] bytes);
        abstract byte[] nextUrlBytesInternal();

        public String nextUrl()
        {
            return Webcrawler.toString(nextUrlBytes());
        }

        public void add(String url)
        {
            add(Webcrawler.toBytes(url));
        }

        public void add(byte[] bytes)
        {
            if (curr >= maxSize)
                return; // Should probably not silently break...
            curr++;
            addInternal(bytes);
        }

        public byte[] nextUrlBytes()
        {
            if (curr <= 0)
                throw new ArrayIndexOutOfBoundsException("No more elements in crawler");
            curr--;
            return nextUrlBytesInternal();
        }

        public int getMaxSize()
        {
            return maxSize;
        }

        public int getSize()
        {
            return curr;
        }

        public void addAll(Iterable<String> iterable)
        {
            iterable.forEach(this::add);
        }

        public void addAllBytes(Iterable<byte[]> iterable)
        {
            iterable.forEach(this::add);
        }
    }

    public class DepthFirstCrawlerEngine extends CrawlerEngine
    {
        private final Stack<byte[]> urlStack;

        public DepthFirstCrawlerEngine(int maxSize)
        {
            super(maxSize);
            urlStack = new Stack<>();
        }

        @Override
        public byte[] nextUrlBytesInternal()
        {
            return urlStack.pop();
        }

        @Override
        public void addInternal(byte[] url)
        {
            urlStack.push(url);
        }

        @Override
        public boolean hasNext()
        {
            return !urlStack.empty();
        }

        @Override
        public int getSize()
        {
            return urlStack.size();
        }

        @Override
        public void resize(int newSize)
        {
            if (newSize < urlStack.size())
                throw new ArrayIndexOutOfBoundsException("Cannot shrink to a size less then the current number of entries");
            this.maxSize = newSize;
        }
    }

    public class WidthFirstCrawlerEngine extends CrawlerEngine
    {
        private Queue<byte[]> urlQueue;

        public WidthFirstCrawlerEngine(int maxSize)
        {
            super(maxSize);
            urlQueue = new ArrayBlockingQueue<>(maxSize);
        }

        @Override
        public byte[] nextUrlBytesInternal()
        {
            return urlQueue.poll();
        }

        @Override
        void addInternal(byte[] url)
        {
            urlQueue.add(url);
        }

        @Override
        public boolean hasNext()
        {
            return !urlQueue.isEmpty();
        }

        @Override
        public int getSize()
        {
            return urlQueue.size();
        }

        @Override
        public void resize(int newSize)
        {
            if (newSize < urlQueue.size())
                throw new ArrayIndexOutOfBoundsException("Cannot shrink to a size less then the current number of entries");

            Queue<byte[]> newQueue = new ArrayBlockingQueue<>(newSize);
            urlQueue.forEach(newQueue::add);
            this.maxSize = newSize;
            this.urlQueue = newQueue;
        }
    }
}
