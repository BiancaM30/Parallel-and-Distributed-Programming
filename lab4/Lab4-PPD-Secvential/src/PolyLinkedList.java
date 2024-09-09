import java.io.*;
import java.util.List;

public class PolyLinkedList {
    private Monomial head;

    public PolyLinkedList(List<String> filenames) throws IOException {
        for (String filename : filenames) {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                var fields = line.split(",");
                Monomial nod = new Monomial(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]));
                insert(nod);
            }
            reader.close();
        }
    }

    public void insert(Monomial monomial) {
        // if list is empty , we mark the new node as head
        if (head == null)
            head = monomial;
        else {
            if (head.exponent >= monomial.exponent) {    // set current monomial as head
                if (head.exponent == monomial.exponent) // if they have the same exponent, we just increase the coefficient
                    head.coefficient += monomial.coefficient;
                else {
                    monomial.next = head;
                    head = monomial;
                }
            } else {
                Monomial current = head;
                while (current.next != null && current.next.exponent <= monomial.exponent)
                    current = current.next;
                if (current.exponent == monomial.exponent) {  // if they have the same exponent, we just increase the coefficient
                    current.coefficient += monomial.coefficient;
                } else {      // add before current monomial
                    monomial.next = current.next;
                    current.next = monomial;
                }
            }
        }
    }

    public void writeResultPolyToFile(String filename) throws IOException {
        FileWriter writer = new FileWriter(filename);
        Monomial monomial = head;
        String line;
        while (monomial != null) {
            if (monomial.coefficient != 0) {
                line = monomial.coefficient + "," + monomial.exponent + "\n";
                writer.write(line);
            }
            monomial = monomial.next;
        }
        writer.close();

    }
}
