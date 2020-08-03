package io.q2.printer.model;

import androidx.annotation.NonNull;

public class PImage {

    private  String img;
    private  int size;

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
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

    private int align;

    @Override
    public String toString() {
        return "Image";
    }
}
