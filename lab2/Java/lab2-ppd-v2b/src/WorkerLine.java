import java.util.Queue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.LinkedBlockingQueue;

import static java.lang.Math.min;

public class WorkerLine extends Thread {
    private final int N, M, n, m, start, end;
    private int[][] F;
    private double[][] kernel;
    private Queue<TemporaryCopy> queue;
    private int[][] upperBorder, lowerBorder;
    private CyclicBarrier barrier;

    public WorkerLine(int N, int M, int[][] F, int n, int m, double[][] kernel, CyclicBarrier barrier, int start, int end) {
        this.N = N;
        this.M = M;
        this.F = F;
        this.n = n;
        this.m = m;
        this.kernel = kernel;
        this.barrier = barrier;
        this.queue = new LinkedBlockingQueue<>();
        this.upperBorder = new int[n / 2][M];
        this.lowerBorder = new int[n / 2][M];
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
        // calcul pentru elementele de pe frontiera de sus
        for (int i = start; i < start + n / 2; i++)
            for (int j = 0; j < M; j++) {
                upperBorder[i - start][j] = transformPixel(i, j);
            }

        // calcul pentru elementele care nu se afla pe frontiera
        for (int i = start + n / 2; i < end - n / 2; i++)
            for (int j = 0; j < M; j++) {
                int newValue = transformPixel(i, j);
                queue.add(new TemporaryCopy(i, j, newValue)); //adaugam un nou element in coada
                TemporaryCopy firstElemQ = queue.peek();
                //verificam daca mai avem nevoie de primul element din coada, iar daca nu, putem face modificarea definitiv in matricea initiala
                if (firstElemQ.getLine() > min(i - n / 2 + 1, start + n / 2 - 1) && firstElemQ.getColumn() > j - m / 2 + 1) {
                    F[firstElemQ.getLine()][firstElemQ.getColumn()] = firstElemQ.getNewValue();
                    queue.poll();
                }
            }

        // calcul pentru elementele de pe frontiera de sus
        for (int i = end - n / 2; i < end; i++)
            for (int j = 0; j < M; j++)
                lowerBorder[end - 1 - i][j] = transformPixel(i, j);

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
        for (int i = start; i < start + n / 2; i++)
            for (int j = 0; j < M; j++)
                F[i][j] = upperBorder[i - start][j];

        for (int i = end - n / 2; i < end; i++)
            for (int j = 0; j < M; j++)
                F[i][j] = lowerBorder[end - 1 - i][j];
    }
}