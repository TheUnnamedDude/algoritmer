package test;

import no.kevin.searchengine.Webcrawler;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import static org.junit.Assert.*;

public class DepthFirstCrawlerEngineTest
{
    private static final int SIZE = 256;
    private static final byte[] CAFEBABE = {(byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE};
    private Webcrawler.CrawlerEngine engine;

    @Before
    public void setUp() throws Exception
    {
        Constructor<Webcrawler.DepthFirstCrawlerEngine> constructor =
                Webcrawler.DepthFirstCrawlerEngine.class.getDeclaredConstructor(Webcrawler.class);
        engine = constructor.newInstance(new Webcrawler(SIZE));
    }

    @Test
    public void testAdd()
    {
        engine.add("Fish");
        assertEquals("Fish", engine.next());
    }
    @Test
    public void testNextUrl()
    {
        engine.add("Fish1");
        engine.add("Fish2");
        assertEquals("Fish2", engine.next());
        assertEquals("Fish1", engine.next());
    }

    @Test
    public void testAddAll()
    {
        engine.addAll(Arrays.asList("Fish1", "Fish2"));
        assertEquals("Fish2", engine.next());
        assertEquals("Fish1", engine.next());
    }

    @Test
    public void testHasNext()
    {
        assertFalse(engine.hasNext());
        engine.add("Fish");
        assertTrue(engine.hasNext());
        engine.next();
        assertFalse(engine.hasNext());
    }
}
