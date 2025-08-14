package com.example.yypome.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yypome.R;
import com.example.yypome.data.CardData;
import com.example.yypome.data.DailyErrorCount;
import com.example.yypome.data.DateUtils;
import com.example.yypome.db.PoemRepository;
import com.example.yypome.db.StatisticsRepository;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReviewCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_CARD = 0;  // 普通卡片类型
    private static final int VIEW_TYPE_ADD_CARD = 1;  // 添加卡片类型
    private static final int VIEW_TYPE_LIST_CARD = 2;  // 复习计划卡片类型
    private static final int VIEW_TYPE_CHART_CARD = 3;  // 复习计划卡片统计表

    private Context context;
    private List<CardData> cardDataList;
    private OnItemClickListener onItemClickListener;
    private OnItemClickListener item1ClickListener;
    private OnItemClickListener item2ClickListener;
    private OnItemClickListener item3ClickListener;
    private AddCardOnItemClickListener addCardOnItemClickListener;
    private String TAG = "ReviewCardAdapter";

//    private ArrayList<Integer> poetryData = new ArrayList<>(Arrays.asList(5, 10, 8, 6, 2, 6, 3));
//    private ArrayList<Integer> exerciseData = new ArrayList<>(Arrays.asList(10, 7, 9, 8, 7, 5, 5));
//    private ArrayList<Integer> errorData = new ArrayList<>(Arrays.asList(8, 6, 6, 4, 2, 6, 3));

    public interface OnItemClickListener {
        void onItemClick(CardData data);
    }

    public interface AddCardOnItemClickListener {
        void onItemClick();
    }

    public ReviewCardAdapter(Context context, List<CardData> cardDataList, OnItemClickListener onItemClickListener, OnItemClickListener item1ClickListener, OnItemClickListener item2ClickListener, OnItemClickListener item3ClickListener, AddCardOnItemClickListener addCardOnItemClickListener) {
        this.context = context;
        this.cardDataList = cardDataList;
        this.onItemClickListener = onItemClickListener;
        this.item1ClickListener = item1ClickListener;
        this.item2ClickListener = item2ClickListener;
        this.item3ClickListener = item3ClickListener;
        this.addCardOnItemClickListener = addCardOnItemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        // 如果是最后一项，则显示“添加习题”按钮卡片
        if (position == 0) {
            return VIEW_TYPE_LIST_CARD;
        } else if (position == 1) {
            return VIEW_TYPE_CHART_CARD;
        } else if (position == cardDataList.size() + 2) {
            return VIEW_TYPE_ADD_CARD;
        } else {
            return VIEW_TYPE_CARD;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.review_item_card, parent, false);
//        return new CardViewHolder(view);
        if (viewType == VIEW_TYPE_CARD) {
            View view = LayoutInflater.from(context).inflate(R.layout.review_item_card, parent, false);
            return new CardViewHolder(view);
        } else if (viewType == VIEW_TYPE_CHART_CARD) {
            View view = LayoutInflater.from(context).inflate(R.layout.review_item_chart, parent, false);
            return new ChartCardViewHolder(view);
        } else if (viewType == VIEW_TYPE_ADD_CARD) {
            View view = LayoutInflater.from(context).inflate(R.layout.card_add_layout_review, parent, false);
            return new AddCardViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.review_item_list, parent, false);
            return new AddCardViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (getItemViewType(position) == VIEW_TYPE_CARD) {
            if (position - 2 < cardDataList.size()) {  // 检查 position 是否有效
                CardViewHolder cardHolder = (CardViewHolder) holder;
                Log.d(TAG, "onBindViewHolder: postion = " + position);
                // 获取数据
                CardData data = cardDataList.get(position - 2);

                // 数据加载
                cardHolder.title.setText(data.getTitle());
                cardHolder.completionDate.setText("背诵完成日期: " + data.getCompletionDate());
                cardHolder.recitationCount.setText("学习次数: " + data.getRecitationCount());

                // 设置卡片点击事件
                // 点击标题，弹出统计图表
                cardHolder.title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onItemClickListener.onItemClick(data);
                    }
                });

                // 点击展开，弹出对应任务的统计结果
                cardHolder.item1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        item1ClickListener.onItemClick(data);
                    }
                });

                cardHolder.item2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        item2ClickListener.onItemClick(data);
                    }
                });

                cardHolder.item3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        item3ClickListener.onItemClick(data);
                    }
                });

            }

        } else if (getItemViewType(position) == VIEW_TYPE_ADD_CARD){
            // “添加习题”按钮不需要特殊绑定数据
            AddCardViewHolder addCardHolder = (AddCardViewHolder) holder;

            // 设置卡片点击事件
            addCardHolder.addCardLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addCardOnItemClickListener.onItemClick();
                }
            });
        } else if (getItemViewType(position) == VIEW_TYPE_CHART_CARD) {
            // 设置图表的 WebView
            ChartCardViewHolder chartHolder = (ChartCardViewHolder) holder;
            setupChartWebView(chartHolder.chartWebView);
        } else {

        }

    }

    private void setupChartWebView(WebView webView) {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // 使用 ExecutorService 在后台线程中查询数据库
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Handler mainHandler = new Handler(Looper.getMainLooper());
                executor.execute(() -> {

                    // 首先获取周一到周日的日期
                    String monday = DateUtils.getCurrentMonday();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
                    Calendar calendar = Calendar.getInstance();

                    try {
                        calendar.setTime(sdf.parse(monday));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    // 初始化一周的数据为零
                    Map<String, Integer> poetryDataMap = new LinkedHashMap<>();
                    Map<String, Integer> exerciseDataMap = new LinkedHashMap<>();
                    Map<String, Integer> errorDataMap = new LinkedHashMap<>();
                    for (int i = 0; i < 7; i++) {
                        String date = sdf.format(calendar.getTime());
                        poetryDataMap.put(date, 0); // 先填充零
                        exerciseDataMap.put(date, 0);
                        errorDataMap.put(date, 0);
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                    }

                    PoemRepository poemRepository = new PoemRepository(context);
                    List<DailyErrorCount> weeklyErrorCounts = poemRepository.getCurrentWeekErrorCounts();
                    List<DailyErrorCount> weeklyPoetryCounts = poemRepository.getWeeklyPoetryCounts();

                    for (DailyErrorCount count : weeklyPoetryCounts) {
                        Log.d("PoetryCount", "Date: " + count.getDate() + ", Poetry Count: " + count.getPoetryCount());
                        poetryDataMap.put(count.getDate(), count.getPoetryCount());
                    }

                    for (DailyErrorCount count : weeklyErrorCounts) {
                        errorDataMap.put(count.getDate(), count.getErrorCount());
                        exerciseDataMap.put(count.getDate(), count.getExerciseCount());
                    }

                    ArrayList<Integer> errorData = new ArrayList<>(errorDataMap.values());
                    ArrayList<Integer> exerciseData = new ArrayList<>(exerciseDataMap.values());
                    ArrayList<Integer> poetryData = new ArrayList<>(poetryDataMap.values());

                    mainHandler.post(() -> {
                        updateChartData(webView, poetryData, exerciseData, errorData);
                    });
                });
            }
        });

        webView.loadUrl("file:///android_asset/charts/echarts.html");
    }

    // 使用 JavaScript 更新图表数据
    private void updateChartData(WebView webView, ArrayList<Integer> poetry, ArrayList<Integer> exercise, ArrayList<Integer> error) {
        // 创建 Gson 对象
        Gson gson = new Gson();

        // 将数据转换为 JSON 格式
        String poetryDataJson = gson.toJson(poetry);
        String exerciseDataJson = gson.toJson(exercise);
        String errorDataJson = gson.toJson(error);

        // 创建 JavaScript 代码
        String jsCode = "javascript:updateData(" + poetryDataJson + ", " + exerciseDataJson + ", " + errorDataJson + ")";

        // 打印生成的 JavaScript 代码，方便调试
//        Log.d("JSCode", "Generated JavaScript code: " + jsCode);

        // 调用 WebView 的 evaluateJavascript 方法
        webView.evaluateJavascript(jsCode, null);
    }


    @Override
    public int getItemCount() {
        // cardDataList 的数量加上一个“添加习题”卡片，一个计划列表卡片，一个图表统计图
        return cardDataList.size() + 3;
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView title, completionDate, recitationCount;
        TextView item1, item2, item3, item4, item5;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            completionDate = itemView.findViewById(R.id.completion_date);
            recitationCount = itemView.findViewById(R.id.recitation_count);

            item1 = itemView.findViewById(R.id.btn_item1);
            item2 = itemView.findViewById(R.id.btn_item2);
            item3 = itemView.findViewById(R.id.btn_item3);
            item4 = itemView.findViewById(R.id.btn_item4);
            item5 = itemView.findViewById(R.id.btn_item5);
        }
    }

    public static class AddCardViewHolder extends RecyclerView.ViewHolder {

        View addCardLayout;

        public AddCardViewHolder(@NonNull View itemView) {
            super(itemView);
            // 设置“添加习题”按钮的点击事件等

            addCardLayout = itemView;
        }
    }

    public static class ChartCardViewHolder extends RecyclerView.ViewHolder {
        WebView chartWebView;

        public ChartCardViewHolder(@NonNull View itemView) {
            super(itemView);
            chartWebView = itemView.findViewById(R.id.chartWebView);
        }
    }


    // 动态添加卡片
    public void addCard(CardData data) {
        if (containsTitle(cardDataList, data.getTitle()) != null) {
            StatisticsRepository statisticsRepository = new StatisticsRepository(context);
            statisticsRepository.updateReviewCardFlag(data.getTitle());
            cardDataList.add(data);
            notifyDataSetChanged();
        }
    }

    public CardData containsTitle(List<CardData> visibleCardList, String targetTitle) {
        for (CardData card : visibleCardList) {
            if (card.getTitle().equals(targetTitle)) {
                return card; // 找到匹配的 title
            }
        }
        return null; // 没有找到匹配的 title
    }

    // 删除卡片数据
    public void removeItem(int position) {
        if (position > 1 && position < cardDataList.size() + 2) {  // 确保 position 有效
            StatisticsRepository statisticsRepository = new StatisticsRepository(context);
            statisticsRepository.clearReviewCardFlag(cardDataList.get(position - 2).getTitle());  // 更新数据库
            cardDataList.remove(position - 2);
            notifyDataSetChanged();  // 刷新UI
        }
    }

}
