import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class Main {
    public static void generateData ( int N, int M, int n, int m, String filename) throws IOException {
        FileWriter writer = new FileWriter(filename);
        writer.write(String.format("%d %d %d %d\n", N, M, n, m));
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                writer.write(String.valueOf(new Random(System.nanoTime()).nextInt(100)) + " ");
            }
            writer.write('\n');
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                writer.write(String.valueOf(new Random(System.nanoTime()).nextDouble()) + " ");
            }
            writer.write('\n');
        }
        writer.close();
    }

    public static void main(String[] args) throws IOException {
        generateData(10, 10, 3, 3, "matrix1");
        generateData(1000, 1000, 5, 5, "matrix2");
        generateData(10, 10000, 5, 5, "matrix3");
        generateData(10000, 10, 5, 5, "matrix4");
    }
}