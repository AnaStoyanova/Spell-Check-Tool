package bg.sofia.uni.fmi.mjt.spellchecker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class NaiveSpellChecker implements SpellChecker {

    private final Set<Word> dictionary;
    private final List<String> stopwords;
    private Map<Word, Integer> misspelledWords;
    private Integer currentLine;

    private String removeNonAlphaNumSymbols(String s) {
        s = s.replaceAll("^\\P{Alpha}+|\\P{Alnum}+$", "");
        return s;
    }

    private List<String> fullFormatWords(BufferedReader buffReader) {
        return buffReader.lines()
                .map(word -> {
                    word = word.toLowerCase();
                    word = word.trim();
                    word = removeNonAlphaNumSymbols(word);
                    return word;
                })
                .filter(word -> word.length() > 1)
                .collect(toList());
    }

    private List<String> partlyFormatWords(BufferedReader buffReader) {
        return buffReader.lines()
                .map(word -> {
                    word = word.toLowerCase();
                    word = word.trim();
                    return word;
                })
                .collect(toList());
    }

    public NaiveSpellChecker(Reader dictionaryReader, Reader stopwordsReader) {

        try (BufferedReader brDictionary = new BufferedReader(dictionaryReader)) {
            dictionary = new HashSet<>();
            List<String> words = fullFormatWords(brDictionary);

            for (String word : words) {
                dictionary.add(new Word(word));
            }

        } catch (IOException e) {
            throw new UncheckedIOException("There is a problem with adding to the dictionary.", e);
        }


        try (BufferedReader brStopwords = new BufferedReader(stopwordsReader)) {
            stopwords = partlyFormatWords(brStopwords);
        } catch (IOException e) {
            throw new UncheckedIOException("There is a problem with adding to the stopwords.", e);
        }

        misspelledWords = new HashMap<>();

        currentLine = 1;
    }

    private void saveWithoutCorrections(String line, BufferedWriter output) throws IOException {
        output.write(line);
        output.write(System.lineSeparator());
        output.flush();
    }

    @Override
    public Metadata metadata(Reader textReader) {

        BufferedReader br = new BufferedReader(textReader);
        String line;
        try {
            line = br.readLine();
        } catch (IOException e) {
            throw new UncheckedIOException("Line is null.", e);
        }

        int whiteSpacesCount = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ' || c == '\t' || c == '\n') {
                whiteSpacesCount++;
            }
        }

        int countChars = 0;
        countChars += line.length();
        countChars -= whiteSpacesCount;

        line = line.toLowerCase();

        List<String> words = Arrays.asList(line.split("\\s+"));

        words = words.stream()
                .map(word -> {
                    word = word.trim();
                    word = removeNonAlphaNumSymbols(word);
                    return word;
                })
                .filter(word -> !stopwords.contains(word))
                .filter(word -> word.length() > 1)
                .collect(toList());

        int countWords = words.size();

        int misspelledWordsCount = 0;
        for (String word : words) {
            if (!dictionaryContainsWord(word)) {
                if (!misspelledContainsWord(word)) {
                    Word newMisspell = new Word(word);
                    misspelledWords.put(newMisspell, currentLine);
                }
                misspelledWordsCount++;
            }
        }

        currentLine++;

        return new Metadata(countChars, countWords, misspelledWordsCount);
    }

    private boolean dictionaryContainsWord(String word) {
        return dictionary.contains(new Word(word));
    }

    private boolean misspelledContainsWord(String word) {
        return misspelledWords.containsKey(new Word(word));
    }

    private Metadata getMetadataForEachLine(String line) {

        BufferedReader brTemp = new BufferedReader(new StringReader(line + "\n"));
        Metadata metadataCurrLine = metadata(brTemp);

        return new Metadata(
                metadataCurrLine.characters(),
                metadataCurrLine.words(),
                metadataCurrLine.mistakes());
    }

    private void writeMetadata(Metadata metadata, BufferedWriter output) throws IOException {
        output.write("= = = Metadata = = =");
        output.write(System.lineSeparator());

        output.write(metadata.characters() + " characters, "
                + metadata.words() + " words, "
                + metadata.mistakes() + " spelling issues(s) found");

        output.write(System.lineSeparator());
        output.flush();
    }

    @Override
    public void analyze(Reader textReader, Writer output, int suggestionsCount) {
        BufferedReader br = new BufferedReader(textReader);
        BufferedWriter bw = new BufferedWriter(output);


        int characters = 0;
        int words = 0;
        int mistakes = 0;

        //Save text and calc metadata for each line
        try {
            String line = br.readLine();
            while (line != null) {
                saveWithoutCorrections(line, bw);
                Metadata metadataCurrLine = getMetadataForEachLine(line);

                characters += metadataCurrLine.characters();
                words += metadataCurrLine.words();
                mistakes += metadataCurrLine.mistakes();
                line = br.readLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("There is a problem with calculating the metadata.", e);
        }

        //Write the metadata to the output file
        try {
            Metadata metadata = new Metadata(characters, words, mistakes);
            writeMetadata(metadata, bw);
        } catch (IOException e) {
            throw new UncheckedIOException("There is a problem with writing the metadata to the output file.", e);
        }

        //Write the misspelled words to the output file
        try {
            writeMisspelledWords(bw, suggestionsCount);
        } catch (IOException e) {
            throw new UncheckedIOException("There is a problem with writing "
                    + "the misspelled words to the output file.", e);
        }
    }

    private void writeMisspelledWords(BufferedWriter output, int suggestionsCount) throws IOException {
        output.write("= = = Findings = = =");
        output.write(System.lineSeparator());
        output.flush();

        for (Map.Entry<Word, Integer> misspelledWord : misspelledWords.entrySet()) {
            output.write("Line #" + misspelledWord.getValue() + ", {"
                    + misspelledWord.getKey().word());

            output.write("} - Possible suggestions are {");

            List<String> suggestions = findClosestWords(misspelledWord.getKey().word(),
                    suggestionsCount);

            if (suggestions != null) {
                for (int j = 0; j < suggestions.size() - 1; j++) {
                    output.write(suggestions.get(j) + ", ");
                }
                output.write(suggestions.get(suggestions.size() - 1));
            }

            output.write("}");
            output.write(System.lineSeparator());
            output.flush();

        }
    }

    @Override
    public List<String> findClosestWords(String string, int n) {
        Word word = new Word(string);

        return dictionary.stream()
                .sorted((y, x) -> (x.vectorSum(word).compareTo(y.vectorSum(word))))
                .limit(n)
                .map(Word::word)
                .collect(toList());
    }

    public Set<Word> getDictionary() {
        return dictionary;
    }

    public Map<Word, Integer> getMisspelledWords() {
        return misspelledWords;
    }

    public List<String> getStopwords() {
        return stopwords;
    }
}
