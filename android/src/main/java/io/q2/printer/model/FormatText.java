package io.q2.printer.model;

public class FormatText {
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getAlign() {
        return align;
    }

    public void setAlign(int align) {
        this.align = align;
    }

    public  String text;
    public int size;
    public int align;

    @Override
    public String toString() {
        return "FormatText";
    }
}
