package com.example.yypome.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "files",
        foreignKeys = @ForeignKey(entity = Title.class, parentColumns = "id", childColumns = "titleId"))
public class FileNameWithImageUrl {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int titleId;
    public String fileName;
    public String imageUrl;

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTitleId() {
        return titleId;
    }

    public void setTitleId(int titleId) {
        this.titleId = titleId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
