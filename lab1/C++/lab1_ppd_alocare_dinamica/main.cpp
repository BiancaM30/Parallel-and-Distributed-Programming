#include <iostream>
#include <fstream>
#include <thread>
#include <chrono>
#include <cmath>

using namespace std;
using namespace std::chrono;

#define N 6
#define M 6


double secvential(int **F, int n, int m, double *ind, int **v) {
    auto startTime = high_resolution_clock::now();
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
    auto endTime = high_resolution_clock::now();
    return chrono::duration<double, milli>(endTime - startTime).count();
}

void subthread(int **F, int n, int m, double *ind, int **v, int start, int end) {
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

double paralel(int **F, int n, int m, double *ind, int **v, int p) {
    int start = 0, end = 0;
    int chunk = N / p;
    int rest = N % p;
    auto *threads = new thread[p];

    auto startTime = high_resolution_clock::now();

    for (int i = 0; i < p; i++) {
        end = start + chunk;
        if (rest > 0) {
            rest--;
            end++;
        }

        threads[i] = thread(subthread, F, n, m, ind, v, start, end);
        start = end;
    }

    for (int i = 0; i < p; i++) {
        threads[i].join();
    }
    auto endTime = high_resolution_clock::now();
    delete[] threads;
    return chrono::duration<double, milli>(endTime - startTime).count();
}

void printMatrix(int **f) {
    for (int i = 0; i < N; i++) {
        for (int j = 0; j < M; j++) {
            cout << f[i][j] << " ";
        }
        cout << endl;
    }
}

bool verifyIdenticalMatrix(int **F1, int **F2) {
    for (int i = 0; i < N; i++) {
        for (int j = 0; j < M; j++) {
            if (F1[i][j] != F2[i][j])
                return false;
        }
    }
    return true;
}

int main() {
    ifstream in("..\\date.txt");
    int n, m, p;
    in >> n >> m >> p;

    int **F = new int *[N];
    auto *ind = new double [N * M];
    int **vSecv = new int *[N];
    int **vParal = new int *[N];

    for (int i = 0; i < N; i++) {
        F[i] = new int[M];
        vSecv[i] = new int[M];
        vParal[i] = new int[M];
    }

    for (int i = 0; i < N; i++) {
        for (int j = 0; j < M; j++) {
            in >> F[i][j];
        }
    }

    int poz = 0;
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < m; j++) {
            in >> ind[poz];
            poz++;
        }
    }
    in.close();

    // secvential
    double time1 = secvential(F, n, m, ind, vSecv);
    cout << "Timp executie - serial: " << time1 << endl;
    //printMatrix(vSecv);

    // paralel
    double time2 = paralel(F, n, m, ind, vParal, p);
    cout << "Timp executie - paralel: " << time2 << endl;
    //printMatrix(vParal);

    //verificare daca rezultatele sunt identice
    if (!verifyIdenticalMatrix(vSecv, vParal)) throw invalid_argument("different results!!!");

    return 0;
}
