package ru.spbau.lobanov.streamAPI;

import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class SecondPartTasksTest {

    @Test
    public void testFindQuotes() throws IOException {
        PrintWriter out = new PrintWriter("a.txt");
        out.println("line 1..elf");
        out.println("       ");
        out.println("   00   ");
        out.println("---.*?[]()");
        out.println("hello world");
        out.close();

        out = new PrintWriter("b.txt");
        out.println(" h hell o");
        out.println(" l e l le");
        out.println("el l");
        out.println("wdsd");
        out.close();

        List<String> files = Arrays.asList("a.txt", "b.txt");

        List<String> list = SecondPartTasks.findQuotes(files, "ell");
        Collections.sort(list);
        assertEquals(Arrays.asList(" h hell o", "hello world"), list);
        assertEquals(Collections.emptyList(), SecondPartTasks.findQuotes(files, "lemon"));

        Files.delete(Paths.get("a.txt"));
        Files.delete(Paths.get("b.txt"));

    }

    @Test
    public void testPiDividedBy4() {
        for (int i = 0; i < 5; i++) {
            double res = SecondPartTasks.piDividedBy4() * 4;
            assertEquals(res, Math.PI, 0.01);
        }
    }

    @Test
    public void testFindPrinter() {
        List<String> selection1 = Arrays.asList("short story", "short story 2");
        List<String> selection2 = Arrays.asList("veeeery looong story", "");
        List<String> selection3 = Arrays.asList("story", "story", "story", "story", "story");
        List<String> selection4 = Arrays.asList("", "", "", "", "", "", "", "", "", "", "");
        List<String> selection5 = Collections.emptyList();

        Map<String, List<String>> test1 = new HashMap<String, List<String>>(){{
            put("writer1", selection1);
            put("writer2", selection2);
            put("writer3", selection3);
            put("writer4", selection4);
            put("writer5", selection5);
        }};

        assertEquals("writer3", SecondPartTasks.findPrinter(test1));
    }

    @Test
    public void testCalculateGlobalOrder() {
        Map<String, Integer> request1 = new HashMap<String, Integer>() {{
           put("apple", 2);
           put("pineapple", 4);
           put("potato", 7);
        }};

        Map<String, Integer> request2 = new HashMap<String, Integer>() {{
            put("apple", 5);
            put("pineapple", 10);
            put("pasta", 17);
        }};

        Map<String, Integer> request3 = new HashMap<String, Integer>() {{
            put("pasta", 0);
            put("tea", 9);
            put("cucumber", 1);
        }};

        Map<String, Integer> request4 = Collections.emptyMap();

        Map<String, Integer> expected = new HashMap<String, Integer>() {{
           put("apple", 7);
           put("pineapple", 14);
           put("potato", 7);
           put("pasta", 17);
           put("tea", 9);
           put("cucumber", 1);
        }};
        List<Map<String, Integer>> list = Arrays.asList(request1, request2, request3, request4);
        assertEquals(expected, SecondPartTasks.calculateGlobalOrder(list));
    }
}