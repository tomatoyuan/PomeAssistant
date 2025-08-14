package com.example.yypome.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.yypome.data.CardStatistics;

import java.util.List;

@Dao
public interface StatisticsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateCardStatistics(CardStatistics cardStatistics);

    @Query("SELECT * FROM card_statistics_table WHERE cardTitle = :cardTitle LIMIT 1")
    CardStatistics getCardStatisticsByTitle(String cardTitle);

    @Query("UPDATE card_statistics_table SET visitCount = visitCount + 1 WHERE cardTitle = :cardTitle")
    void incrementVisitCount(String cardTitle);

    @Query("UPDATE card_statistics_table SET incorrectCount = incorrectCount + 1 WHERE cardTitle = :cardTitle")
    void incrementIncorrectCount(String cardTitle);

    @Query("SELECT * FROM card_statistics_table")
    List<CardStatistics> getAllStatistics();

    // 更新指定卡片的 checkCardFlag 为 1
    @Query("UPDATE card_statistics_table SET checkCardFlag = 1 WHERE cardTitle = :title")
    void updateCheckCardFlagByTitle(String title);

    // 更新指定卡片的 checkCardFlag 为 0
    @Query("UPDATE card_statistics_table SET checkCardFlag = 0 WHERE cardTitle = :title")
    void clearCheckCardFlagByTitle(String title);

    // 获取所有checkCardFlag为1的卡片名称
    @Query("SELECT cardTitle FROM card_statistics_table WHERE checkCardFlag = 1")
    LiveData<List<String>> getTitlesWithCheckFlag();

    // 更新指定卡片的 reviewCardFlag 为 1
    @Query("UPDATE card_statistics_table SET reviewCardFlag = 1 WHERE cardTitle = :title")
    void updateReviewCardFlagByTitle(String title);

    // 更新指定卡片的 reviewCardFlag 为 0
    @Query("UPDATE card_statistics_table SET reviewCardFlag = 0 WHERE cardTitle = :title")
    void clearReviewCardFlagByTitle(String title);

    // 获取所有reviewCardFlag为1的卡片名称
    @Query("SELECT cardTitle FROM card_statistics_table WHERE reviewCardFlag = 1")
    LiveData<List<String>> getTitlesWithReviewFlag();
}
