#include <iostream>
#include <fstream>
#include <thread>
#include <chrono>
#include <cmath>
#include <queue>
#include "barrier.cpp"

using namespace std;
using namespace std::chrono;

int** F1;
int** F2;
double** kernel;

struct TemporaryCopy {
	int line, column, value;
	TemporaryCopy(int l, int c, int v) {
		line = l;
		column = c;
		value = v;
	}
};

double secvential(int N, int M, int** F, int n, int m, double** kernel) {
	auto startTime = high_resolution_clock::now();

	int* solPartiala = new int[N * M];
	for (int i = 0; i < N * M; i++)
	{
		solPartiala[i] = 0;
	}

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

				solPartiala[i] += (int)(F[poz1][poz2] * kernel[k][l]);
			}
		}
	}

	// modificam elementul filtrat in matricea initiala
	for (int i = 0; i < N * M; i++) {
		int line = i / M;
		int col = i % M;
		F[line][col] = solPartiala[i];
	}

	auto endTime = high_resolution_clock::now();
	delete[] solPartiala;
	return chrono::duration<double, milli>(endTime - startTime).count();
}

int transformPixel(int N, int M, int n, int m, int line, int column, int** F, double** kernel) {
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

void workerLine(int N, int M, int** F, int n, int m, double** kernel, MyBarrier* barrier, int start, int end) {
	int** upperBorder = new int* [n / 2];
	for (int i = 0; i < n / 2; ++i)
		upperBorder[i] = new int[M];
	int** lowerBorder = new int* [n / 2];
	for (int i = 0; i < n / 2; ++i)
		lowerBorder[i] = new int[M];
	queue<TemporaryCopy> queue;

	for (int i = start; i < start + n / 2; i++)
		for (int j = 0; j < M; j++)
			upperBorder[i - start][j] = transformPixel(N, M, n, m, i, j, F, kernel);

	for (int i = start + n / 2; i < end - n / 2; i++)
		for (int j = 0; j < M; j++) {
			int newValue = transformPixel(N, M, n, m, i, j, F, kernel);
			queue.push(TemporaryCopy(i, j, newValue));
			TemporaryCopy firstElemQ = queue.front();
			if (firstElemQ.line > min(i - n / 2 + 1, start + n / 2 - 1) && firstElemQ.column > j - m / 2 + 1) {
				F[firstElemQ.line][firstElemQ.column] = firstElemQ.value;
				queue.pop();
			}
		}

	for (int i = end - n / 2; i < end; i++)
		for (int j = 0; j < M; j++)
			lowerBorder[end - 1 - i][j] = transformPixel(N, M, n, m, i, j, F, kernel);

	while (!queue.empty()) {
		TemporaryCopy firstElemQ = queue.front();
		F[firstElemQ.line][firstElemQ.column] = firstElemQ.value;
		queue.pop();
	}

	barrier->wait();

	for (int i = start; i < start + n / 2; i++)
		for (int j = 0; j < M; j++)
			F[i][j] = upperBorder[i - start][j];

	for (int i = end - n / 2; i < end; i++)
		for (int j = 0; j < M; j++)
			F[i][j] = lowerBorder[end - 1 - i][j];
}

void workerColumn(int N, int M, int** F, int n, int m, double** kernel, MyBarrier* barrier, int start, int end) {
	int** leftBorder = new int* [N];
	for (int i = 0; i < N; ++i)
		leftBorder[i] = new int[m / 2];
	int** rightBorder = new int* [N];
	for (int i = 0; i < N; ++i)
		rightBorder[i] = new int[m / 2];
	queue<TemporaryCopy> queue;

	for (int i = 0; i < N; i++)
		for (int j = start; j < start + n / 2; j++)
			leftBorder[i][j - start] = transformPixel(N, M, n, m, i, j, F, kernel);

	for (int i = 0; i < N; i++)
		for (int j = start + n / 2; j < end - n / 2; j++) {
			int newValue = transformPixel(N, M, n, m, i, j, F, kernel);
			queue.push(TemporaryCopy(i, j, newValue));

			TemporaryCopy firstElemQ = queue.front();
			if (firstElemQ.line > i - n / 2 + 1 && firstElemQ.column > min(j - m / 2 + 1, start + m / 2 - 1)) {
				F[firstElemQ.line][firstElemQ.column] = firstElemQ.value;
				queue.pop();
			}
		}

	for (int i = 0; i < N; i++)
		for (int j = end - n / 2; j < end; j++)
			rightBorder[i][end - 1 - j] = transformPixel(N, M, n, m, i, j, F, kernel);

	while (!queue.empty()) {
		TemporaryCopy firstElemQ = queue.front();
		F[firstElemQ.line][firstElemQ.column] = firstElemQ.value;
		queue.pop();
	}

	barrier->wait();

	for (int i = 0; i < N; i++)
		for (int j = start; j < start + n / 2; j++)
			F[i][j] = leftBorder[i][j - start];

	for (int i = 0; i < N; i++)
		for (int j = end - n / 2; j < end; j++)
			F[i][j] = rightBorder[i][end - 1 - j];
}

double paralel(int N, int M, int** F, int n, int m, double** kernel, int p) {
	MyBarrier* barrier = new MyBarrier(p);
	auto* threads = new thread[p];

	auto startTime = high_resolution_clock::now();
	// nr linii >= nr coloane => impartim pe linii
	if (N >= M) {
		int start = 0;
		int chunk = N / p;
		int rest = N % p;
		for (int i = 0; i < p; i++) {
			int end = start + chunk;
			if (rest > 0) {
				rest--;
				end++;
			}
			threads[i] = thread(workerLine, N, M, F, n, m, kernel, barrier, start, end);
			start = end;
		}
		for (int i = 0; i < p; i++) {
			threads[i].join();
		}
	}
	// nr coloane > nr linii => impartim pe coloane
	else {
		int start = 0;
		int chunk = M / p;
		int rest = M % p;
		for (int i = 0; i < p; i++) {
			int end = start + chunk;
			if (rest > 0) {
				end++;
				rest--;
			}
			threads[i] = thread(workerColumn, N, M, F, n, m, kernel, barrier, start, end);
			start = end;
		}
		for (int i = 0; i < p; i++) {
			threads[i].join();
		}
	}
	auto endTime = high_resolution_clock::now();
	delete[] threads;
	delete barrier;
	return chrono::duration<double, milli>(endTime - startTime).count();
}

void printMatrix(int** f, int N, int M) {
	for (int i = 0; i < N; i++) {
		for (int j = 0; j < M; j++) {
			cout << f[i][j] << " ";
		}
		cout << endl;
	}
}

bool verifyIdenticalMatrix(int** F1, int** F2, int N, int M) {
	for (int i = 0; i < N; i++) {
		for (int j = 0; j < M; j++) {
			if (F1[i][j] != F2[i][j])
				return false;
		}
	}
	return true;
}

int main(int argc, char* argv[]) {
	ifstream in("C:/Facultate/Cursuri facultate sem 5/PPD/Labs/Teme/lab2-ok/C++/lab2_ppd_v2b_dinamic/lab2_ppd_v2b_dinamic/date.txt");
	int N, M, n, m, p;
	in >> N >> M >> n >> m;
	p = 4;
	//p = atoi(argv[1]);

	F1 = new int* [N];
	F2 = new int* [N];
	for (int i = 0; i < N; i++) {
		F1[i] = new int[M];
		F2[i] = new int[M];
	}
	for (int i = 0; i < N; i++) {
		for (int j = 0; j < M; j++) {
			int val;
			in >> val;
			F1[i][j] = val;
			F2[i][j] = val;
		}
	}

	kernel = new double* [n];
	for (int i = 0; i < n; i++) {
		kernel[i] = new double[m];
	}
	for (int i = 0; i < n; i++) {
		for (int j = 0; j < m; j++) {
			in >> kernel[i][j];
		}
	}
	in.close();

	//secvential
	double time1 = secvential(N, M, F1, n, m, kernel);
	cout << "Timp executie - serial: " << time1 << endl;
	//printMatrix(F1, N, M);

	// paralel
	double time2 = paralel(N, M, F2, n, m, kernel, p);
	cout << "Timp executie - paralel: " << time2 << endl;
	//printMatrix(F2, N, M);

	//verificare daca rezultatele sunt identice
	if (!verifyIdenticalMatrix(F1, F2, N, M)) throw invalid_argument("different results!!!");

	//cout << time1;
	cout << time2;

	return 0;
}
