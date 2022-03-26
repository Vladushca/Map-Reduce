import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyMapTask implements Runnable {
    private final String fileName;
    private final int off;
    private final int fragmentSize;
    ExecutorService tpe;
    AtomicInteger inQueue;

    public MyMapTask(String fileName, int off, int fragmentSize, ExecutorService tpe, AtomicInteger inQueue) {
        this.fileName = fileName;
        this.off = off;
        this.fragmentSize = fragmentSize;
        this.inQueue = inQueue;
        this.tpe = tpe;
    }

    public void run() {
        // cuvintele fragmentului
        ArrayList<String> words = getWords();
        // dictionarul lungime=nr_aparitii
        Map<Integer, Integer> dict  =  getDictionary(words);
        // lista de cuvinte maxime
        ArrayList<String> maxList = getListMaxWords(dict, words);

        Tema2.mapResult.putIfAbsent(fileName, Collections.synchronizedList(new ArrayList<DictAndList>()));
        List<DictAndList> list = Tema2.mapResult.get(fileName);
        list.add(new DictAndList(dict, maxList));

        int left = inQueue.decrementAndGet();
        if (left == 0) {
            synchronized (Tema2.obj) {
                Tema2.obj.notifyAll();
            }
        }
    }

    private ArrayList<String> getWords() {
        char[] buf = new char[fragmentSize];
        BufferedReader reader = null;
        ArrayList<String> words = new ArrayList<>(Collections.emptyList());

        // obtin fragmentul dintre offset si offset + fragmentsize
        try {
            reader = new BufferedReader(new FileReader(fileName));
            reader.skip(off);
            reader.read(buf, 0, fragmentSize);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Pattern p = Pattern.compile("[a-zA-Z0-9]+");
        String content = new String(buf);
        Matcher m = p.matcher(content);

        // obtin cuvintele din acest fragment
        while (m.find()) {
            words.add(m.group());
        }

        return words;
    }

    private Map<Integer, Integer> getDictionary(ArrayList<String> words) {
        Map<Integer, Integer> dict = new HashMap<>();
        for (String word : words) {
            Integer anterior = dict.putIfAbsent(word.length(), 1);
            if (anterior != null) {
                dict.put(word.length(), anterior + 1);
            }
        }
        return dict;
    }

    private ArrayList<String> getListMaxWords(Map<Integer, Integer> dict, ArrayList<String> words) {
        ArrayList<String> maximLength = new ArrayList<>(Collections.emptyList());
        int maxKey = 0;
        for (int key : dict.keySet()) {
            if (key >= maxKey) maxKey = key;
        }
        for (String word : words) {
            if (word.length() == maxKey) {
                maximLength.add(word);
            }
        }
        return maximLength;
    }
}