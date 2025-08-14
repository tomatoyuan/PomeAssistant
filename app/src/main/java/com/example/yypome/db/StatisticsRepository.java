package com.example.yypome.db;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.yypome.data.CardStatistics;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StatisticsRepository {
    private static final String TAG = "StatisticsRepository";
    private StatisticsDao statisticsDao;
    private Executor executor = Executors.newSingleThreadExecutor();

    public StatisticsRepository(Context context) {
        StatisticsDatabase db = StatisticsDatabase.getDatabase(context);
        statisticsDao = db.statisticsDao();
    }

    // 更新访问次数
    public void incrementVisitCount(String title) {
        executor.execute(() -> {
            try {
                statisticsDao.incrementVisitCount(title);
                Log.d(TAG, "Incremented visit count for title: " + title);
            } catch (Exception e) {
                Log.e(TAG, "Error incrementing visit count", e);
            }
        });
    }

    // 更新错题次数
    public void incrementIncorrectCount(String title) {
        executor.execute(() -> {
            try {
                statisticsDao.incrementIncorrectCount(title);
                Log.d(TAG, "Incremented incorrect count for title: " + title);
            } catch (Exception e) {
                Log.e(TAG, "Error incrementing incorrect count", e);
            }
        });
    }

    // 插入或更新 CardStatistics 数据
    public void insertOrUpdateCardStatistics(CardStatistics cardStatistics) {
        executor.execute(() -> {
            try {
                statisticsDao.insertOrUpdateCardStatistics(cardStatistics);
                Log.d(TAG, "Inserted or updated card statistics for title: " + cardStatistics.getCardTitle());
            } catch (Exception e) {
                Log.e(TAG, "Error inserting/updating card statistics", e);
            }
        });
    }

    // 插入或更新卡片初始数据
    public void insertInitialData(String cardTitle, int initialVisitCount) {
        executor.execute(() -> {
            CardStatistics cardStatistics = new CardStatistics(cardTitle, initialVisitCount, 0, 0, 0, 0); // 初始化访问次数为0，错误次数为0
            statisticsDao.insertOrUpdateCardStatistics(cardStatistics);
        });
    }

    // 更新指定卡片的 checkCardFlag 为 1
    public void updateCheckCardFlag(String title) {
        executor.execute(() -> statisticsDao.updateCheckCardFlagByTitle(title));
    }

    // 更新指定卡片的 checkCardFlag 为 0
    public void clearCheckCardFlag(String title) {
        executor.execute(() -> statisticsDao.clearCheckCardFlagByTitle(title));
    }

    // 获取所有checkCardFlag为1的卡片名称
    public LiveData<List<String>> getTitlesWithCheckFlag() {
        return statisticsDao.getTitlesWithCheckFlag();
    }

    // 更新指定卡片的 reviewCardFlag 为 1
    public void updateReviewCardFlag(String title) {
        executor.execute(() -> statisticsDao.updateReviewCardFlagByTitle(title));
    }

    // 更新指定卡片的 reviewCardFlag 为 0
    public void clearReviewCardFlag(String title) {
        executor.execute(() -> statisticsDao.clearReviewCardFlagByTitle(title));
    }

    // 获取所有reviewCardFlag为1的卡片名称
    public LiveData<List<String>> getTitlesWithReviewFlag() {
        return statisticsDao.getTitlesWithReviewFlag();
    }

}
