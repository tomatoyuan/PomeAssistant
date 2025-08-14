package com.example.yypome.data;

public class MeListItem {

    private int iconResId;
    private String title;

    public MeListItem(int iconResId, String title) {
        this.iconResId = iconResId;
        this.title = title;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getTitle() {
        return title;
    }

}
