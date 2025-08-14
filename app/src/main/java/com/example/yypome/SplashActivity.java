package com.example.yypome;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.yypome.data.CardData;
import com.example.yypome.data.Question;
import com.example.yypome.db.FileRepository;
import com.example.yypome.db.PoemRepository;
import com.example.yypome.db.StatisticsRepository;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        PoemRepository poemRepository = new PoemRepository(this);
        StatisticsRepository statisticsRepository = new StatisticsRepository(this);
        FileRepository fileRepository = new FileRepository(this);

        Button skipButton = findViewById(R.id.btn_skip);
        skipButton.setVisibility(View.GONE);
        skipButton.setOnClickListener(view -> startMainActivity());

        poemRepository.getAllCardData().observe(this, dataList -> {
            if (dataList == null || dataList.isEmpty()) {
                Log.d(TAG, "onCreate: 数据库为空，准备从 JSON 导入数据");
                initializeDatabases(poemRepository, statisticsRepository, fileRepository);
            } else {
                Log.d(TAG, "onCreate: 数据库已创建，包含 " + dataList.size() + " 条数据");
                skipButton.setVisibility(View.VISIBLE);
            }
        });

        VideoView videoView = findViewById(R.id.splash_video);
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash_video);
        videoView.setVideoURI(videoUri);
        videoView.setOnCompletionListener(mp -> startMainActivity());
        videoView.start();
    }

    // 初始化数据库方法，确保两个数据库同时初始化
    private void initializeDatabases(PoemRepository poemRepository, StatisticsRepository statisticsRepository, FileRepository fileRepository) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String jsonData = loadJSONFromAsset(this, "data.json");
            if (jsonData != null) {
                List<CardData> cardDataList = parseJsonData(jsonData);
                for (CardData cardData : cardDataList) {
                    poemRepository.saveCardData(cardData);
                    statisticsRepository.insertInitialData(cardData.getTitle(), 0);
                }

                // 初始化部分习题卡片和检查卡片
                initializeCheckAndReview(statisticsRepository);

                Log.d(TAG, "PoemDatabase和StatisticsDatabase初始化完成");
//                runOnUiThread(() -> Toast.makeText(this, "数据初始化完成", Toast.LENGTH_SHORT).show());
            }

            // 加载 hash2url.json 文件并存储到数据库
            fileRepository.loadJsonToDatabase(this);
            Log.d(TAG, "FileRepository的Hash2Url数据初始化完成");

            // 在主线程显示初始化完成提示
            runOnUiThread(() -> Toast.makeText(this, "数据初始化完成", Toast.LENGTH_SHORT).show());
        });
    }

    private void initializeCheckAndReview(StatisticsRepository statisticsRepository) {
        // 检查卡片
        statisticsRepository.updateCheckCardFlag("静夜思");
        statisticsRepository.updateCheckCardFlag("两小儿辩日");
        statisticsRepository.updateCheckCardFlag("将进酒");
        statisticsRepository.updateCheckCardFlag("桃花源记");
        statisticsRepository.updateCheckCardFlag("池上");

        // 复习卡片
        statisticsRepository.updateReviewCardFlag("静夜思");
        statisticsRepository.updateReviewCardFlag("两小儿辩日");
        statisticsRepository.updateReviewCardFlag("将进酒");
        statisticsRepository.updateReviewCardFlag("桃花源记");
        statisticsRepository.updateReviewCardFlag("池上");
    }

    private List<CardData> parseJsonData(String jsonData) {
        List<CardData> cardDataList = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonArray jsonArray = parser.parse(jsonData).getAsJsonArray();
        Gson gson = new Gson();
        for (JsonElement element : jsonArray) {
            JsonObject obj = element.getAsJsonObject();
            CardData cardData = gson.fromJson(obj, CardData.class);

            if (obj.has("exercise")) {
                Type questionListType = new TypeToken<List<Question>>() {}.getType();
                List<Question> questions = gson.fromJson(obj.get("exercise"), questionListType);
                cardData.setQuestions(questions);
            } else {
                cardData.setQuestions(new ArrayList<>());
            }

            if (obj.has("short_answer_question")) {
                Type questionListType = new TypeToken<List<Question>>() {}.getType();
                List<Question> qaQuestions = gson.fromJson(obj.get("short_answer_question"), questionListType);
                cardData.setShort_answer_question(qaQuestions);
            } else {
                cardData.setShort_answer_question(new ArrayList<>());
            }

            cardData.setCompletionDate(obj.has("completion_date") ? obj.get("completion_date").getAsString() : "暂未学习");
            cardData.setNice_sentence(obj.has("nice_sentences") ? gson.fromJson(obj.get("nice_sentences").getAsJsonArray(), new TypeToken<List<String>>() {}.getType()) : new ArrayList<>());
            cardData.setApproach(obj.has("approach") ? obj.get("approach").getAsString() : "其他");
            cardData.setStyle(obj.has("style") ? obj.get("style").getAsString() : "其他");
            cardData.setRecitationCount(0);
            cardData.setSuggestMethod(obj.has("method") ? obj.get("method").getAsString() : "");

            cardDataList.add(cardData);
        }
        return cardDataList;
    }

    private String loadJSONFromAsset(Context context, String fileName) {
        try (InputStream is = context.getAssets().open(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            Log.e(TAG, "Error reading JSON file", e);
            return null;
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
