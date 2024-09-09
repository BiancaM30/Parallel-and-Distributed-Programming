public class TemporaryCopy {
    private final int line, column, newValue;

    public TemporaryCopy(int line, int column, int newValue) {
        this.line = line;
        this.column = column;
        this.newValue = newValue;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
    public int getNewValue() {
        return newValue;
    }
}