package com.example.yypome.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "daily_poetry_count_log")
public class DailyPoetryCountLog {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String date;
    private String title;

    public DailyPoetryCountLog(String date, String title) {
        this.date = date;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    // Constructor, getters, and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
