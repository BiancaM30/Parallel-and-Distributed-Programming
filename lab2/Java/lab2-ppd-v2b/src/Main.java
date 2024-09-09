import java.io.*;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;

public class Main {

    public static double secvential(int N, int M, int[][] F, int n, int m, double[][] kernel) {
        long startTime = System.nanoTime();
        int solPartiala[] = new int[N * M];
        for (int i = 0; i < N * M; i++) {
            int kernelCenterL = n / 2;
            int kernelCenterC = m / 2;
            int line = i / M;
            int col = i % M;
            // filtram elementul curent
            for (int k = 0; k < n; k++) {
                int poz1 = line + k - kernelCenterL;
                if (poz1 < 0) poz1 = 0;
                else if (poz1 >= N) poz1 = N - 1;
                for (int l = 0; l < m; l++) {
                    int poz2 = col + l - kernelCenterC;
                    if (poz2 < 0) poz2 = 0;
                    else if (poz2 >= M) poz2 = M - 1;
                    solPartiala[i] += F[poz1][poz2] * kernel[k][l];
                }
            }
        }
        // modificam elementul filtrat in matricea initiala
        for (int i = 0; i < N * M; i++) {
            int line = i / M;
            int col = i % M;
            F[line][col] = solPartiala[i];
        }
        long endTime = System.nanoTime();
        return (double) (endTime - startTime) / 1E6;
    }
    static double paralel(int N, int M, int[][] F, int n, int m, double[][] kernel, int p) throws InterruptedException {
        CyclicBarrier barrier = new CyclicBarrier(p);
        long startTime = System.nanoTime();
        // nr linii >= nr coloane => impartim pe linii
        if (N >= M) {
            Thread[] threads = new WorkerLine[p];
            int start = 0;
            int chunk = N / p;
            int rest = N % p;
            for (int i = 0; i < p; i++) {
                int end = start + chunk;
                if (rest > 0) {
                    rest--;
                    end++;
                }
                threads[i] = new WorkerLine(N, M, F, n, m, kernel, barrier, start, end);
                threads[i].start();
                start = end;
            }
            for (int i = 0; i < p; i++) {
                threads[i].join();
            }
        }
        // nr coloane > nr linii => impartim pe coloane
        else {
            Thread[] threads = new WorkerColumn[p];
            int start = 0;
            int chunk = M / p;
            int rest = M % p;
            for (int i = 0; i < p; i++) {
                int end = start + chunk;
                if (rest > 0) {
                    end++;
                    rest--;
                }
                threads[i] = new WorkerColumn(N, M, F, n, m, kernel, barrier, start, end);
                threads[i].start();

                start = end;
            }
            for (int i = 0; i < p; i++) {
                try {
                    threads[i].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        long endTime = System.nanoTime();
        return (double) (endTime - startTime) / 1E6;
    }

    public static void printMatrix(int N, int M, int[][] f) {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                System.out.print(f[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static boolean verifyIdenticalMatrix(int N, int M, int[][] F1, int[][] F2) {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                if (F1[i][j] != F2[i][j])
                    return false;
            }
        }
        return true;
    }

    public static void main(String[] args) throws Exception {
        File myFile = new File("C:\\Facultate\\Cursuri facultate sem 5\\PPD\\Labs\\Teme\\lab2-ok\\Java\\lab2-ppd-v2b\\date.txt");
        Scanner myReader = new Scanner(myFile);
        String[] line = myReader.nextLine().split(" ");

        int N, M, n, m, p;
        p =4;
        // p = Integer.parseInt(args[0]);

        N = Integer.parseInt(line[0]);
        M = Integer.parseInt(line[1]);
        n = Integer.parseInt(line[2]);
        m = Integer.parseInt(line[3]);

        int[][] F1 = new int[N][M];
        int[][] F2 = new int[N][M];
        double[][] kernel = new double[n][m];

        for (int i = 0; i < N; i++) {
            line = myReader.nextLine().split(" ");
            for (int j = 0; j < M; j++) {
                F1[i][j] = Integer.parseInt(line[j]);
                F2[i][j] = Integer.parseInt(line[j]);
            }
        }

        for (int i = 0; i < n; i++) {
            line = myReader.nextLine().split(" ");
            for (int j = 0; j < m; j++) {
                kernel[i][j] = Double.parseDouble(line[j]);
            }
        }
        myReader.close();

//        // SECVENTIAL
        double time1 = secvential(N, M, F1, n, m, kernel);
        System.out.println("Timp executie - serial: " + time1);
        //printMatrix(N, M, F1);
        System.out.println();

//        //PARALEL
        double time2 = paralel(N, M, F2, n, m, kernel, p);
        System.out.println("Timp executie - paralel: " + time2);
        //printMatrix(N, M, F2);

//        verificare daca rezultatele sunt identice
        if (!verifyIdenticalMatrix(N, M, F1, F2)) throw new Exception();

       // System.out.println(time1);
       System.out.println(time2);
    }
}