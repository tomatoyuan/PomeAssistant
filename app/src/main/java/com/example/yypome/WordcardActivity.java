package com.example.yypome;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yypome.adapter.WordcardLeftListAdapter;
import com.example.yypome.data.CardData;
import com.example.yypome.data.JSONReader;
import com.example.yypome.data.niceSentencesData;
import com.example.yypome.fragment.CardFragment1;
import com.example.yypome.fragment.CardFragment2;
import com.example.yypome.fragment.CardFragment3;
import com.example.yypome.fragment.CardFragment4;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WordcardActivity extends AppCompatActivity {

    TextView titleTextView, authorTextView;
    ImageView imageView;
    private WordcardLeftListAdapter leftListAdapter;

    private RecyclerView leftRecyclerView;

    private List<String> leftDataList = new ArrayList<>();

    private CardFragment1 cardFragment1;
    private CardFragment2 cardFragment2;
    private CardFragment3 cardFragment3;
    private CardFragment4 cardFragment4;

    private List<CardData> cardDataList;
    List<niceSentencesData> niceSentencesList = new ArrayList<>();
    private final String TAG = "WordcardActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wordcard);

        // 初始化控件
        leftRecyclerView = findViewById(R.id.WordcardLeftRecyclerView);

        // 初始化左侧数据
        leftDataList.add("原文");
        leftDataList.add("提问");
        leftDataList.add("名句");
        leftDataList.add("方法背诵");

        Log.d(TAG, "onCreate: leftDataList Successful!");
        // 设置左侧RecyclerView的适配器
        leftListAdapter = new WordcardLeftListAdapter(leftDataList);
        leftRecyclerView.setAdapter(leftListAdapter);

        // 初始化视图
        titleTextView = findViewById(R.id.detailTitleTextView);

        // 获取传递的数据
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");

        // 设置数据到视图
        titleTextView.setText(title);

        // 调用方法读取 JSON 数据
        cardDataList = loadCardDataFromJson(title);

        Log.d(TAG, "onCreate: loadCardDataFromJson Successful!");
        // recyclerView点击事件
        leftListAdapter.setLeftListOnClickItemListener(new WordcardLeftListAdapter.LeftListOnClickItemListener() {
            @Override
            public void onItemClick(int position) {
                leftListAdapter.setCurrentIndex(position);

                // 点击左侧分类，右侧显示对应界面
                selectedFragment(position, cardDataList);
            }
        });

        Log.d(TAG, "onCreate: xxx Successful!");
        // 默认首页选中
        selectedFragment(0, cardDataList);

        Log.d(TAG, "onCreate: xxxxx Successful!");
        // 返回
        findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 销毁注册页面返回
                finish();
            }
        });

        // 实例化 JSONReader 并加载数据
        JSONReader jsonReader = new JSONReader();
        niceSentencesList = jsonReader.loadPoemData(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    private void selectedFragment(int position, List<CardData> cardDataList) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        hideFragment(fragmentTransaction);

        // 获取原文内容
        CardData data = cardDataList.get(0);
        String title = data.getTitle();
        String author = data.getAuthor();
//        String image = data.getImage();
        String image = "";
        String completion_data = data.getCompletionDate();
        int recitation_count = data.getRecitationCount();
        String original_text = data.getOriginal_text();
        String translation_text = data.getTranslation_text();
        String comment_text = data.getComment_text();
        String author_text = data.getAuthor_text();
        String work_introduction = data.getWork_introduction();
        String createtive_bg = data.getCreative_background();
        String work_appreciation = data.getAppreciation();
        List<String> niceSentence = data.getNice_sentence();
        String approach = data.getApproach();
        String style = data.getStyle();
        if (position == 0) {
            if (cardFragment1 == null) {
                cardFragment1 = CardFragment1.newInstance(original_text, translation_text, comment_text, author_text, image, work_introduction, createtive_bg, work_appreciation, approach, style);

                fragmentTransaction.add(R.id.fragment_container, cardFragment1);
            } else {
                fragmentTransaction.show(cardFragment1);
            }
        } else if (position == 1) {
            if (cardFragment2 == null) {
                cardFragment2 = new CardFragment2();
                fragmentTransaction.add(R.id.fragment_container, cardFragment2);
            } else {
                fragmentTransaction.show(cardFragment2);
            }
        } else if(position == 2) {
            if (cardFragment3 == null) {
                cardFragment3 = CardFragment3.newInstance(title, niceSentence, original_text, niceSentencesList);

                fragmentTransaction.add(R.id.fragment_container, cardFragment3);
            } else {
                fragmentTransaction.show(cardFragment3);
            }
        } else {
            if (cardFragment4 == null) {
                cardFragment4 = CardFragment4.newInstance(title, original_text);

                fragmentTransaction.add(R.id.fragment_container, cardFragment4);
            } else {
                fragmentTransaction.show(cardFragment4);
            }
        }
        // 一定要提交
        fragmentTransaction.commit();

    }

    private void hideFragment(FragmentTransaction fragmentTransaction) {
        if (cardFragment1 != null) {
            fragmentTransaction.hide(cardFragment1);
        }

        if (cardFragment2 != null) {
            fragmentTransaction.hide(cardFragment2);
        }

        if (cardFragment3 != null) {
            fragmentTransaction.hide(cardFragment3);
        }

        if (cardFragment4 != null) {
            fragmentTransaction.hide(cardFragment4);
        }
    }

    private List<CardData> loadCardDataFromJson(String title) {
        List<CardData> dataList = new ArrayList<>();
        try {
            InputStream is = getAssets().open("data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                // 获取本文内容，后续可以利用数据库查询优化
                if (!obj.getString("title").trim().equalsIgnoreCase(title.trim())) {
                    continue;
                }

                CardData data = new CardData();
                data.setTitle(obj.getString("title"));
                data.setAuthor(obj.getString("author"));
                data.setImage(obj.getString("image"));
                data.setCompletionDate(obj.getString("completion_date"));
                data.setRecitationCount(obj.getInt("recitation_count"));
                data.setOriginal_text(obj.getString("original_text"));
                data.setTranslation_text(obj.getString("translation_text"));
                data.setComment_text(obj.getString("comment_text"));
                data.setWork_introduction(obj.getString("work_introduction"));
                data.setCreative_background(obj.getString("creative_background"));
                data.setAppreciation(obj.getString("appreciation"));
                // 获取 "nice_sentences" 数组
                List<String> poemSentences = new ArrayList<>();
                if (obj.has("nice_sentences")) {
                    JSONArray niceSentencesArray = obj.getJSONArray("nice_sentences");
                    for (int idx = 0; idx < niceSentencesArray.length(); idx++) {
                        String sentence = niceSentencesArray.getString(idx);
                        poemSentences.add(sentence);
                    }
                } else {
                    Log.w(TAG, "\"nice_sentences\" not found in JSON.");
                }
                data.setNice_sentence(poemSentences);
                data.setAuthor_text(obj.getString("author_text"));
//                data.setGrade(obj.getString("grade"));
                if (obj.has("grade")) {
                    data.setGrade(obj.getString("grade"));
                } else {
                    data.setGrade(""); // 或者设置默认值
                }
                data.setApproach(obj.getString("approach"));
                data.setStyle(obj.getString("style"));


                dataList.add(data);

                // 得到本文数据后退出
                break;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return dataList;
    }
}