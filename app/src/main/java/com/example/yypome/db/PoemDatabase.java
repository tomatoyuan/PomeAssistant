package com.example.yypome.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.yypome.data.AnswerResult;
import com.example.yypome.data.CardData;
import com.example.yypome.data.DailyErrorCount;
import com.example.yypome.data.DailyPoetryCountLog;
import com.example.yypome.data.RecitationResult;
import com.example.yypome.data.RecitationShortQaResult;
import com.example.yypome.data.ShortqaAnswerResult;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {CardData.class, RecitationResult.class, RecitationShortQaResult.class, AnswerResult.class, ShortqaAnswerResult.class, DailyErrorCount.class, DailyPoetryCountLog.class}, version = 1, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class PoemDatabase extends RoomDatabase {

    private static volatile PoemDatabase INSTANCE;

    public abstract PoemDao poemDao();

    // 定义数据库写操作的 ExecutorService
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4); // 可以根据需要调整线程池大小

    public static PoemDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (PoemDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    PoemDatabase.class, "poem_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

