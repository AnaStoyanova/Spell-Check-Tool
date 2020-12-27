package bg.sofia.uni.fmi.mjt.spellchecker;

import java.util.HashMap;
import java.util.Map;

public class Word {
    private String string;
    private Map<String, Integer> twoGrams;
    private double vectorLength;

    public Word(String word) {
        if (word == null) {
            throw new IllegalArgumentException("The word can not be null!");
        }

        this.string = word;
        twoGrams = generateTwoGrams(word);
        calculateVectorLength();
    }

    public String word() {
        return string;
    }

    private void calculateVectorLength() {
        vectorLength = 0;

        twoGrams.forEach((key, value) -> vectorLength += Math.pow(value, 2));

        vectorLength = Math.sqrt(vectorLength);

    }

    public Map<String, Integer> twoGrams() {
        return twoGrams;
    }

    public double vectorLength() {
        return vectorLength;
    }

    private Map<String, Integer> generateTwoGrams(String word) {
        StringBuilder twoLetters = new StringBuilder(word.substring(0, 2));
        Map<String, Integer> twoGrams = new HashMap<>();
        twoGrams.put(twoLetters.toString(), 1);

        for (int i = 2; i < word.length(); i++) {
            twoLetters.deleteCharAt(0);
            twoLetters.append(word.charAt(i));

            Integer occurrences = twoGrams.get(twoLetters.toString());
            twoGrams.put(twoLetters.toString(), occurrences == null ? 1 : (occurrences + 1));
        }

        return twoGrams;
    }


    public Double vectorSum(Word otherWord) {
        Map<String, Integer> otherGrams = otherWord.twoGrams();

        Double vectorSum = Double.valueOf(0);

        for (Map.Entry<String, Integer> twoGram : twoGrams.entrySet()) {
            if (otherGrams.containsKey(twoGram.getKey())) {
                vectorSum += (otherGrams.get(twoGram.getKey()) * twoGram.getValue());
            }
        }

        vectorSum /= (this.vectorLength() * otherWord.vectorLength());

        return vectorSum;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Word other
                && string.equals(other.string);
    }


    @Override
    public int hashCode() {
        return word().hashCode();
    }
}
