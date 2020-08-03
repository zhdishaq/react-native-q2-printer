package io.q2.printer.model;

public class SimpleText {
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    private String text;

    @Override
    public String toString() {
        return "SimpleText";
    }
}
