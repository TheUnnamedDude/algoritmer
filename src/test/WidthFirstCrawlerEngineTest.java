package test;

import no.kevin.searchengine.Webcrawler;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class WidthFirstCrawlerEngineTest
{
    private static final int SIZE = 256;
    private static final byte[] CAFEBABE = {(byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE};
    Webcrawler.WidthFirstCrawlerEngine engine;

    @Before
    public void setUp() throws Exception
    {
        Constructor<Webcrawler.WidthFirstCrawlerEngine> constructor =
                Webcrawler.WidthFirstCrawlerEngine.class.getDeclaredConstructor(Webcrawler.class, int.class);
        engine = constructor.newInstance(new Webcrawler(), SIZE);
    }

    @Test
    public void testAdd()
    {
        engine.add("Fish");
        assertEquals("Fish", engine.nextUrl());
        engine.add(CAFEBABE);
        assertEquals(CAFEBABE, engine.nextUrlBytes());
    }

    @Test
    public void testSize()
    {
        engine.add("Fish");
        assertEquals(1, engine.getSize());
        engine.add(CAFEBABE);
        assertEquals(2, engine.getSize());
    }

    @Test
    public void testMaxSize()
    {
        assertEquals(SIZE, engine.getMaxSize());
    }

    @Test
    public void testNextUrl()
    {
        engine.add("Fish1");
        engine.add("Fish2");
        assertEquals("Fish1", engine.nextUrl());
        assertEquals("Fish2", engine.nextUrl());
    }

    @Test
    public void testAddStringGetBytes()
    {
        String testString = "Kakedeig";
        engine.add(testString);
        assertArrayEquals(toBytes(testString), engine.nextUrlBytes());
    }

    @Test
    public void testAddBytesGetString()
    {
        String testString = "Kakedeig";
        engine.add(toBytes(testString));
        assertEquals(testString, engine.nextUrl());
    }

    @Test
    public void testBounds()
    {
        for (int i = 0; i < SIZE + 1; i++)
        {
            engine.add("kaker");
        }
        assertEquals(SIZE, engine.getSize());

        for (int i = 0; i < SIZE; i++)
        {
            engine.nextUrlBytes();
        }
        assertEquals(0, engine.getSize());
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testNegativeBounds()
    {
        engine.nextUrl();
    }

    public byte[] toBytes(String str)
    {
        try
        {
            return str.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            System.out.println("No utf-8 support?");
            return str.getBytes();
        }
    }
}
