import java.util.Queue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.LinkedBlockingQueue;

import static java.lang.Math.min;

public class WorkerColumn extends Thread {
    private final int N, M, n, m, start, end;
    private int[][] F;
    private double[][] kernel;
    private Queue<TemporaryCopy> queue;
    private int[][] leftBorder, rightBorder;
    private CyclicBarrier barrier;

    public WorkerColumn(int N, int M, int[][] F, int n, int m, double[][] kernel, CyclicBarrier barrier, int start, int end) {
        this.F = F;
        this.kernel = kernel;
        this.N = N;
        this.M = M;
        this.n = n;
        this.m = m;
        this.barrier = barrier;
        this.queue = new LinkedBlockingQueue<>();
        this.leftBorder = new int[N][m / 2];
        this.rightBorder = new int[N][m / 2];
        this.start = start;
        this.end = end;
    }

    private int transformPixel(int line, int column) {
        int s = 0;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++) {
                int poz1 = line + (i - 1), poz2 = column + (j - 1);
                //daca depasim liniile
                if (poz1 < 0)
                    poz1 = 0;
                else if (poz1 >= N)
                    poz1 = N - 1;
                //daca depasim coloanele
                if (poz2 < 0)
                    poz2 = 0;
                else if (poz2 >= M)
                    poz2 = M - 1;
                s += F[poz1][poz2] * kernel[i][j];
            }
        return s;
    }

    @Override
    public void run() {
        // calcul pentru elementele de pe frontiera din stanga
        for (int i = 0; i < N; i++)
            for (int j = start; j < start + n / 2; j++)
                leftBorder[i][j - start] = transformPixel(i, j);

        // calcul pentru elementele care nu se afla pe frontiera
        for (int i = 0; i < N; i++)
            for (int j = start + n / 2; j < end - n / 2; j++) {
                int newValue = transformPixel(i, j);
                queue.add(new TemporaryCopy(i, j, newValue)); //adaugam un nou element in coada
                TemporaryCopy firstElemQ = queue.peek();
                //verificam daca mai avem nevoie de primul element din coada, iar daca nu, putem face modificarea definitiv in matricea initiala
                if (firstElemQ.getLine() > i - n / 2 + 1 && firstElemQ.getColumn() > min(j - m / 2 + 1, start + m / 2 - 1)) {
                    F[firstElemQ.getLine()][firstElemQ.getColumn()] = firstElemQ.getNewValue();
                    queue.poll();
                }
            }

        // calcul pentru elementele de pe frontiera de sus
        for (int i = 0; i < N; i++)
            for (int j = end - n / 2; j < end; j++)
                rightBorder[i][end - 1 - j] = transformPixel(i, j);

        //golim coada
        while (!queue.isEmpty()) {
            TemporaryCopy firstElemQ = queue.peek();
            F[firstElemQ.getLine()][firstElemQ.getColumn()] = firstElemQ.getNewValue();
            queue.poll();
        }

        try {
            barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }

        // dupa ce threadurile au asteptat la bariera, putem modifica definitiv si valorile de pe frontiere
        for (int i = 0; i < N; i++)
            for (int j = start; j < start + n / 2; j++)
                F[i][j] = leftBorder[i][j - start];

        for (int i = 0; i < N; i++)
            for (int j = end - n / 2; j < end; j++)
                F[i][j] = rightBorder[i][end - 1 - j];
    }
}