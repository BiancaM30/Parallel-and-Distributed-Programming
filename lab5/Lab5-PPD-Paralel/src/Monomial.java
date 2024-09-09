import java.util.concurrent.locks.ReentrantLock;

class Monomial {
    protected int coefficient;
    protected int exponent;
    protected Monomial next;
    protected Monomial prev;
    protected ReentrantLock nodeLock;

    public Monomial(int coefficient, int exponent) {
        this.coefficient = coefficient;
        this.exponent = exponent;
        this.next = null;
        this.prev = null;
        this.nodeLock = new ReentrantLock();
    }
}