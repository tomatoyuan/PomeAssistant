package com.example.yypome.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.yypome.data.Title;
import com.example.yypome.data.TitleWithFileNames;

import java.util.List;

@Dao
public interface TitleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTitle(Title title);

    @Transaction
    @Query("SELECT * FROM titles WHERE title = :title")
    List<TitleWithFileNames> getTitleWithFiles(String title);
}
