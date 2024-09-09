public class Subthread extends Thread {
    int N, M, n, m, start, end;
    int[][] F;
    double[] ind;
    int[][] v;

    public Subthread(int N, int M, int[][] F, int n, int m, double[] ind, int[][] v, int start, int end) {
        this.N = N;
        this.M = M;
        this.F = F;
        this.n = n;
        this.m = m;
        this.ind = ind;
        this.v = v;
        this.start = start;
        this.end = end;
    }

    @Override
    public void run() {
        for (int i = start; i < end; i++) {
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
    }
}