package com.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class PoemStack {

    public static Integer CHUNK_SIZE = 16;

    private List<List<String>> cache = new ArrayList<>();

    private java.util.List<String> items = new ArrayList<>();

    private List<String> history = new ArrayList<>();

    public List<String> shortPoem = new ArrayList<>();

    public List<String> source = new ArrayList<>();

    public List<String> current;

    public static Map<String, List<String>> authorPoems = new HashMap<>();

    public static Set<String> authors = new HashSet<>();

    public void initDB() {

        if (Env.getHeight() < 1000) {
            CHUNK_SIZE = 12;
        }

        try {

            String data = "";
            try (InputStream is = ClockAndPoem.class.getResourceAsStream(Env.POEM_DATA_FILE)) {
                try (InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                     BufferedReader reader = new BufferedReader(isr)) {
                    data = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                }
            }

            for (String item : data.split("\n")) {

                try {
                    if (!item.trim().equals("")) {
                        push(item);
                    }
                } catch (Exception e) {

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        authorPoems.put("短诗", shortPoem);
        authors.add("短诗");

    }

    public boolean isShort() {
        return current.size() < 10;
    }

    public void push(String item) {
        items.add(item);
        authorData(item);
    }

    public void clearCache() {
        cache.clear();
    }

    public int cacheSize() {
        return cache.size();
    }

    public void addHistory(String poem) {
        if (history.size() > 10) {
            history.clear();
        }
        history.add(poem);
    }

    public String getHistory() {
        if (history.size() > 1) {
            String cacheItem = history.get(history.size() - 2);
            history.remove(history.size() - 2);
            return cacheItem;
        }
        return null;
    }

    public String random() {

        List<String> from = items;

        if (source.size() > 0) {
            from = source;
        }

        Random rand = new Random();
        int index = 0 + rand.nextInt(from.size());
        String poem = from.get(index);

        return poem;
    }

    public List<String> popHistory() {

        clearCache();

        String poem = getHistory();
        if (poem == null) {
            return pop();
        }

        List<String> poems = current = Arrays.asList(poem.split(";"));

        if (poems.size() > CHUNK_SIZE) {
            cache = chunkList(poems, CHUNK_SIZE);
            return pop();
        }

        return poems;
    }

    private String getAuthor(List<String> poems, String item) {
        String author = poems.get(1);
        if (author.indexOf("《") == -1) {
            return author;
        }

        int start = item.indexOf("》;");
        if (start != -1 && item.substring(start).length() < 40) {
            shortPoem.add(item);
        }

        return author.substring(0, author.indexOf("《"));
    }

    public List<String> popRandom() {

        String poem = random();
        addHistory(poem);

        List<String> poems = current = Arrays.asList(poem.split(";"));

        String author = getAuthor(poems, poem);
        authors.add(author);

        return poems;
    }


    public List<String> pop() {

        if (cache.size() > 0) {
            List<String> res = cache.get(0);
            cache.remove(0);
            return res;
        }

        String poem = random();
        addHistory(poem);

        List<String> poems = current = Arrays.asList(poem.split(";"));

        String author = getAuthor(poems, poem);
        authors.add(author);

        if (poems.size() > CHUNK_SIZE) {

            cache = chunkList(poems, CHUNK_SIZE);

            return pop();
        }

        return poems;
    }


    private void authorData(String poem) {
        List<String> poems = Arrays.asList(poem.split(";"));

        String author = getAuthor(poems, poem);

        List<String> list = authorPoems.get(author);
        if (list == null) {
            list = new ArrayList<>();
            authorPoems.put(author, list);
        }
        list.add(poem);
    }

    public static <T> List<List<T>> chunkList(List<T> list, int chunkSize) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Invalid chunk size: " + chunkSize);
        }

        List<List<T>> chunkList = new ArrayList<>(list.size() / chunkSize);
        for (int i = 0; i < list.size(); i += chunkSize) {

            List<T> base = new ArrayList<>();

            if (i != 0) {
                base.add(list.get(0));
                base.add(list.get(1));
            }

            List<T> poems = list.subList(i, i + chunkSize >= list.size() ? list.size() : i + chunkSize);
            if (poems.size() > 0) {
                base.addAll(poems);
                chunkList.add(base);
            }
        }
        return chunkList;
    }

}
