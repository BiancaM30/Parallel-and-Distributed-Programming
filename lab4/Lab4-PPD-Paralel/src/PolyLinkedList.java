import java.io.*;
import java.util.concurrent.locks.ReentrantLock;

public class PolyLinkedList {
    private Monomial head;

    private ReentrantLock lock = new ReentrantLock();

    public PolyLinkedList() {
    }

    public Monomial insert(Monomial monomial) {
        lock.lock();
        // if list is empty , we mark the new node as head
        if (head == null) {
            head = monomial;
            lock.unlock();
        } else {
            if (head.exponent >= monomial.exponent) {    // set current monomial as head
                if (head.exponent == monomial.exponent) // if they have the same exponent, we just increase the coefficient
                    head.coefficient += monomial.coefficient;
                else {
                    monomial.next = head;
                    head = monomial;
                }
                lock.unlock();
            } else {
                lock.unlock();
                Monomial current = head;
                while (current.next != null && current.next.exponent <= monomial.exponent)
                    current = current.next;
                lock.lock();
                if (current.exponent == monomial.exponent) {  // if they have the same exponent, we just increase the coefficient
                    current.coefficient += monomial.coefficient;
                } else {      // add before current monomial
                    monomial.next = current.next;
                    current.next = monomial;
                }
                lock.unlock();
            }
        }
        return monomial;
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
