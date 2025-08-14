package com.example.yypome.data;

public class TitleSuggestMethod {
    private String title;
    private String suggestMethod;

    public TitleSuggestMethod(String title, String suggestMethod) {
        this.title = title;
        this.suggestMethod = suggestMethod;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSuggestMethod() {
        return suggestMethod;
    }

    public void setSuggestMethod(String suggestMethod) {
        this.suggestMethod = suggestMethod;
    }
}
