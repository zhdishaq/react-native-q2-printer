package io.q2.printer.model;


import com.facebook.react.bridge.ReadableArray;

import java.util.ArrayList;
import java.util.Arrays;


public class Table {


    public ReadableArray getRows() {
        return rows;
    }

    public void setRows(ReadableArray rows) {
        this.rows = rows;
    }

    public int[] getColumnwidth() {
        return columnwidth;
    }

    public void setColumnwidth(int[] columnwidth) {
        this.columnwidth = columnwidth;
    }

    public int[] getColumnalign() {
        return columnalign;
    }

    public void setColumnalign(int[] columnalign) {
        this.columnalign = columnalign;
    }

    public int getFontsize() {
        return fontsize;
    }

    public void setFontsize(int fontsize) {
        this.fontsize = fontsize;
    }

    public int getTablealign() {
        return tablealign;
    }

    public void setTablealign(int tablealign) {
        this.tablealign = tablealign;
    }

    @Override
    public String toString() {
        return "Table" ;
    }

    public ReadableArray rows;

    public int[] columnwidth;
    public int[] columnalign;
    public int fontsize;
    public int tablealign;

}

