package com.example.yypome.db;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.yypome.data.CardStatistics;
import com.example.yypome.data.CardData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {CardStatistics.class}, version = 1, exportSchema = false)
public abstract class StatisticsDatabase extends RoomDatabase {

    private static StatisticsDatabase INSTANCE;
    private static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(1);

    public abstract StatisticsDao statisticsDao();

    public static synchronized StatisticsDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            StatisticsDatabase.class, "statistics_database")
                    .addCallback(new RoomDatabaseCallback(context))  // 传递 context
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }

    // 定义非静态的 RoomDatabase.Callback，并将 context 作为构造参数传入
    private static class RoomDatabaseCallback extends RoomDatabase.Callback {
        private final Context context;

        RoomDatabaseCallback(Context context) {
            this.context = context;
        }

        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            // 在数据库创建时执行初始化
            databaseWriteExecutor.execute(() -> {
                PoemDatabase poemDatabase = PoemDatabase.getDatabase(context);  // 使用传入的 context
                StatisticsDao statisticsDao = INSTANCE.statisticsDao();
                List<CardData> cardDataList = poemDatabase.poemDao().getAllCardDataSync();

                for (CardData cardData : cardDataList) {
                    CardStatistics cardStatistics = new CardStatistics(
                            cardData.getTitle(),
                            0, // 初始化学习次数为0
                            0,  // 错误次数也初始化为0
                            0,
                            0,  // 初始化为不添加该检查
                            0   // 初始化为不添加该复习卡片
                    );
                    statisticsDao.insertOrUpdateCardStatistics(cardStatistics);
                }
            });
        }
    }
}
