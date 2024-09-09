#include <mpi.h>
#include <iostream>
#include <fstream>
#include <thread>
#include <chrono>

using namespace std;
using namespace std::chrono;

// varianta 0: secvential
void secvential(int n1, int* numar1, int n2, int* numar2, int n, int* suma) {
	int carry = 0;
	for (int i = 0; i < n; i++) {
		int s = numar1[i] + numar2[i];
		suma[i] = (s + carry) % 10;
		carry = (s + carry) / 10;
	}
}

// verifica daca 2 tablouri au exact aceleasi elemente, il folosim pt a verifica suma secventiala vs paralela
bool verifyIdenticalArrays(int n, int* v1, int* v2) {
	for (int i = 0; i < n; i++) {
		if (v1[i] != v2[i])
			return false;
	}
	return true;
}

//afiseaza vectorul suma rezultat pe ecran si in fisier
void print_result(int n, int* suma) {
	ofstream out("C:/Facultate/Cursuri facultate sem 5/PPD/Labs/Teme/lab3/Lab3-PPD-v2/Lab3-PPD_v2/Lab3-PPD_v2/Numar3.txt");

	for (int i = n - 1; i >= 0; i--)
		if (i == n - 1) {
			// nu afisam zerourile de la inceputul numarului
			if (suma[i] != 0) {
				//cout << suma[i];
				out << suma[i];
			}
		}
		else {
			//cout << suma[i];
			out << suma[i];
		}
	//cout << '\n';
	out.close();
}

// varianta 2 (scatterv, gatherv)
int main(int argc, char** argv) {
	// initializare mpi
	int rc = MPI_Init(&argc, &argv);
	if (rc != MPI_SUCCESS)
	{
		cout << "Err init MPI";
		MPI_Abort(MPI_COMM_WORLD, rc);
	}
	int rank, nr_procese;
	MPI_Comm_size(MPI_COMM_WORLD, &nr_procese);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);
	//printf("Proces %d din %d\n", rank, nr_procese);

	auto startTime = high_resolution_clock::now();

	//citim lungimile celor 2 numare
	int n = 0, n1 = 0, n2 = 0;
	ifstream in1("C:/Facultate/Cursuri facultate sem 5/PPD/Labs/Teme/lab3/Lab3-PPD-v2/Lab3-PPD_v2/Lab3-PPD_v2/Numar1.txt");
	ifstream in2("C:/Facultate/Cursuri facultate sem 5/PPD/Labs/Teme/lab3/Lab3-PPD-v2/Lab3-PPD_v2/Lab3-PPD_v2/Numar2.txt");
	in1 >> n1;
	in2 >> n2;

	//lungime rezultat
	n = max(n1, n2) + 1;
	
	int cat = n / nr_procese;
	int rest = n % nr_procese;

	int* cifre_per_proces = new int[nr_procese];
	int* displs = new int[nr_procese]; // Locatiile datelor pe care trebuie sa le trimitem fiecarui communicator de proces

	for (int i = 0; i < nr_procese; i++) {
		if (i < rest) {
			cifre_per_proces[i] = cat + 1;
			displs[i] = i * cat + i;
		}
		else {
			cifre_per_proces[i] = cat;
			displs[i] = i * cat + rest;
		}
	}
	int* numar1 = new int[n];
	int* numar2 = new int[n];
	int* suma = new int[n];
	int* subnumar1 = new int[cifre_per_proces[rank]];
	int* subnumar2 = new int[cifre_per_proces[rank]];
	int* suma_partiala = new int[cifre_per_proces[rank]];

	// procesul 0
	if (rank == 0) {
		for (int i = n - 1; i >= 0; i--) {
			char cifra;
			if (i < n1) {
				in1 >> cifra;
				numar1[i] = cifra - '0';
			}
			else {
				numar1[i] = 0;
			}
			if (i < n2) {
				in2 >> cifra;
				numar2[i] = cifra - '0';
			}
			else {
				numar2[i] = 0;
			}			
		}
	}
	in1.close();
	in2.close();
	

	// suma pe threaduri
	MPI_Scatterv(numar1, cifre_per_proces, displs, MPI_INT, subnumar1, cifre_per_proces[rank], MPI_INT, 0, MPI_COMM_WORLD);
	MPI_Scatterv(numar2, cifre_per_proces, displs, MPI_INT, subnumar2, cifre_per_proces[rank], MPI_INT, 0, MPI_COMM_WORLD);
	int carry = 0;
	MPI_Status out;
	for (int i = 0; i < cifre_per_proces[rank]; i++)
	{
		int s = subnumar1[i] + subnumar2[i];
		suma_partiala[i] = (s + carry) % 10;
		carry = (s + carry) / 10;
	}
	int received_carry = 0, j = 0;
	if (rank != 0)
		// primeste carry-ul de la procesul precedent
		MPI_Recv(&received_carry, 1, MPI_INT, rank - 1, 0, MPI_COMM_WORLD, &out);
	//actualizeaza suma si carry-ul
	while (received_carry != 0 && j < cifre_per_proces[rank]) {
		int s = subnumar1[j] + subnumar2[j];
		suma_partiala[j] = (s + received_carry) % 10;
		received_carry = (s + received_carry) / 10;
		j++;
	}
	if (j == cifre_per_proces[rank])
		carry = received_carry;

	// trimite carry la procesul urmator, daca nu am ajuns la ultimul proces
	if (rank != nr_procese - 1)
		MPI_Send(&carry, 1, MPI_INT, rank + 1, 0, MPI_COMM_WORLD);

	MPI_Gatherv(suma_partiala, cifre_per_proces[rank], MPI_INT, suma, cifre_per_proces, displs, MPI_INT, 0, MPI_COMM_WORLD);

	// procesul 0
	if (rank == 0) {
		// afisare rezultat in fisier si pe ecran
		print_result(n, suma);

		auto endTime = high_resolution_clock::now();
		int* suma_secventiala = new int[n];
		secvential(n1, numar1, n2, numar2, n, suma_secventiala);
		if (verifyIdenticalArrays(n, suma, suma_secventiala)) {
			cout << chrono::duration <double, milli>(endTime - startTime).count();
		}
		else {
			throw invalid_argument("Rezultat diferit fata de varianta secventiala!");
		}
	}

	MPI_Finalize();


	return 0;
}
