import java.io.*;
import java.util.Scanner;

public class Main {
    static double secvential(int N, int M, int[][] F, int n, int m, double[] ind, int[][] v) {
        long startTime = System.nanoTime();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                int sumCrt = 0;
                int pozKernel = 0;
                for (int k = -n / 2; k <= n / 2; k++) {
                    int poz1 = i + k;
                    if (poz1 < 0)
                        poz1 = 0;
                    else if (poz1 > N - 1)
                        poz1 = N - 1;
                    for (int l = -m / 2; l <= m / 2; l++) {
                        int poz2 = j + l;
                        if (poz2 < 0)
                            poz2 = 0;
                        else if (poz2 > M - 1)
                            poz2 = M - 1;
                        sumCrt += ind[pozKernel] * F[poz1][poz2];
                        pozKernel++;
                    }
                }
                v[i][j] = sumCrt;
            }
        }
        long endTime = System.nanoTime();
        return (double) (endTime - startTime) / 1E6;
    }

    static double paralel(int N, int M, int[][] F, int n, int m, double[] ind, int[][] v, int p) throws InterruptedException {
        int start = 0, end = 0;
        int chunk = N / p;
        int rest = N % p;
        Thread[] threads = new Subthread[p];

        long startTime = System.nanoTime();
        for (int i = 0; i < p; i++) {
            end = start + chunk;
            if (rest > 0) {
                rest--;
                end++;
            }

            threads[i] = new Subthread(N, M, F, n, m, ind, v, start, end);
            threads[i].start();
            start = end;
        }

        for (int i = 0; i < p; i++) {
            threads[i].join();
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
        int p = 4;
        //int p = Integer.parseInt(args[0]);

        System.out.println(p);
        File myFile = new File("C:\\Facultate\\Cursuri facultate sem 5\\PPD\\Labs\\Teme\\lab1\\Java\\lab1ppd\\date.txt");
        Scanner myReader = new Scanner(myFile);
        String[] line = myReader.nextLine().split(" ");
        int N, M, n, m;
        N = Integer.parseInt(line[0]);
        M = Integer.parseInt(line[1]);
        n = Integer.parseInt(line[2]);
        m = Integer.parseInt(line[3]);
        int[][] F = new int[N][M];
        double[] ind = new double[n * m];

        for (int i = 0; i < N; i++) {
            line = myReader.nextLine().split(" ");
            for (int j = 0; j < M; j++) {
                F[i][j] = Integer.parseInt(line[j]);
            }
        }

        int poz = 0;
        for (int i = 0; i < n; i++) {
            line = myReader.nextLine().split(" ");
            for (int j = 0; j < m; j++) {
                ind[poz] = Double.parseDouble(line[j]);
                poz++;
            }
        }
        myReader.close();

        // SECVENTIAL
        int[][] vSecv = new int[N][M];
        double time1 = secvential(N, M, F, n, m, ind, vSecv);
        System.out.println("Timp executie - serial: " + time1);
        // printMatrix(N, M, vSecv);

        //PARALEL
        System.out.println(p);
        int[][] vParal = new int[N][M];
        double time2 = paralel(N, M, F, n, m, ind, vParal, p);
        System.out.println("Timp executie - paralel: " + time2);
        printMatrix(N, M, vParal);

        //verificare daca rezultatele sunt identice
        if (!verifyIdenticalMatrix(N, M, vSecv, vParal)) throw new Exception();

//        System.out.println(time1);
        System.out.println(time2);

    }
}