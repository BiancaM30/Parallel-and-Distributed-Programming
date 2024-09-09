import java.io.*;
import java.util.concurrent.locks.ReentrantLock;

public class PolyLinkedList {
    private Monomial head;

    private ReentrantLock listLock = new ReentrantLock(); // used only for the first element

    public PolyLinkedList() {
    }

    // insert node to list
    public Monomial insert(Monomial monomial) {
        // if list is empty , we mark the new node as head
        if (head == null) {
            listLock.lock();
            head = monomial;
            listLock.unlock();
        } else {
            head.nodeLock.lock();
            if (head.exponent >= monomial.exponent) {    // set current monomial as head
                if (head.exponent == monomial.exponent) { // if they have the same exponent, we just increase the coefficient
                    head.coefficient += monomial.coefficient;
                    if (head.coefficient == 0) { // if coefficient is 0, we remove the monomial
                        deleteMonomial(head);
                    }
                    head.nodeLock.unlock();
                } else { // add monomial before head
                    monomial.next = head;
                    monomial.next.prev = monomial;
                    head = monomial;
                    head.next.nodeLock.unlock(); // unlock previous head
                }
            } else {
                Monomial current = head;

                if (current.next != null) {
                    current.next.nodeLock.lock();
                }

                while (current.next != null && current.next.exponent <= monomial.exponent) {
                    current = current.next;
                    if (current.next != null)
                        current.next.nodeLock.lock();
                    current.prev.nodeLock.unlock();
                }

                // current and current.next are locked
                if (current.exponent == monomial.exponent) {  // if they have the same exponent, we just increase the coefficient
                    current.coefficient += monomial.coefficient;
                    if (current.coefficient == 0) { // if coefficient is 0, we remove the monomial
                        current.prev.nodeLock.lock();
                        deleteMonomial(current);
                        current.prev.nodeLock.unlock();
                    }
                    if (current.next != null) {
                        current.next.nodeLock.unlock();
                    }
                    current.nodeLock.unlock();
                } else {      // add before current monomial
                    monomial.next = current.next;
                    if (current.next != null) {
                        monomial.next.prev = monomial;
                    }
                    current.next = monomial;
                    monomial.prev = current;

                    // unlock current and current.next that is now represented by 'monomial'
                    if (monomial.next != null) {
                        monomial.next.nodeLock.unlock();
                    }
                    current.nodeLock.unlock();
                }
            }
        }
        return monomial;
    }

    // remove node from list
    public void deleteMonomial(Monomial toDelete) {
        if (head == toDelete)
            head = toDelete.next;
        if (toDelete.next != null) {
            toDelete.next.prev = toDelete.prev;
        }
        if (toDelete.prev != null) {
            toDelete.prev.next = toDelete.next;
        }
    }


    public void writeResultPolyToFile(String filename) throws IOException {
        FileWriter writer = new FileWriter(filename);
        Monomial monomial = head;
        String line;
        while (monomial != null) {
            line = monomial.coefficient + "," + monomial.exponent + "\n";
            writer.write(line);
            monomial = monomial.next;
        }
        writer.close();

    }
}
