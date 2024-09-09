class Monomial {
    protected int coefficient;
    protected int exponent;
    protected Monomial next;

    public Monomial(int coefficient, int exponent) {
        this.coefficient = coefficient;
        this.exponent = exponent;
        this.next = null;
    }
}