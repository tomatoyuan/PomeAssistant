package com.example.yypome.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.yypome.data.FileNameWithImageUrl;

@Dao
public interface FileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertFile(FileNameWithImageUrl file);

    // 通过 fileName 查询 imageUrl
    @Query("SELECT imageUrl FROM files WHERE fileName = :fileName LIMIT 1")
    String getImageUrlByFileName(String fileName);
}
