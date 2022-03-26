import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class MyReduceTask implements Runnable {
    private final String fileName;
    private final List<DictAndList> list;
    private ExecutorService tpe;
    private final AtomicInteger inQueue;

    public MyReduceTask(String fileName, List<DictAndList> list, ExecutorService tpe, AtomicInteger inQueue) {
        this.fileName = fileName;
        this.list = list;
        this.tpe = tpe;
        this.inQueue = inQueue;
    }

    @Override
    public void run() {
        //Etapa de combinare
        DictAndList a = new DictAndList(new ConcurrentHashMap<>(), new ArrayList<>());
            for (var pair : list) {
                // combinare dictionare;
                for (var entry : pair.dictionary.entrySet()) {
                    Integer aux = a.dictionary.putIfAbsent(entry.getKey(), entry.getValue());
                    if (aux != null) {
                        a.dictionary.put(entry.getKey(), aux + entry.getValue());
                    }
                }

                //combinare liste
                if (a.list.isEmpty()) {
                    for (int i = 0; i < pair.list.size(); i++)
                        a.list.add(pair.list.get(i));
                } else if (a.list.get(0).length() < pair.list.get(0).length()) {
                    a.list.clear();
                    for (int i = 0; i < pair.list.size(); i++)
                        a.list.add(pair.list.get(i));
                } else if (a.list.get(0).length() == pair.list.get(0).length()) {
                    for (int i = 0; i < pair.list.size(); i++)
                        a.list.add(pair.list.get(i));
                }
            }

            Tema2.reduceResult.put(fileName, a);

           // Etapa de procesare
            float rang = 0;
            int fibonacci_sum = 0;
            int totalWords = 0;

            // obtinerea rangului conform formulei din enunt
            for (var entry : a.dictionary.entrySet()) {
                fibonacci_sum += entry.getValue() * fibonacci(entry.getKey() + 1);
                totalWords += entry.getValue();
            }

            rang = (float) fibonacci_sum / totalWords;

            int lungimea_cuvintelor_maxime = a.list.get(0).length();
            int nr_cuvinte_maxime = a.list.size();

            // rezultatul combinarii dictionarelor si listelor de la map, mapate fiecare la fisierul sau
            Tema2.outputValues.put(fileName, new OutputPackage(rang, nr_cuvinte_maxime, lungimea_cuvintelor_maxime));


        int left = inQueue.decrementAndGet();
        if (left == 0) {
            synchronized (Tema2.obj) {
                Tema2.obj.notifyAll();
            }
        }

    }

    private int fibonacci(int n) {
        if(n == 0)
            return 0;
        else if(n == 1)
            return 1;
        else
            return fibonacci(n - 1) + fibonacci(n - 2);
    }
}
