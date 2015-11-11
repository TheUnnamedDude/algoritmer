package test;

import no.kevin.searchengine.MyEngine;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CrawlerTest
{
    private final String WEBPAGE = "http://s.tud.pw/testcrawl/index.html";
    private MyEngine engine;

    @Before
    public void setUp()
    {
        engine = new MyEngine(2048);
        engine.crawlFrom(WEBPAGE);
    }
    @Test
    public void testCrawler()
    {
        assertTrue(engine.size() > 0);
    }

    @Test
    public void testFindWords()
    {
        assertEquals(1, engine.searchHits("cow").length);
        assertEquals(2, engine.searchHits("put").length);
        assertTrue(engine.searchHits("cow")[0].endsWith("folder1/page1.html"));
        assertTrue(engine.searchHits("bird")[0].endsWith("folder1/folder3/page1.html"));
        assertTrue(engine.searchHits("lawyer")[0].endsWith("folder1/folder3/page2.html"));
    }

    @Test
    public void testSize()
    {
        assertEquals(40, engine.size());
    }
}
