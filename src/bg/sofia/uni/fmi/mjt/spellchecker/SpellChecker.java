package bg.sofia.uni.fmi.mjt.spellchecker;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SpellChecker {

    /**
     * Analyzes the text contained in {@code textReader} for spelling mistakes and outputs the result in {@code output}
     * The format of the analisis depends on the concrete implemetation.
     *
     * @param textReader       a java.io.Reader input stream containing some text
     * @param output           java.io.Writer output stream containing the analysis result
     * @param suggestionsCount The number of suggestions to be generated for each misspelled word in the text
     */
    void analyze(Reader textReader, Writer output, int suggestionsCount) throws IOException;

    /**
     * Returns the metadata of the text contained in {@code textReader}
     * The metadata gives information about the number of characters, words, and spelling mistakes in the text
     *
     * @param textReader a java.io.Reader input stream containing some text
     * @return Metadata for the given text
     */
    Metadata metadata(Reader textReader);

    /**
     * Returns {@code n} closest words to {@code word}, sorted in descending order.
     * The algorithm used for computing the similarity between words depends on the concrete implementation.
     *
     * @param word
     * @param n
     * @return A List of {@code n} closest words to {@code word}, sorted in descending order
     */
    List<String> findClosestWords(String word, int n);

    Set<Word> getDictionary();

    Map<Word, Integer> getMisspelledWords();

    List<String> getStopwords();
}