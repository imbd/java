package ru.spbau.mit;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static ru.spbau.mit.SecondPartTasks.*;

public class SecondPartTasksTest {

    @Test
    public void testFindQuotes() {
        List<String> correct = Arrays.asList("asdjsaopa", "  opa ppb", "opa", "sdjhfopaf", "opapapa");
        List<String> directory = Arrays.asList("src/test/resources/findQuotes.txt","src/test/resources/findQuotes2.txt");
        List<String> myAnswer = findQuotes(directory, "opa");
        Collections.sort(correct);
        Collections.sort(myAnswer);
        assertEquals(correct, myAnswer);
    }

    @Test
    public void testPiDividedBy4() {

        assertEquals(piDividedBy4(), Math.PI/4, 0.01);
    }

    @Test
    public void testFindPrinter() {
        String author1text1 = "bumbumbum";
        String author1text2 = "blablabla";
        String author2text1 = "a";
        String author2text2 = "b";
        String author2text3 = "c";
        String author2text4 = "d";
        String author2text5 = "e";
        String author2text6 = "f";
        String author3text1 = "aaaaaaaaaaaaa";

        Map<String, List<String>> compositions = new HashMap<>();
        compositions.put("Author1", Arrays.asList(author1text1, author1text2));
        compositions.put("Author2", Arrays.asList(author2text1, author2text2, author2text3, author2text4, author2text5, author2text6));
        compositions.put("Author3", Arrays.asList(author3text1));

        assertEquals("Author1", findPrinter(compositions));
        assertEquals(null, findPrinter( new HashMap<>()));
    }

    @Test
    public void testCalculateGlobalOrder() {
        Map<String, Integer> order1 = new HashMap<>();
        order1.put("a", 10);
        order1.put("b", 1000);
        order1.put("c", 1);
        order1.put("d", 5);

        Map<String, Integer> order2 = new HashMap<>();
        order2.put("a", 10);
        order2.put("b", 20);
        order2.put("c", 30);
        order2.put("d", 40);
        order2.put("e", 500);

        Map<String, Integer> order3 = new HashMap<>();
        order3.put("b", 3);
        order3.put("c", 3);
        order3.put("smth", 12345);

        Map<String, Integer> all = new HashMap<>();
        all.put("a", 20);
        all.put("b", 1023);
        all.put("c", 34);
        all.put("d", 45);
        all.put("e", 500);
        all.put("smth", 12345);

        assertEquals(all, calculateGlobalOrder(Arrays.asList(order1, order2, order3)));
        assertEquals(all, calculateGlobalOrder(Arrays.asList(all)));
        assertEquals(new HashMap<>(), calculateGlobalOrder(new ArrayList<>()));
    }
}