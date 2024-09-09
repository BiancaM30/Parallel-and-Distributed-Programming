import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    private static final PolyLinkedList list = new PolyLinkedList();

    private static final MyQueue queue = new MyQueue();
    private static final int maxSize = 15;
    private static boolean exit = false;

    public static List<String> listFilesUsingJavaIO(String dir) {
        return Stream.of(new File(dir).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toList());
    }

    public static void produce(List<String> filenames, String thName) throws IOException, InterruptedException {
        for (String each : filenames) {
            BufferedReader reader = new BufferedReader(new FileReader(each));
            String line;
            while ((line = reader.readLine()) != null) {
                var fields = line.split(",");
                Monomial nod = new Monomial(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]));
                //System.out.println(thName + " added in queue (" + nod.coefficient + ", " + nod.exponent + ")");

                while (queue.getSize() == maxSize) {
                    synchronized (queue) {
                        //System.out.println(thName + " is waiting...Queue is full");
                        queue.wait();
                    }
                }
                //producing element and notify consumers
                synchronized (queue) {
                    queue.enqueue(nod);
                    queue.notifyAll();
                }
            }
            reader.close();
        }
        exit = true;
        // for the last elements
        synchronized (queue) {
            queue.notifyAll();
        }
        list.writeResultPolyToFile("C://Facultate//Cursuri facultate sem 5//PPD//Labs//Teme//lab4//Lab4-PPD-Paralel//result2.txt");
    }

    public static void consume(String thName) throws InterruptedException {
        while (true) {
            while (queue.isEmpty() && !exit) {
                synchronized (queue) {
                    //System.out.println(thName + " is waiting...Queue is empty");
                    queue.wait();
                }
            }
            if (queue.isEmpty() && exit) {
                return;
            }

            synchronized (queue) {
                Monomial node = list.insert(queue.dequeue());
                queue.notifyAll();
                //System.out.println(thName + " removed from queue (" + node.coefficient + ", " + node.exponent + ")");
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int p = 6;
//        int p = Integer.parseInt(args[0]);

        //get the paths of files that contain polynomials
        String path = "C:\\Facultate\\Cursuri facultate sem 5\\PPD\\Labs/\\Teme\\lab4\\Lab4-PPD-Paralel\\date2";
        List<String> filenames = listFilesUsingJavaIO(path);

        for (int i = 0; i < filenames.size(); i++) {
            filenames.set(i, path + "/" + filenames.get(i));
        }

        long startTime = System.nanoTime();

//        producer thread
        Thread producer = new Thread(() -> {
            try {
                String thName = "Producer";
                produce(filenames, thName);
            } catch (IOException | InterruptedException e) {
            }
        });

//        consumers threads
        Thread[] consumers = new Thread[p - 1];
        for (int i = 0; i < p - 1; i++) {
            String thName = "Consumer" + (i + 1);
            consumers[i] = new Thread(() -> {
                try {
                    consume(thName);
                } catch (Exception e) {
                }
            });
        }

        producer.start();
        for (int i = 0; i < p - 1; i++)
            consumers[i].start();

        producer.join();
        for (int i = 0; i < p - 1; i++)
            consumers[i].join();

        long endTime = System.nanoTime();
        double time = (double) (endTime - startTime) / 1E6;
        System.out.println(time);  // ms

//        list.writeResultPolyToFile("C://Facultate//Cursuri facultate sem 5//PPD//Labs//Teme//lab4//Lab4-PPD-Paralel//result1.txt");
    }
}