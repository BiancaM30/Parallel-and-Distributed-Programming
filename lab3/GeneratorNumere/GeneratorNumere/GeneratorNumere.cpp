#include <stdlib.h>
#include <fstream>
#include <iostream>

using namespace std;

int main() {
	int n1, n2;
	cout << "n1 = ";
	cin >> n1;
	cout << "\nn2 = ";
	cin >> n2;
	
	ofstream out1("numar1.txt");
	ofstream out2("numar2.txt");
	out1 << n1 << endl;
	out2 << n2 << endl;
	if (n1 == 18 && n2 == 18) {
		out1 << "123456789123456789" << endl;
		out2 << "123456789123456789" << endl;
	}
	else {
		srand((unsigned)time(0));
		for (int i = 0; i < n1; i++) {
			out1 << rand() % 10;
		}
		for (int i = 0; i < n2; i++) {
			out2 << rand() % 10;
		}
	}
	out1.close();
	out2.close();
}

