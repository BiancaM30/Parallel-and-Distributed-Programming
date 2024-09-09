import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

public class Main {

    //If we find a discrepancy, we return the byte position of the mismatch. Otherwise, the files are identical and the method returns -1L.
    public static long filesCompareByByte(Path path1, Path path2) throws IOException {
        try (BufferedInputStream fis1 = new BufferedInputStream(new FileInputStream(path1.toFile()));
             BufferedInputStream fis2 = new BufferedInputStream(new FileInputStream(path2.toFile()))) {

            int ch = 0;
            long pos = 1;
            while ((ch = fis1.read()) != -1) {
                if (ch != fis2.read()) {
                    return pos;
                }
                pos++;
            }
            if (fis2.read() == -1) {
                return -1;
            } else {
                return pos;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Path path1 = Path.of("C://Facultate//Cursuri facultate sem 5//PPD//Labs//Teme//lab4//FileComparator//result1-secvential");
        Path path2 = Path.of("C://Facultate//Cursuri facultate sem 5//PPD//Labs//Teme//lab4//FileComparator//result1-paralel");
        Path path3 = Path.of("C://Facultate//Cursuri facultate sem 5//PPD//Labs//Teme//lab4//FileComparator//result2-secvential");
        Path path4 = Path.of("C://Facultate//Cursuri facultate sem 5//PPD//Labs//Teme//lab4//FileComparator//result2-paralel");
        if (filesCompareByByte(path1, path2) == -1 && filesCompareByByte(path3, path4) == -1) {
            System.out.println("Files contain same elements!");
        } else {
            System.out.println("Files are not the same!");
        }

    }
}