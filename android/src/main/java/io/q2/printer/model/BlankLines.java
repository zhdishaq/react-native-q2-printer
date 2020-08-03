package io.q2.printer.model;

public class BlankLines {

    public int getLines() {
        return lines;
    }

    public void setLines(int lines) {
        this.lines = lines;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    private int lines;
    private int height;

    @Override
    public String toString() {
        return "BlankLines" ;
    }
}
