package com.example.yypome.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "daily_error_count_table")
public class DailyErrorCount {
    @PrimaryKey
    @NonNull
    private String date;  // 日期，格式为 "yyyy-MM-dd"
    private int errorCount;  // 当天的错题数
    private int poetryCount; // 当天背诵的诗词数
    private int exerciseCount; // 当天完成的习题数

    public DailyErrorCount(@NonNull String date, int errorCount, int poetryCount, int exerciseCount) {
        this.date = date;

        this.errorCount = errorCount;
        this.poetryCount = poetryCount;
        this.exerciseCount = exerciseCount;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public int getPoetryCount() {
        return poetryCount;
    }

    public void setPoetryCount(int poetryCount) {
        this.poetryCount = poetryCount;
    }

    public int getExerciseCount() {
        return exerciseCount;
    }

    public void setExerciseCount(int exerciseCount) {
        this.exerciseCount = exerciseCount;
    }

    // 自增错题数
    public void incrementErrorCount() {
        this.errorCount += 1;
    }

    // 自增诗词数
    public void incrementPoetryCount() {
        this.poetryCount += 1;
    }

    // 自增习题数
    public void incrementExerciseCount() {
        this.exerciseCount += 1;
    }
}
