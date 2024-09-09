import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws IOException {
        int nrPolynomials = 5; // nr de polinoame care va fi generat
        int maxMonoms = 100; // nr maxim de monoame pe care il poate avea un polinom
        int maxDegree = 10000; // gradul maxim pe care il poate avea un polinom

        for (int i = 0; i < nrPolynomials; i++) {
            String filename = "2.polinom-" + (i + 1) + ".txt";
            FileWriter writer = new FileWriter(filename);
            Random rand = new Random();
            String line;
            int nrMonoms = rand.nextInt(maxMonoms) + 1;
            for (int j = 0; j < nrMonoms; j++) {
                // coeficientii sunt din intervalul [-10000,10000)
                int coef = rand.ints(-10000, 10000)
                        .findFirst()
                        .getAsInt();

                // nu se pastreaza monoame cu coeficientul 0
                if (coef == 0)
                    coef++;
                int exp = rand.nextInt(maxDegree);
                line = coef + "," + exp;
                writer.write(line + "\n");
            }
            writer.close();
        }
    }
}