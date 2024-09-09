import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static List<String> listFilesUsingJavaIO(String dir) {
        return Stream.of(new File(dir).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toList());
    }

    public static void main(String[] args) throws IOException {
        //get the paths of files that contain polynomials
        String path = "C:\\Facultate\\Cursuri facultate sem 5\\PPD\\Labs\\Teme\\lab4\\Lab4-PPD-Secvential\\date2";
        List<String> filenames = listFilesUsingJavaIO(path);
        for (int i = 0; i < filenames.size(); i++) {
            filenames.set(i, path + "/"+ filenames.get(i));
        }

        long startTime = System.nanoTime();
        PolyLinkedList list = new PolyLinkedList(filenames);
        long endTime = System.nanoTime();
        System.out.println((double) (endTime - startTime) / 1E6);    // ms
//        list.writeResultPolyToFile("C:\\Facultate\\Cursuri facultate sem 5\\PPD\\Labs\\Teme\\lab4\\Lab4-PPD-Secvential\\result1.txt");

    }
}