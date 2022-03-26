import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;


public class Tema2 {
    private static int nrOfWorkers;
    private static int nrOfOctets;
    private static int nrOfFiles;
    private static int nrOfFragments;
    private static String INPUT_FILE_PATH = "./";
    private static String OUTPUT_FILE_PATH = "./";

    private static final int FIRST_LINE = 1;
    private static final int SECOND_LINE = 2;
    private static AtomicInteger inQueue;
    private static ExecutorService tpe, tpe2;

    public static final Object obj = new Object();
    public static Map<String, List<DictAndList>> mapResult = new ConcurrentHashMap<>();
    public static Map<String, DictAndList> reduceResult = new ConcurrentHashMap<>();
    private static final ArrayList<MyMapTask> mapTasks = new ArrayList<>();
    private static final ArrayList<MyReduceTask> reduceTasks = new ArrayList<>();

    public static Map<String, OutputPackage> outputValues = new ConcurrentHashMap<>();
    private static final ArrayList<String> filesNAMES = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        nrOfWorkers = Integer.parseInt(args[0]);
        INPUT_FILE_PATH += args[1];
        OUTPUT_FILE_PATH += args[2];

        inQueue = new AtomicInteger(0);
        tpe = Executors.newFixedThreadPool(nrOfWorkers);
        tpe2 = Executors.newFixedThreadPool(nrOfWorkers);

        Map();
        Reduce();
        writeOutput();
    }

    private static void writeOutput() throws FileNotFoundException {
        PrintStream x = new PrintStream(new FileOutputStream(OUTPUT_FILE_PATH));
        // sortez fisierele dupa rang
        filesNAMES.sort((a, b) -> (outputValues.get(a).rang > outputValues.get(b).rang ? -1 : 1));
        for (var f : filesNAMES) {
            String rang = String.format("%.2f", outputValues.get(f).rang);
            x.println(f.substring(f.lastIndexOf("/") + 1) + "," + rang + "," + outputValues.get(f).LungimeMaxima +
                    "," + outputValues.get(f).nrAparitii);
        }
        x.close();
    }

    private static void Reduce() {
        for (var entry : mapResult.entrySet()) {
            reduceTasks.add(new MyReduceTask(entry.getKey(), entry.getValue(), tpe2, inQueue));
        }
        inQueue.set(reduceTasks.size()); // setam nr de taskuri

        for (var task : reduceTasks) tpe2.submit(task);  // pornim taskurile

        synchronized (obj) {
            try {
                obj.wait();  // asteptam finalizarea taskurilor
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        tpe2.shutdown();
    }

    private static void Map() {
        processInputFile(); // citirea fisierului de input

        inQueue.set(mapTasks.size());  // setam nr de taskuri
        for (var task : mapTasks) tpe.submit(task); // pornim taskurile

        synchronized (obj) {
            try {
                obj.wait();  // asteptam finalizarea
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        tpe.shutdown();
    }

    private static void processInputFile() {
        try {
            File inputFile = new File(INPUT_FILE_PATH);
            Scanner fileReader = new Scanner(inputFile);
            int lineNumber = 1;
            while (fileReader.hasNextLine()) {
                String data = fileReader.nextLine();
                // initializarea valorilor de input
                initialize(data, lineNumber);
                lineNumber++;
            }
            fileReader.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void initialize(String data, int lineNumber) throws IOException, InterruptedException {
        switch (lineNumber) {
            case FIRST_LINE:
                nrOfOctets = Integer.parseInt(data);
                break;
            case SECOND_LINE:
                nrOfFiles = Integer.parseInt(data);
                break;
            default: {  // daca e nume e fisier
                processTextFile("./" + data);
            }
        }
    }

    private static void processTextFile(String filePath) throws IOException, InterruptedException {
        filesNAMES.add(filePath); // il salvez pentru a putea sorta aceste fisiere mai apoi dupa rang

        Pattern pattern = Pattern.compile("[\";:/?~ .,><`\\[\\]{}()!@#$%^&\\-_+'=*|\\t\\r\\n]", Pattern.CASE_INSENSITIVE);
        File file = new File(filePath);

        long bytes = file.length();  // lungimea fisierului

        Scanner scanner = new Scanner(file);
        int offset = 0;
        String textFromFile = scanner.nextLine();
        while (scanner.hasNextLine()) {
            textFromFile = textFromFile + "\n" + scanner.nextLine();
        }

        // obtinerea taskurilor si introducerea lor intr-o lista de taskuri
        while (offset < bytes) {
            if(offset + nrOfOctets > bytes){
                mapTasks.add(new MyMapTask(filePath, offset, (int) (bytes - offset + 1), tpe, inQueue));
                break;
            }
            // pana cand dau de delimitator
            if (pattern.matcher(String.valueOf(textFromFile.charAt(offset + nrOfOctets))).find()) {
                mapTasks.add(new MyMapTask(filePath, offset, nrOfOctets, tpe, inQueue));
                offset += nrOfOctets;

                // daca nu e delimitator, adica cuvant care trebuie fragmentat:
            } else {
                int tempOffset = 0;  // variabila de contor de offset pentru fragmentare
                // while != delimititator
                while (!pattern.matcher(String.valueOf(textFromFile.charAt(offset + nrOfOctets + tempOffset - 1))).find()) {
                    if (offset + nrOfOctets - 1 + tempOffset + 1 > bytes) {
                        break;
                    } else {
                        tempOffset++;
                        if (offset + nrOfOctets + tempOffset == bytes) {
                            tempOffset++;
                            break;
                        }
                    }
                }
                // cuvant gol
                if (textFromFile.charAt(offset) == ' ') {
                    mapTasks.add(new MyMapTask(filePath, offset + 1, tempOffset + nrOfOctets - 1, tpe, inQueue));
                } else {
                    mapTasks.add(new MyMapTask(filePath, offset, tempOffset + nrOfOctets - 1, tpe, inQueue));
                }
                offset += tempOffset + nrOfOctets;
            }
        }
    }
}
