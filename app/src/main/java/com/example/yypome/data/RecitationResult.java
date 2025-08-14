package com.example.yypome.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "recitation_result_table")
public class RecitationResult {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String resultText; // 存储带颜色标记的文本
    private String date; // 存储日期

    public RecitationResult(String title, String resultText, String date) {
        this.title = title;
        this.resultText = resultText;
        this.date = date;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getResultText() { return resultText; }
    public void setResultText(String resultText) { this.resultText = resultText; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}
