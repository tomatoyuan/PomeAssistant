package com.example.yypome.imageIdentity;

import android.graphics.Rect;

public class OcrResult {

    private String words;
    private Rect location;

    public OcrResult(String words, Rect location) {
        this.words = words;
        this.location = location;
    }

    public String getWords() {
        return words;
    }

    public Rect getLocation() {
        return location;
    }

}
