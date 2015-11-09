package no.kevin.searchengine;

public interface SearchEngine {

    /**
     * Sets the maximal number of word-occurences (ordforekomster).
     * 
     * When one word occurs in N web pages, this counts as N 
     * word-occurences.
     */
    void setMax(int max);

    /*
     * Instructs the engine to use breadth first-search
     *
     * Method returns "true" if the coice is valid, i.e.
     * if the search order is implemented in the search engine,
     * and "false" if the choice is invalid.
     */
    boolean setBreadthFirst();

    /*
     * Instructs the engine to use depth first-search
     *
     * Method returns "true" if the coice is valid, i.e.
     * if the search order is implemented in the search engine,
     * and "false" if the choice is invalid.
     */
    boolean setDepthFirst();

    /**
     * Start crawling at the given web page.
     */
    void crawlFrom(String webPage);

    /**
     * Returns a table containing identifiers of 
     * the web pages containing the given word.
     */
    String[] searchHits(String word);

    /**
     * Returns the number of word-occuremces. 
     *
     * When one word occurs in N web pages, this counts as N 
     * word-occurences.
     */
    int size();
}
