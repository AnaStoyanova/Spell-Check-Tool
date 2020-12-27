package bg.sofia.uni.fmi.mjt.spellchecker;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class WordTest {

    @Test(expected = IllegalArgumentException.class)
    public void wordTestFail() {
        Word newWord = new Word(null);
        newWord.word();
    }

    @Test
    public void calculateVectorLengthTest() {
        Word newWord = new Word("Annie");
        assertEquals(2.0, newWord.vectorLength(), 0.0);
    }

    @Test
    public void getTwoGramsTest() {
        Map<String, Integer> mustBe = new HashMap<>();
        mustBe.put("ba", 1);
        mustBe.put("an", 2);
        mustBe.put("na", 2);

        Word newWord = new Word("banana");

        assertEquals(newWord.twoGrams(), mustBe);
    }

    @Test
    public void getVectorSum() {
        Word newWord = new Word("hellow");
        assertEquals(0.7999999999999998, newWord.vectorSum(new Word("mellow")), 0.1);
    }
}
