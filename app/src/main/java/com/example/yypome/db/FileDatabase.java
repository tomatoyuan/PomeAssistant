package com.example.yypome.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.yypome.data.FileNameWithImageUrl;
import com.example.yypome.data.Title;

@Database(entities = {Title.class, FileNameWithImageUrl.class}, version = 1)
public abstract class FileDatabase extends RoomDatabase {

    public abstract FileDao fileDao();
    public abstract TitleDao titleDao();

    private static volatile FileDatabase INSTANCE;

    public static FileDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (FileDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    FileDatabase.class, "file_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
