package io.q2.printer.model;

public class QRCode {

    public String text;
    public int mErrorCorrectionLevel;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getmErrorCorrectionLevel() {
        return mErrorCorrectionLevel;
    }

    public void setmErrorCorrectionLevel(int mErrorCorrectionLevel) {
        this.mErrorCorrectionLevel = mErrorCorrectionLevel;
    }

    public int getModulesize() {
        return modulesize;
    }

    public void setModulesize(int modulesize) {
        this.modulesize = modulesize;
    }

    public int modulesize;

    @Override
    public String toString() {
        return "QRCode";
    }
}
