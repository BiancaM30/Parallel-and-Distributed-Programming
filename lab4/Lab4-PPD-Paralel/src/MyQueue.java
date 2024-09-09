public class MyQueue {
    private int size;
    private Monomial head;
    private Monomial tail;

    public MyQueue() {
        this.size = 0;
        head = tail = null;
    }

    // add element to queue
    synchronized public void enqueue(Monomial Monomial) {
        if (head == null)
            head = tail = Monomial;
        else {
            tail.next = Monomial;
            tail = Monomial;
        }
        size++;
    }

    // remove first element from queue
    synchronized public Monomial dequeue(){
        Monomial removedMonomial = head;
        if (head.next == null) {
            head = tail = null;
        } else {
            head = head.next;
        }
        size--;
        return removedMonomial;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    public int getSize() {
        return size;
    }
}