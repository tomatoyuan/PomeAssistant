package com.example.yypome.data;

public class Sentence {

    private String text;
    private String imageResUrl; // 图片资源ID

    public Sentence(String text, String imageResUrl) {
        this.text = text;
        this.imageResUrl = imageResUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageResUrl() {
        return imageResUrl;
    }

    public void setImageResUrl(String imageResUrl) {
        this.imageResUrl = imageResUrl;
    }
}
