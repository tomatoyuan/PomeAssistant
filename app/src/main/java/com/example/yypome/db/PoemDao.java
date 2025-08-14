package com.example.yypome.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.yypome.data.AnswerResult;
import com.example.yypome.data.CardData;
import com.example.yypome.data.DailyErrorCount;
import com.example.yypome.data.DailyPoetryCountLog;
import com.example.yypome.data.RecitationResult;
import com.example.yypome.data.RecitationShortQaResult;
import com.example.yypome.data.ShortqaAnswerResult;
import com.example.yypome.data.TitleSuggestMethod;

import java.util.List;

@Dao
public interface PoemDao {

    @Query("SELECT * FROM card_data_table")
    LiveData<List<CardData>> getAllCardData();  // 返回 LiveData，Room 会处理线程

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CardData cardData);  // 插入数据

    // 更新 CardData
    @Update
    void update(CardData cardData);

    @Query("SELECT * FROM card_data_table WHERE title LIKE '%' || :query || '%'")
    List<CardData> searchCardDataByTitle(String query);  // 根据标题查询数据

    @Query("SELECT * FROM card_data_table WHERE title = :title LIMIT 1")
    CardData getCardDataByTitle(String title);

    @Query("SELECT * FROM card_data_table WHERE title = :title LIMIT 1")
    LiveData<CardData> getCardDataLiveByTitle(String title);

    @Query("SELECT * FROM card_data_table")
    List<CardData> getAllCardDataSync();

    // 获取前5条 CardData
    @Query("SELECT * FROM card_data_table LIMIT 5")
    LiveData<List<CardData>> getTopFiveCardData();

    // 查询指定标题的 nice_sentence 列表
    @Query("SELECT nice_sentence FROM card_data_table WHERE title = :title LIMIT 1")
    List<String> getNiceSentencesByTitle(String title);

    // 新增按 style 查询的接口
    @Query("SELECT * FROM card_data_table WHERE style = :style")
    LiveData<List<CardData>> getAllCardDataByStyle(String style);

    // 新增按 approach 查询的接口
    @Query("SELECT * FROM card_data_table WHERE approach = :approach")
    LiveData<List<CardData>> getAllCardDataByApproach(String approach);

    // 获取指定标题的 recitationCount 值
    @Query("SELECT recitation_count FROM card_data_table WHERE title = :title")
    int getRecitationCountByTitle(String title);

    // 将 recitationCount 字段递增 1
    @Query("UPDATE card_data_table SET recitation_count = recitation_count + 1 WHERE title = :title")
    void incrementRecitationCount(String title);

    // 重置或设置特定的 recitationCount 值
    @Query("UPDATE card_data_table SET recitation_count = :count WHERE title = :title")
    void updateRecitationCount(String title, int count);

    @Query("SELECT suggestMethod FROM card_data_table WHERE title = :title LIMIT 1")
    String getSuggestMethodByTitle(String title);

    // 更新方法：通过标题更新 suggestMethod 字段
    @Query("UPDATE card_data_table SET suggestMethod = :suggestMethod WHERE title = :title")
    void updateSuggestMethodByTitle(String title, String suggestMethod);

    // 获取<标题，推荐背诵方法>对，用于建立字典映射
    @Query("SELECT title, suggestMethod FROM card_data_table")
    List<TitleSuggestMethod> getAllTitlesAndSuggestMethods();

    // 插入背诵结果
    @Insert
    void insertRecitationResult(RecitationResult result);

    // 获取指定标题的所有简答题结果
    @Query("SELECT * FROM short_qa_recitation_result_table WHERE title = :title")
    List<RecitationResult> getRecitationResultsByTitle(String title);

    // 插入新的简答题结果
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecitationShortQaResult(RecitationShortQaResult result);

    // 获取指定标题的所有简答题结果
    @Query("SELECT * FROM short_qa_recitation_result_table WHERE title = :title")
    List<RecitationShortQaResult> getRecitationShortQaResultsByTitle(String title);

    @Insert
    void insertAnswerResult(AnswerResult answerResult);

    @Query("SELECT * FROM answer_result_table WHERE question_text = :questionText")
    List<AnswerResult> getAnswerResultsByQuestion(String questionText);

    // 根据 title 查询相关的问答记录和回答时间
    @Query("SELECT * FROM answer_result_table WHERE title = :title")
    List<AnswerResult> getAnswerResultsByTitle(String title);


    // 插入 ShortqaAnswerResult
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertShortqaAnswerResult(ShortqaAnswerResult answerResult);

    // 查询指定问题的 ShortqaAnswerResult
    @Query("SELECT * FROM shortqa_answer_result_table WHERE question_text = :questionText")
    List<ShortqaAnswerResult> getShortqaAnswerResultsByQuestion(String questionText);

    // 根据标题查询 ShortqaAnswerResult 记录
    @Query("SELECT * FROM shortqa_answer_result_table WHERE title = :title")
    List<ShortqaAnswerResult> getShortqaAnswerResultsByTitle(String title);

    // 插入或更新指定日期的错题记录
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertDailyErrorCount(DailyErrorCount dailyErrorCount);

    @Query("UPDATE daily_error_count_table SET errorCount = errorCount + 1 WHERE date = :date")
    void incrementErrorCount(String date);

    @Query("SELECT * FROM daily_error_count_table WHERE date = :date")
    DailyErrorCount getErrorCountByDate(String date);

    // 新增 poetryCount 和 exerciseCount 增量的操作
    @Query("UPDATE daily_error_count_table SET poetryCount = poetryCount + 1 WHERE date = :date")
    void incrementPoetryCount(String date);

    @Query("UPDATE daily_error_count_table SET exerciseCount = exerciseCount + 1 WHERE date = :date")
    void incrementExerciseCount(String date);

    @Update
    void updateDailyErrorCount(DailyErrorCount dailyErrorCount);

    // 获取本周内每日的错题数
    @Query("SELECT * FROM daily_error_count_table WHERE date BETWEEN :monday AND :sunday ORDER BY date ASC")
    List<DailyErrorCount> getWeeklyErrorCounts(String monday, String sunday);

    @Query("SELECT * FROM daily_error_count_table WHERE date = :date LIMIT 1")
    DailyErrorCount getDailyErrorCountByDate(String date);

    // 插入新统计记录
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertDailyPoetryCountLog(DailyPoetryCountLog log);

    // 检查是否已统计该 title 在当天
    @Query("SELECT COUNT(*) FROM daily_poetry_count_log WHERE date = :date AND title = :title")
    int hasPoetryCountLogForTitle(String date, String title);

    @Query("SELECT * FROM daily_error_count_table WHERE date BETWEEN :monday AND :sunday ORDER BY date ASC")
    List<DailyErrorCount> getWeeklyPoetryCounts(String monday, String sunday);

    // 事务方法
    @Transaction
    default void incrementCounts(String date, boolean isPoetry, boolean isExercise, boolean isError) {
        DailyErrorCount dailyCount = getDailyErrorCountByDate(date);

        if (dailyCount == null) {
            dailyCount = new DailyErrorCount(date, isError ? 1 : 0, isPoetry ? 1 : 0, isExercise ? 1 : 0);
            insertDailyErrorCount(dailyCount);  // 插入新记录
        } else {
            if (isError) incrementErrorCount(date);
            if (isPoetry) incrementPoetryCount(date);
            if (isExercise) incrementExerciseCount(date);
        }
    }

}
