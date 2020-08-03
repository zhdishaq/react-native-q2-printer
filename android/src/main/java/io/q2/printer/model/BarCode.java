package io.q2.printer.model;

public class BarCode {

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getSymbology() {
        return symbology;
    }

    public void setSymbology(int symbology) {
        this.symbology = symbology;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getAlign() {
        return align;
    }

    public void setAlign(int align) {
        this.align = align;
    }

    @Override
    public String toString() {
        return "BarCode";
    }

    public String text;
    public int symbology;
    public int height;
    public  int width;
    public int align;

}
