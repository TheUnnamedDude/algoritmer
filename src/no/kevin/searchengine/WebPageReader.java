package no.kevin.searchengine;

import java.util.HashSet;
import java.util.Set;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.NoSuchElementException;

import java.io.BufferedInputStream;
import java.io.IOException;

import java.net.URL;
import java.net.MalformedURLException;

public class WebPageReader implements Runnable {

    public static final String UTF = "UTF-8";
    public static final String WESTERN = "ISO-8859-1";

    private static final Pattern START_BODY = Pattern.compile ("<body");
    private static final Pattern END_BODY = Pattern.compile ("</body");
    private static final Pattern WORD_DELIMITER = Pattern.compile ("[^\\p{Alpha}]+");


    private String link;
    private URL parent = null;
    private URL url = null;
    private String encoding;
    private HashSet < String > links = null;
    private HashSet < String > words = null;



    /*
     * Public constructor
     */
    public WebPageReader (String theUrl) {
        this (theUrl, null, WESTERN);
    }

    /*
     * Downloads the web page and gathers information,
     * if it is not already done.
     */
    public void run () {
        if (!alreadyDownloaded ())
            download ();
    }

    /*
     * Returns a set containing
     * all the links within the body
     * of the web page.
     */
    public Set < String > getLinks () {
        run ();
        return links;
    }

    /*
     * Returns a set containing
     * all the words within the body
     * of the web page.
     */
    public Set < String > getWords () {
        run ();
        return words;
    }


    /*
     * SECONDARY INTERFACE
     */
    /**
     * If you encounter problems related to the encoding, 
     * this may be of some help.
     */
    public void setEncoding (String theEncoding) {
        this.encoding = theEncoding;
    }

    public String toString () {
        return link;
    }


    /*
     * Private methods
     */
    
    /**
     * This method interprets the given
     * link within the context of this web page.
     */
    private WebPageReader (String theLink, URL theParent, String theEncoding) {
        link = theLink;
        url = establishURL(theParent,link);
        if (url != null)
           link = url.toString();

        encoding = theEncoding;
    }

    private String getAsChild (String theLink) {
        URL url = establishURL(this.url,theLink);
        if (url == null)
            return "invalid link";
        else
            return url.toString();
    }

    private void download () {
        links = new HashSet < String > ();
        words = new HashSet < String > ();

        if (url != null) {
            String contents = getContents (url, encoding);
            collectLinks (contents, links);
            collectWords (contents, words);
        }
    }

    private static URL establishURL(URL parent, String link) {
        try {
            return new URL (parent, link);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private boolean alreadyDownloaded () {
        return links != null;
    }

    private static String getContents (URL url, String encoding) {
        if (url == null)
            return "";

        Scanner scanner = null;

        try {
            scanner =
                new Scanner (new
                             BufferedInputStream (url.
                                                  openConnection ().getInputStream
                                                  ()), encoding);
        } catch (IOException e) {
            return "";
        }

        if (!scanner.hasNextLine ()) {
            return "";
        }

        scanner.useDelimiter (START_BODY);
        scanner.next ();
        scanner.useDelimiter (END_BODY);
        try {
            return scanner.next ().toLowerCase ();
        } catch (NoSuchElementException e) {
            return "";
        }
    }


    private void collectLinks (String contents, Set < String > set) {

        int off = 9;
        int begin = 0;
        int end = begin;

        while (true) {
            begin = contents.indexOf ("<a href=\"", end);
            end = contents.indexOf ("\"", begin + off);

            if (begin == -1 || end == -1) {break;}

            String possibleLink = contents.substring (begin + off, end);
            possibleLink = getAsChild(possibleLink);
            if (!(possibleLink.contains ("mailto") || possibleLink.contains ("#"))) {
                set.add (possibleLink);
            }
        }
    }

    private static void collectWords (String contents, Set < String > set) {
        Scanner sc = new Scanner (contents);
        sc.useDelimiter(WORD_DELIMITER);
        while (sc.hasNext ()) {
            String word = sc.next ();
            set.add (word);
        }
    }


    private static final int MAX_VISIT = 1000;

    public static void main (String[]args) {
        basicTest ();
    }


    private static void basicTest () {
        //no.kevin.searchengine.WebPageReader wp = new no.kevin.searchengine.WebPageReader ("https://en.wikipedia.org");
        WebPageReader wp = new WebPageReader ("http://aftenposten.no");
        wp.run ();

        System.out.println ("------------------\nLINKS:\n------------------");
        for (String link:wp.getLinks ()) {
            System.out.println (link);
        }

        System.out.println ("---------------------\nWORDS:\n---------------");
        for (String word:wp.getWords ()) {
            System.out.printf("%s, ",word);
        }
    }
}
