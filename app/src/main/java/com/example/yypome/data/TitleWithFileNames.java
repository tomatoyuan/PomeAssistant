package com.example.yypome.data;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class TitleWithFileNames {
    @Embedded
    public Title title;

    @Relation(parentColumn = "id", entityColumn = "titleId")
    public List<FileNameWithImageUrl> files;

    // Getters and setters
    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public List<FileNameWithImageUrl> getFiles() {
        return files;
    }

    public void setFiles(List<FileNameWithImageUrl> files) {
        this.files = files;
    }
}
