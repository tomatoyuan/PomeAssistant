package com.example.yypome.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "titles")
public class Title {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;

    public Title(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}