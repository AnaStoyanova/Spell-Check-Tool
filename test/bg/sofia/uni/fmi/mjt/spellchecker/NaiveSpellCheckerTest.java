package bg.sofia.uni.fmi.mjt.spellchecker;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class NaiveSpellCheckerTest {
    SpellChecker spellChecker;

    {
        Path dict = Path.of("dictionaryTest.txt");
        Path stopWords = Path.of("stopwordsTest.txt");

        try (BufferedReader dictReader = Files.newBufferedReader(dict);
             BufferedReader stopwordsReader = Files.newBufferedReader(stopWords)) {

            spellChecker = new NaiveSpellChecker(dictReader, stopwordsReader);

        } catch (IOException e) {
            throw new UncheckedIOException("some err msg", e);
        }
    }

    @Test
    public void naiveSpellCheckerConstructedTest() {
        assertFalse(spellChecker.getDictionary().isEmpty());
    }

    @Test(expected = IllegalStateException.class)
    public void naiveSpellCheckerTestFail() {
        File data = new File("blop.txt");
        Path stopWords = Path.of("stopwords.txt");

        try (Reader in = new FileReader(data);
             BufferedReader stopwordsReader = Files.newBufferedReader(stopWords)) {

            SpellChecker spellChecker = new NaiveSpellChecker(in, stopwordsReader);
            spellChecker.metadata(in);

        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while reading from the file", e);
        }
    }

    @Test
    public void fullFormatWordsTest() {
        Reader dictionaryReader = new StringReader(String.join(System.lineSeparator(),
                List.of("&.Cat& ", "dog", "bird")));
        Reader stopwordsReader = new StringReader(String.join(System.lineSeparator(),
                List.of("a", "am", "me")));

        SpellChecker formatChecker = new NaiveSpellChecker(dictionaryReader, stopwordsReader);

        Iterator<Word> it = formatChecker.getDictionary().iterator();
        assertEquals("cat", it.next().word());
    }

    @Test
    public void partlyFormatWordsTest() {
        Reader dictionaryReader = new StringReader(String.join(System.lineSeparator(),
                List.of("car=t", "dog", "bird")));
        Reader stopwordsReader = new StringReader(String.join(System.lineSeparator(),
                List.of("a. ", "am", "me")));

        SpellChecker formatChecker = new NaiveSpellChecker(dictionaryReader, stopwordsReader);

        assertEquals("a.", formatChecker.getStopwords().get(0));
    }

    @Test
    public void findClosestWordsTest() {
        List<String> closest = spellChecker.findClosestWords("catd", 2);
        List<String> mustBe = Arrays.asList("cat", "cats");

        assertEquals(mustBe, closest);
    }

    @Test
    public void metadataTest() {
        Reader catTextReader = new StringReader("hello, i am a cat!");

        Metadata metadata = spellChecker.metadata(catTextReader);
        Metadata mustBe = new Metadata(14, 2, 1);

        assertEquals(mustBe, metadata);

    }

    @Test
    public void analyzeTest() {
        try {
            Path file = Path.of("text.txt");
            Reader text = Files.newBufferedReader(file);

            Path result = Path.of("result.txt");
            Writer output = Files.newBufferedWriter(result);

            spellChecker.analyze(text, output, 2);

            BufferedReader stopwordsReader = Files.newBufferedReader(result);
            List<String> res = new ArrayList<>();
            stopwordsReader.lines().forEach(res::add);

            List<String> mustBe = Arrays.asList("i am a catd!",
                    "= = = Metadata = = =",
                    "9 characters, 1 words, 1 spelling issues(s) found",
                    "= = = Findings = = =",
                    "Line #1, {catd} - Possible suggestions are {cat, cats}");

            assertEquals(mustBe, res);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
