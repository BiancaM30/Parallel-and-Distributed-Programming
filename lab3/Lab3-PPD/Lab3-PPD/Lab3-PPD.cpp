#include <iostream>
#include <fstream>
#include <chrono>
#include <mpi.h>

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
	ofstream out("C:/Facultate/Cursuri facultate sem 5/PPD/Labs/Teme/lab3/Lab3-PPD/Lab3-PPD/Numar3.txt");
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

// varianta 1 (send, recv)
int main() {
	// initializare MPI
	int rank, nr_procese;
	int rc = MPI_Init(NULL, NULL);
	if (rc != MPI_SUCCESS)
	{
		cout << "Err init MPI";
		MPI_Abort(MPI_COMM_WORLD, rc);
	}
	MPI_Comm_size(MPI_COMM_WORLD, &nr_procese);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);
	//printf("Proces %d din %d\n", rank, nr_procese);

	//citim lungimile celor 2 numare
	int n = 0, n1 = 0, n2 = 0;
	ifstream in1("C:/Facultate/Cursuri facultate sem 5/PPD/Labs/Teme/lab3/Lab3-PPD/Lab3-PPD/Numar1.txt");
	ifstream in2("C:/Facultate/Cursuri facultate sem 5/PPD/Labs/Teme/lab3/Lab3-PPD/Lab3-PPD/Numar2.txt");
	in1 >> n1;
	in2 >> n2;

	//lungime rezultat
	n = max(n1, n2) + 1;

	// Varianta 0 - secvential
	if (nr_procese == 1) {
		auto startTimeSecv = high_resolution_clock::now();

		int* numar1 = new int[n];
		int* numar2 = new int[n];
		int* suma = new int[n];
		// citim numerele in tablouri de cifre, in care cifra cea mai nesimnificativa se afla pe pe prima pozitie 
		// completam cu zerouri pentru a aduce numerele la aceeasi lungime
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
		in1.close();
		in2.close();

		// calcul suma
		secvential(n1, numar1, n2, numar2, n, suma);

		// afisare rezultat
		print_result(n, suma);

		auto endTimeSecv = high_resolution_clock::now();
		cout << chrono::duration <double, milli>(endTimeSecv - startTimeSecv).count();
	}

	// Varianta 1
	else {
		auto startTimeV1 = high_resolution_clock::now();

		int cat = n / (nr_procese - 1);
		int rest = n % (nr_procese - 1);

		// procesul 0
		if (rank == 0) {
			int* numar1 = new int[n];
			int* numar2 = new int[n];
			int* suma = new int[n];
			int* cifre_per_proces = new int[nr_procese];
			//calculam cate cifre trebuie sa trimitem la fiecare proces
			cifre_per_proces[0] = 0;
			for (int i = 1; i < nr_procese; i++) {
				if (i <= rest) {
					cifre_per_proces[i] = cat + 1;
				}
				else {
					cifre_per_proces[i] = cat;
				}
			}

			int id_proces_curent = nr_procese - 1;
			int cifre_citite = 0;
			for (int i = n - 1; i >= 0; i--) {
				// fiecare proces citeste cate n/p cifre din fisiere
				char cifra;
				cifre_citite++;
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

				// trimite subliste procesului "id_proces_curent"
				if (cifre_citite == cifre_per_proces[id_proces_curent]) {
					MPI_Send(numar1 + i, cifre_per_proces[id_proces_curent], MPI_INT, id_proces_curent, 20, MPI_COMM_WORLD);
					MPI_Send(numar2 + i, cifre_per_proces[id_proces_curent], MPI_INT, id_proces_curent, 30, MPI_COMM_WORLD);
					// decrementeaza "id_proces_curent"
					id_proces_curent--;
					cifre_citite = 0;
				}
			}
			in1.close();
			in2.close();

			// primeste sumele partiale de la procese si le reuneste intr-un vectorul rezultat "suma"
			MPI_Status out;
			for (int i = 1; i < nr_procese; i++) {
				int poz = 0;
				for (int j = 0; j < i; j++) {
					poz += cifre_per_proces[j];
				}
				MPI_Recv(suma + poz, cifre_per_proces[i], MPI_INT, i, 40, MPI_COMM_WORLD, &out);
			}

			// afisare rezultat in fisier si pe eccran
			print_result(n, suma);

			auto endTimeV1 = high_resolution_clock::now();
			int* suma_secventiala = new int[n];
			secvential(n1, numar1, n2, numar2, n, suma_secventiala);
			if (verifyIdenticalArrays(n, suma, suma_secventiala)) {
				cout << chrono::duration <double, milli>(endTimeV1 - startTimeV1).count();
			}
			else {
				throw invalid_argument("Rezultat diferit fata de varianta secventiala!");
			}
		}
		//proces != 0 
		else {
			// initializare variabile
			in1.close();
			in2.close();
			if (rank <= rest)
				cat++;
			int* subnumar1 = new int[cat];
			int* subnumar2 = new int[cat];
			int* suma_partiala = new int[cat];

			// Varianta b)
			// procesele primesc cifrele pe care trebuie sa le adune si apoi carry de la precedent
			MPI_Status out;
			MPI_Recv(subnumar1, cat, MPI_INT, 0, 20, MPI_COMM_WORLD, &out);
			MPI_Recv(subnumar2, cat, MPI_INT, 0, 30, MPI_COMM_WORLD, &out);

			// calculeaza suma numerelor si carry-ul curent
			int carry = 0;
			for (int i = 0; i < cat; i++)
			{
				int s = subnumar1[i] + subnumar2[i];
				suma_partiala[i] = (s + carry) % 10;
				carry = (s + carry) / 10;
			}

			// adaugam carry-ul de la procesul precedent
			int received_carry = 0, j = 0;
			if (rank != 1)
				MPI_Recv(&received_carry, 1, MPI_INT, rank - 1, 50, MPI_COMM_WORLD, &out);
			while (received_carry != 0 && j < cat) {
				int s = subnumar1[j] + subnumar2[j];
				suma_partiala[j] = (s + received_carry) % 10;
				received_carry = (s + received_carry) / 10;
				j++;
			}
			if (j == cat)
				carry = received_carry;

			// trimite carry la procesul urmator, daca nu am ajuns la ultimul proces
			if (rank != nr_procese - 1)
				MPI_Send(&carry, 1, MPI_INT, rank + 1, 50, MPI_COMM_WORLD);

			// trimite suma partiala la procesul 0
			MPI_Send(suma_partiala, cat, MPI_INT, 0, 40, MPI_COMM_WORLD);
		}
	}

	MPI_Finalize();

	return 0;
}
