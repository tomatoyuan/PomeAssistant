package com.example.yypome.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.yypome.data.AnswerResult;
import com.example.yypome.data.CardData;
import com.example.yypome.data.DailyErrorCount;
import com.example.yypome.data.DailyPoetryCountLog;
import com.example.yypome.data.DateUtils;
import com.example.yypome.data.RecitationResult;
import com.example.yypome.data.RecitationShortQaResult;
import com.example.yypome.data.ShortqaAnswerResult;
import com.example.yypome.data.TitleSuggestMethod;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PoemRepository {
    private static final String TAG = "PoemRepository";
    private PoemDao poemDao;
    private Executor executor = Executors.newSingleThreadExecutor();
    private final Context context;

    public PoemRepository(Context context) {
        this.context = context.getApplicationContext(); // 使用应用级 context 避免内存泄漏
        PoemDatabase db = PoemDatabase.getDatabase(context);
        poemDao = db.poemDao();
    }

    public void saveCardData(CardData cardData) {
        try {
            Log.d(TAG, "准备保存 CardData 数据: " + new Gson().toJson(cardData));  // 打印即将保存的数据
            if (cardData.getQuestions() == null) {
                cardData.setQuestions(new ArrayList<>());  // 确保 questions 不为空
            }
            if (cardData.getShort_answer_question() == null) {
                cardData.setShort_answer_question(new ArrayList<>());  // 确保 questions 不为空
            }
            poemDao.insert(cardData);
        } catch (Exception e) {
            Log.e("PoemRepository", "Error inserting data", e);
        }
    }

    public LiveData<List<CardData>> getAllCardData() {
        return poemDao.getAllCardData();  // 执行同步查询
    }

    // 查询数据库中标题包含指定关键词的 CardData
    public List<CardData> searchCardDataByTitle(String query) {
        return poemDao.searchCardDataByTitle(query);
    }

    public List<CardData> getAllCardDataSync() {
        return poemDao.getAllCardDataSync();  // Dao 中需要添加同步查询的方法
    }

    public CardData getCardDataByTitle(String title) {
        return poemDao.getCardDataByTitle(title);
    }

    public LiveData<CardData> getCardDataLiveByTitle(String title) {
        return poemDao.getCardDataLiveByTitle(title);
    }


    // 获取前5条数据
    public LiveData<List<CardData>> getTopFiveCardData() {
        return poemDao.getTopFiveCardData();
    }

    // 新增获取 nice_sentence 的方法
    public List<String> getNiceSentencesByTitle(String title) {
        return poemDao.getNiceSentencesByTitle(title);  // 执行查询
    }

    public void updateCardData(CardData cardData) {
        try {
            poemDao.update(cardData);  // 使用 update 方法更新数据
            Log.d("PoemRepository", "Data updated for: " + cardData.getTitle());
        } catch (Exception e) {
            Log.e("PoemRepository", "Error updating data", e);
        }
    }

    // 新增按 style 查询的方法
    public LiveData<List<CardData>> getAllCardDataByStyle(String style) {
        return poemDao.getAllCardDataByStyle(style);
    }

    // 新增按 style 查询的方法
    public LiveData<List<CardData>> getAllCardDataByApproach(String approach) {
        return poemDao.getAllCardDataByApproach(approach);
    }

    // 通过 title 查询并更新 CompletionDate
    public void updateCompletionDateByTitle(String title) {
        executor.execute(() -> {
            try {
                // 获取当前日期，格式为 "yyyy/MM/dd"
                String currentDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());

                // 查询指定 title 的 CardData
                CardData cardData = poemDao.getCardDataByTitle(title);

                if (cardData != null) {
                    // 更新 CompletionDate 字段
                    cardData.setCompletionDate(currentDate);
                    poemDao.update(cardData);  // 保存更改到数据库
                    Log.d(TAG, "Updated CompletionDate for title: " + title);
                } else {
                    Log.d(TAG, "No CardData found with title: " + title);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating CompletionDate", e);
            }
        });
    }

    // 查询指定标题的 recitationCount 值
    public void getRecitationCount(String title, RecitationCountCallback callback) {
        executor.execute(() -> {
            int count = poemDao.getRecitationCountByTitle(title);
            callback.onCountLoaded(count);
        });
    }

    // 增加 recitationCount
    public void incrementRecitationCount(String title) {
        executor.execute(() -> {
            poemDao.incrementRecitationCount(title);
            Log.d(TAG, "Incremented recitationCount for title: " + title);
        });
    }

    // 设置 recitationCount 为指定值
    public void setRecitationCount(String title, int count) {
        executor.execute(() -> {
            poemDao.updateRecitationCount(title, count);
            Log.d(TAG, "Set recitationCount for title: " + title + " to: " + count);
        });
    }

    // 回调接口，用于获取异步 recitationCount 的结果
    public interface RecitationCountCallback {
        void onCountLoaded(int count);
    }

    // 获取方法：通过标题获取 suggestMethod 字段
    public void getSuggestMethodByTitle(String title, SuggestMethodCallback callback) {
        executor.execute(() -> {
            String suggestMethod = poemDao.getSuggestMethodByTitle(title);
            callback.onSuggestMethodLoaded(suggestMethod);
        });
    }

    // 回调接口，用于在异步任务完成后返回结果
    public interface SuggestMethodCallback {
        void onSuggestMethodLoaded(String suggestMethod);
    }

    // 更新方法：通过标题更新 suggestMethod 字段
    public void updateSuggestMethodByTitle(String title, String suggestMethod) {
        executor.execute(() -> poemDao.updateSuggestMethodByTitle(title, suggestMethod));
    }

    // 获取所有 title 和 suggestMethod 的字典，同步方法
    public Map<String, String> getSuggestMethodMapSync() {
        List<TitleSuggestMethod> titleSuggestMethods = poemDao.getAllTitlesAndSuggestMethods();
        Map<String, String> suggestMethodMap = new HashMap<>();
        for (TitleSuggestMethod item : titleSuggestMethods) {
            suggestMethodMap.put(item.getTitle(), item.getSuggestMethod());
        }
        return suggestMethodMap;
    }

    // 插入背诵结果
    public void insertRecitationResult(RecitationResult result) {
        executor.execute(() -> poemDao.insertRecitationResult(result));
    }

    // 同步获取背诵结果
    public List<RecitationResult> getRecitationResultsByTitle(String title) {
        return poemDao.getRecitationResultsByTitle(title);
    }

    // 插入简答题回答
    public void insertNewRecitationResult(RecitationShortQaResult result) {
        executor.execute(() -> poemDao.insertRecitationShortQaResult(result));
    }

    // 获取指定标题的所有简答题回答
    public List<RecitationShortQaResult> getRecitationShortQaResult(String title) {
        return poemDao.getRecitationShortQaResultsByTitle(title);
    }

    // 记录理解性默写的内容
    public void insertAnswerResult(AnswerResult answerResult) {
        executor.execute(() -> poemDao.insertAnswerResult(answerResult));
    }

    public List<AnswerResult> getAnswerResultsByTitle(String title) {
        return poemDao.getAnswerResultsByTitle(title);
    }

    // 插入 ShortqaAnswerResult
    public void insertShortqaAnswerResult(ShortqaAnswerResult result) {
        executor.execute(() -> poemDao.insertShortqaAnswerResult(result));
    }

    // 查询指定问题的 ShortqaAnswerResult
    public List<ShortqaAnswerResult> getShortqaAnswerResultsByQuestion(String questionText) {
        return poemDao.getShortqaAnswerResultsByQuestion(questionText);
    }

    // 根据标题查询 ShortqaAnswerResult 记录
    public List<ShortqaAnswerResult> getShortqaAnswerResultsByTitle(String title) {
        return poemDao.getShortqaAnswerResultsByTitle(title);
    }

    // 获取 RecitationResult 的每日错误统计
    public Map<String, Integer> getPoetryErrorCountsByDate(String title) {
        List<RecitationResult> recitationResults = poemDao.getRecitationResultsByTitle(title);
        Map<String, Integer> dateErrorMap = new HashMap<>();

        for (RecitationResult result : recitationResults) {
            String date = result.getDate();
            dateErrorMap.put(date, dateErrorMap.getOrDefault(date, 0) + 1);
        }
        return dateErrorMap;
    }

    // 获取 AnswerResult 的每日错误统计
    public Map<String, Integer> getExerciseErrorCountsByDate(String title) {
        List<AnswerResult> answerResults = poemDao.getAnswerResultsByTitle(title);
        Map<String, Integer> dateErrorMap = new HashMap<>();

        for (AnswerResult result : answerResults) {
            String date = result.getAnswerDate();
            dateErrorMap.put(date, dateErrorMap.getOrDefault(date, 0) + 1);
        }
        return dateErrorMap;
    }

    // 更新指定日期的错题计数
    public void updateDailyErrorCount() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        // 检查数据库中是否已有该日期的记录
        DailyErrorCount dailyError = poemDao.getErrorCountByDate(currentDate);

        if (dailyError == null) {
            // 如果没有记录，插入新记录并设置初始错题数为 1
            dailyError = new DailyErrorCount(currentDate, 1, 0, 0);
            poemDao.insertDailyErrorCount(dailyError);
        } else {
            // 如果有记录，错题数 +1
            poemDao.incrementErrorCount(currentDate);
        }
    }

    // 调用该方法以保存 RecitationResult 错题
    public void saveRecitationError(RecitationResult result) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            poemDao.insertRecitationResult(result);
            updateDailyErrorCount();
        });
        executor.shutdown();
    }

    // 调用该方法以保存 AnswerResult 错题
    public void saveAnswerError(AnswerResult result, Runnable onSuccess) {
        ExecutorService executor = Executors.newSingleThreadExecutor(); // 创建一个 ExecutorService
        executor.execute(() -> {
            // 获取 PoemDatabase 实例
            PoemDatabase db = PoemDatabase.getDatabase(context);

            // 使用 runInTransaction 保证操作在同一事务内执行
            db.runInTransaction(() -> {
                poemDao.insertAnswerResult(result);
                updateDailyErrorCount();
            });

            // 在事务完成后调用回调
            if (onSuccess != null) {
                // 确保回调是在主线程执行的
                new Handler(Looper.getMainLooper()).post(onSuccess);
            }

//            Log.d("PoemRepository", "Answer error saved and daily error count updated.");
        });
        executor.shutdown();  // 关闭 ExecutorService
    }

    // 调用该方法以保存 AnswerResult 错题
    public void saveQaAnswerError(ShortqaAnswerResult result, Runnable onSuccess) {
        ExecutorService executor = Executors.newSingleThreadExecutor(); // 创建一个 ExecutorService
        executor.execute(() -> {
            // 获取 PoemDatabase 实例
            PoemDatabase db = PoemDatabase.getDatabase(context);

            // 使用 runInTransaction 保证操作在同一事务内执行
            db.runInTransaction(() -> {
                poemDao.insertShortqaAnswerResult(result);
                updateDailyErrorCount();
            });

            // 在事务完成后调用回调
            if (onSuccess != null) {
                // 确保回调是在主线程执行的
                new Handler(Looper.getMainLooper()).post(onSuccess);
            }

            Log.d("PoemRepository", "Answer error saved and daily error count updated.");
        });
        executor.shutdown();  // 关闭 ExecutorService
    }


    public List<DailyErrorCount> getCurrentWeekErrorCounts() {
        String monday = DateUtils.getCurrentMonday();
        String sunday = DateUtils.getCurrentSunday();
//        Log.d(TAG, "getCurrentWeekErrorCounts: monday: " + monday);
//        Log.d(TAG, "getCurrentWeekErrorCounts: sunday: " + sunday);
        return poemDao.getWeeklyErrorCounts(monday, sunday);
    }

    // 修改增量计数方法，直接调用PoemDao的增量方法
    public void incrementCounts(String date, boolean isPoetry, boolean isExercise, boolean isError) {
        // 使用ExecutorService来异步执行数据库操作
        executor.execute(() -> {
            // 调用PoemDao的事务方法
            poemDao.incrementCounts(date, isPoetry, isExercise, isError);
        });
    }

    // 异步执行 incrementPoetryCount 逻辑
    public void incrementPoetryCount(String date, String title) {
        executor.execute(() -> {
//            Log.d(TAG, "incrementPoetryCount: " + poemDao.hasPoetryCountLogForTitle(date, title));
            if (poemDao.hasPoetryCountLogForTitle(date, title) == 0) {
//                Log.d(TAG, "Poetry count before increment: " + poemDao.getErrorCountByDate(date).getPoetryCount());
                poemDao.incrementCounts(date, true, false, false);
//                Log.d(TAG, "Poetry count after increment: " + poemDao.getErrorCountByDate(date).getErrorCount());
                poemDao.insertDailyPoetryCountLog(new DailyPoetryCountLog(date, title));
            }
//            Log.d(TAG, "incrementPoetryCount: " + poemDao.hasPoetryCountLogForTitle(date, title));
        });
    }

    public void getWeeklyPoetryCountsAsync(PoetryCountListCallback callback) {
        // 创建一个 ExecutorService 来在后台线程执行任务
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // 获取当前周的周一和周日的日期
            String monday = DateUtils.getCurrentMonday();  // 自定义方法返回本周一日期字符串
            String sunday = DateUtils.getCurrentSunday();  // 自定义方法返回本周日日期字符串

            // 调用 Dao 获取每周的诗歌数量数据
            List<DailyErrorCount> weeklyPoetryCounts = poemDao.getWeeklyPoetryCounts(monday, sunday);

//            Log.d(TAG, "Weekly poetry counts: " + weeklyPoetryCounts.get(0).getPoetryCount());
            // 在主线程中调用回调，返回结果
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.onPoetryCountListLoaded(weeklyPoetryCounts);
            });
        });
        executor.shutdown();
    }


    public List<DailyErrorCount> getWeeklyPoetryCounts() {
        // 获取当前周的周一和周日的日期
        String monday = DateUtils.getCurrentMonday();
        String sunday = DateUtils.getCurrentSunday();

        // 直接同步查询并返回结果
        return poemDao.getWeeklyPoetryCounts(monday, sunday);
    }


    // 创建回调接口以处理异步结果
    public interface PoetryCountListCallback {
        void onPoetryCountListLoaded(List<DailyErrorCount> poetryCounts);
    }


}

