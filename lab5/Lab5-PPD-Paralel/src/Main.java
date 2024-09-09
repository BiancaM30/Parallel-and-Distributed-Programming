import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    private static final PolyLinkedList list = new PolyLinkedList();
    private static final MyQueue queue = new MyQueue();
    private static final Object fullQueue = new Object();
    private static final Object emptyQueue = new Object();
    private static final int maxSize = 20;
    private static AtomicInteger exit;

    public static List<String> listFilesUsingJavaIO(String dir) {
        return Stream.of(new File(dir).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toList());
    }

    public static void produce(String name, List<String> filenames, int start, int end) throws IOException, InterruptedException {
        for (int i = start; i < end; i++) {
            BufferedReader reader = new BufferedReader(new FileReader(filenames.get(i)));
            String line;
            while ((line = reader.readLine()) != null) {
                var fields = line.split(",");
                Monomial nod = new Monomial(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]));
                System.out.println(name + " added in queue (" + nod.coefficient + ", " + nod.exponent + ")");

                synchronized (fullQueue) {
                    while (queue.getSize() == maxSize) {
                        System.out.println(name + " is waiting...Queue is full");
                        fullQueue.wait();
                    }
                }

                //producing element and notify consumers
                synchronized (emptyQueue) {
                    queue.enqueue(nod);
                    emptyQueue.notifyAll();
                }
            }
            reader.close();
        }
        System.out.println(name + "exits");
        if (exit.decrementAndGet() == 0) {
            // for the last elements
            synchronized (emptyQueue) {
                emptyQueue.notifyAll();
            }
        }
    }

    public static void consume(String name) throws InterruptedException {
        while (true) {
                synchronized (emptyQueue) {
                    while (queue.isEmpty() && exit.get() > 0)
//                    System.out.println(name + " is waiting...Queue is empty");
                        emptyQueue.wait();
                }
                if (exit.get() == 0 && queue.isEmpty()) {
                    System.out.println("Thread " + name + " exits.");
                    return;
                }
                 if (!queue.isEmpty()) {
                     Monomial node = list.insert(queue.dequeue());
                     synchronized (fullQueue) {
                                             System.out.println(name + " removed from queue (" + node.coefficient + ", " + node.exponent + ")");
                         fullQueue.notifyAll();
                     }
                 }
            }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int p = 4;
        int p1 = 3;

//        int p = Integer.parseInt(args[0]);
//        int p1 = Integer.parseInt(args[1]);
        int p2 = p - p1;
        exit = new AtomicInteger(p1);

        //get the paths of files that contain polynomials
        String path = "C:\\Facultate\\Cursuri facultate sem 5\\PPD\\Labs/\\Teme\\lab5\\Lab5-PPD-Paralel\\date1";
        List<String> filenames = listFilesUsingJavaIO(path);
        for (int i = 0; i < filenames.size(); i++) {
            filenames.set(i, path + "/" + filenames.get(i));
        }

        long startTime = System.nanoTime();

//                producer threads - uniform distribution of input files
        Thread[] producers = new Thread[p1];
        int start = 0, end = 0;
        int chunk = filenames.size() / p1;
        int rest = filenames.size() % p1;
        for (int i = 0; i < p1; i++) {
            end = start + chunk;
            if (rest > 0) {
                rest--;
                end++;
            }
            String thName = "Producer " + (i + 1);
            int finalStart = start;
            int finalEnd = end;
            producers[i] = new Thread(() -> {
                try {
                    produce(thName, filenames, finalStart, finalEnd);
                } catch (Exception e) {
                }
            });
            start = end;
        }


//        consumers threads
        Thread[] consumers = new Thread[p2];
        for (int i = 0; i < p2; i++) {
            String thName = "Consumer" + (i + 1);
            consumers[i] = new Thread(() -> {
                try {
                    consume(thName);
                } catch (Exception e) {
                }
            });
        }

        for (int i = 0; i < p1; i++)
            producers[i].start();
        for (int i = 0; i < p2; i++)
            consumers[i].start();

        for (int i = 0; i < p1; i++)
            producers[i].join();
        for (int i = 0; i < p2; i++)
            consumers[i].join();

        long endTime = System.nanoTime();
        double time = (double) (endTime - startTime) / 1E6;
        System.out.println(time);  // ms

       // list.writeResultPolyToFile("C://Facultate//Cursuri facultate sem 5//PPD//Labs//Teme//lab5//Lab5-PPD-Paralel//result1.txt");
    }
}