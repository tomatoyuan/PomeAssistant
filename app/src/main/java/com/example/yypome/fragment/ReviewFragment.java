package com.example.yypome.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yypome.R;
import com.example.yypome.adapter.ReviewCardAdapter;
import com.example.yypome.data.AnswerResult;
import com.example.yypome.data.CardData;
import com.example.yypome.data.RecitationResult;
import com.example.yypome.data.ShortqaAnswerResult;
import com.example.yypome.db.LiveDataUtils;
import com.example.yypome.db.PoemRepository;
import com.example.yypome.db.StatisticsRepository;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReviewFragment extends Fragment {

    private static final String TAG = "ReviewFragment";
    private View rootView;
    private RecyclerView recyclerView;
    private ReviewCardAdapter cardAdapter;
    private List<CardData> visibleCardList;  // 仅展示的题目数据（前5条）

    public ReviewFragment() {
        // Required empty public constructor
    }

    public static ReviewFragment newInstance(String param1, String param2) {
        ReviewFragment fragment = new ReviewFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_review, container, false);

        // 初始化卡片数据列表
        visibleCardList = new ArrayList<>();

        // 加载已添加的复习卡片
        loadVisibleCardList();

        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        cardAdapter = new ReviewCardAdapter(getActivity(), visibleCardList, new ReviewCardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CardData data) {
                showChartDialog(data);
            }
        }, new ReviewCardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CardData data) {
                showItem1Dialog(data);
            }
        }, new ReviewCardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CardData data) {
                showItem2Dialog(data);
            }
        }, new ReviewCardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CardData data) {
                showItem3Dialog(data);
            }
        }, new ReviewCardAdapter.AddCardOnItemClickListener() {
            @Override
            public void onItemClick() {
                showAddCardDialog();
            }
        });
        recyclerView.setAdapter(cardAdapter);

        // 添加滑动删除功能
        enableSwipeToDelete();

        return rootView;
    }

    private void loadVisibleCardList() {
        StatisticsRepository statisticsRepository = new StatisticsRepository(getContext());
        PoemRepository repository = new PoemRepository(getContext());

        // 获取 reviewCardFlag 为 1 的卡片标题
        statisticsRepository.getTitlesWithReviewFlag().observe(getViewLifecycleOwner(), reviewTitles -> {
            visibleCardList.clear();  // 确保每次加载前清空列表
            if (reviewTitles != null && !reviewTitles.isEmpty()) {
                for (String title : reviewTitles) {
//                    Log.d(TAG, "loadVisibleCardList: title " + title);

                    // 使用 SingleEventObserver 避免重复观察 LiveData
                    LiveDataUtils.observeOnce(repository.getCardDataLiveByTitle(title), getViewLifecycleOwner(), cardData -> {
                        if (cardData != null && containsTitle(visibleCardList, cardData.getTitle()) == null) {
                            visibleCardList.add(cardData);  // 添加符合条件的卡片
                            cardAdapter.notifyDataSetChanged();  // 刷新UI
                        }
                    });
                }
            } else {
                visibleCardList.clear();
                cardAdapter.notifyDataSetChanged();  // 刷新UI
            }
        });
    }


    private void showChartDialog(CardData data) {
        // 创建一个 Dialog
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_chart);

        // 设置 Dialog 背景透明
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView title = dialog.findViewById(R.id.poem_title);
        title.setText(data.getTitle());


        // 设置图表的 WebView
        WebView chartWebView = dialog.findViewById(R.id.chartWebView);
        chartWebView.setBackgroundColor(0); // 设置背景色透明
        chartWebView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null); // 启用透明模式（重要）
//        setupChartWebView(chartWebView, data);

        // 使用 ExecutorService 在后台线程中执行数据库查询
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());  // 获取主线程的 Handler

        executor.execute(() -> {
            PoemRepository poemRepository = new PoemRepository(getContext());
            Map<String, Integer> poetryErrorMap = poemRepository.getPoetryErrorCountsByDate(data.getTitle());
            Map<String, Integer> exerciseErrorMap = poemRepository.getExerciseErrorCountsByDate(data.getTitle());

            // 获取两个Map中的所有日期，使用Set自动去重
            Set<String> dateSet = new HashSet<>();
            dateSet.addAll(poetryErrorMap.keySet());
            dateSet.addAll(exerciseErrorMap.keySet());

            // 准备图表数据
            List<String> dates = new ArrayList<>(dateSet);
            Collections.sort(dates);  // 确保日期顺序

            ArrayList<Integer> poetryData = new ArrayList<>();
            ArrayList<Integer> exerciseData = new ArrayList<>();
            List<String> xLabels = new ArrayList<>();


            // 构建x轴标签和数据
            for (int i = 0; i < 4; i++) {
                String label;
                if (i < dates.size()) {
                    label = "第" + (i + 1) + "次复习\n" + dates.get(i);
                    poetryData.add(poetryErrorMap.getOrDefault(dates.get(i), 0));
                    exerciseData.add(exerciseErrorMap.getOrDefault(dates.get(i), 0));
                } else {
                    label = "第" + (i + 1) + "次复习";
                    poetryData.add(0);
                    exerciseData.add(0);
                }
                xLabels.add(label);
            }

            mainHandler.post(() -> {
                setupChartWebView(chartWebView, poetryData, exerciseData, xLabels);
            });
        });

        // 显示 Dialog
        dialog.show();
    }

    // 配置并更新图表 WebView
    private void setupChartWebView(WebView webView, ArrayList<Integer> poetryData, ArrayList<Integer> exerciseData, List<String> dates) {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/charts/cardcharts.html");

//        Log.d(TAG, "setupChartWebView: poetryData" + poetryData);
//        Log.d(TAG, "setupChartWebView: exerciseData" + exerciseData);
//        Log.d(TAG, "setupChartWebView: dates" + dates);

        // 将数据传递给图表
        webView.postDelayed(() -> {
            updateChartData(webView, poetryData, exerciseData, dates);
        }, 500);
    }

    // 使用 JavaScript 更新图表数据，将日期数据也传递给图表
    private void updateChartData(WebView webView, ArrayList<Integer> poetryData, ArrayList<Integer> exerciseData, List<String> dates) {
        Gson gson = new Gson();
        String poetryDataJson = gson.toJson(poetryData);
        String exerciseDataJson = gson.toJson(exerciseData);
        String datesJson = gson.toJson(dates);

        String jsCode = "updateData(" + poetryDataJson + ", " + exerciseDataJson + ", " + datesJson + ")";
        webView.evaluateJavascript(jsCode, null);
    }


    private void showItem1Dialog(CardData data) {
        // 创建一个 Dialog
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.review_dialog_item1);

        // 设置 Dialog 背景透明
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView title = dialog.findViewById(R.id.poem_title);
        title.setText(data.getTitle());

        TextView historyText = dialog.findViewById(R.id.ansShow);

        // 使用 Executor 在后台线程中进行查询
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());  // 获取主线程的 Handler

        executor.execute(() -> {
            PoemRepository poemRepository = new PoemRepository(dialog.getContext());
            List<RecitationResult> results = poemRepository.getRecitationResultsByTitle(data.getTitle());

            // 使用 Handler 在主线程上更新 UI
            mainHandler.post(() -> {
                Log.d(TAG, "showItem1Dialog: results" + results);
                for (RecitationResult result : results) {
                    SpannableString restoredText = htmlToSpannable(result.getResultText());
                    String answerDate = result.getDate();

                    // 设置日期和文本的显示格式
                    SpannableString dateText = new SpannableString("背诵时间: " + answerDate + "\n");
                    dateText.setSpan(new StyleSpan(Typeface.BOLD), 0, dateText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // 显示答题时间和背诵结果
                    historyText.append(dateText);
                    historyText.append(restoredText);
                    historyText.append("\n");  // 用于分隔多个结果
                }
            });
        });

        // 显示 Dialog
        dialog.show();
    }

    private void showItem2Dialog(CardData data) {
        // 创建一个 Dialog
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.review_dialog_item1);

        // 设置 Dialog 背景透明
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView title = dialog.findViewById(R.id.poem_title);
        title.setText(data.getTitle());

        TextView historyText = dialog.findViewById(R.id.ansShow);

        // 使用 Executor 在后台线程中进行查询
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());  // 获取主线程的 Handler

        executor.execute(() -> {
            PoemRepository poemRepository = new PoemRepository(dialog.getContext());
            List<AnswerResult> answerResults = poemRepository.getAnswerResultsByTitle(data.getTitle());

            // 使用 Handler 在主线程上处理查询结果并更新 UI
            mainHandler.post(() -> {
                for (AnswerResult result : answerResults) {
                    // 从数据库获取的结果数据
                    String question = result.getQuestionText();
                    String userAnswer = result.getUserAnswer();
                    String correctAnswer = result.getCorrectAnswer();
                    String answerDate = result.getAnswerDate();

                    // 使用 SpannableString 来标记不同的文本颜色
                    SpannableString questionTtile = new SpannableString("问题: \n");
                    SpannableString questionText = new SpannableString(question + "\n");
                    SpannableString userAnswerTtile = new SpannableString("你的答案: \n");
                    SpannableString userAnswerText = new SpannableString(userAnswer + "\n");
                    SpannableString correctAnswerTtile = new SpannableString("正确答案: \n");
                    SpannableString correctAnswerText = new SpannableString(correctAnswer + "\n\n");

                    // 添加日期信息
                    SpannableString answerDateText = new SpannableString("回答时间: " + answerDate + "\n");
                    answerDateText.setSpan(new ForegroundColorSpan(Color.BLACK), 0, answerDateText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // 小标题设置为黑色
                    questionTtile.setSpan(new ForegroundColorSpan(Color.BLACK), 0, questionTtile.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    userAnswerTtile.setSpan(new ForegroundColorSpan(Color.BLACK), 0, userAnswerTtile.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    correctAnswerTtile.setSpan(new ForegroundColorSpan(Color.BLACK), 0, correctAnswerTtile.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // 小标题设置加粗
                    answerDateText.setSpan(new StyleSpan(Typeface.BOLD), 0, answerDateText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    questionTtile.setSpan(new StyleSpan(Typeface.BOLD), 0, questionTtile.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    userAnswerTtile.setSpan(new StyleSpan(Typeface.BOLD), 0, userAnswerTtile.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    correctAnswerTtile.setSpan(new StyleSpan(Typeface.BOLD), 0, correctAnswerTtile.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // 问题也设置为黑色
                    questionText.setSpan(new ForegroundColorSpan(Color.BLACK), 0, questionText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // 问题设置为宋体
                    questionText.setSpan(new TypefaceSpan("serif"), 0, questionText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // 设置颜色：错误答案标红，正确答案标绿
                    if (userAnswer.equals(correctAnswer)) {
                        userAnswerText.setSpan(new ForegroundColorSpan(Color.GREEN), 0, userAnswerText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        userAnswerText.setSpan(new ForegroundColorSpan(Color.RED), 0, userAnswerText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    correctAnswerText.setSpan(new ForegroundColorSpan(Color.GREEN), 0, correctAnswerText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // 将标记好的文本追加到 TextView 中
                    historyText.append(answerDateText);
                    historyText.append(questionTtile);
                    historyText.append(questionText);
                    historyText.append(userAnswerTtile);
                    historyText.append(userAnswerText);
                    historyText.append(correctAnswerTtile);
                    historyText.append(correctAnswerText);

//                    Log.d("AnswerResult", "Question: " + question);
//                    Log.d("AnswerResult", "User Answer: " + userAnswer);
//                    Log.d("AnswerResult", "Correct Answer: " + correctAnswer);
//                    Log.d("AnswerResult", "Answer Date: " + answerDate);
                }
            });
        });

        // 显示 Dialog
        dialog.show();
    }

    private void showItem3Dialog(CardData data) {
        // 创建一个 Dialog
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.review_dialog_item3);

        // 设置 Dialog 背景透明
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView title = dialog.findViewById(R.id.poem_title);
        title.setText(data.getTitle());

        TextView historyText = dialog.findViewById(R.id.ansShow);

        // 使用 Executor 在后台线程中进行查询
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());  // 获取主线程的 Handler

        executor.execute(() -> {
            PoemRepository poemRepository = new PoemRepository(dialog.getContext());
            List<ShortqaAnswerResult> answerResults = poemRepository.getShortqaAnswerResultsByTitle(data.getTitle());

            for (ShortqaAnswerResult answer: answerResults) {
                Log.d(TAG, "showItem3Dialog: astAnswer:" + answer.getAstAnswer());
            }
            // 使用 Handler 在主线程上处理查询结果并更新 UI
            mainHandler.post(() -> {
                for (ShortqaAnswerResult result : answerResults) {
                    // 从数据库获取的结果数据
                    String question = result.getQuestionText();
                    String userAnswer = result.getUserAnswer();
                    String correctAnswer = result.getCorrectAnswer();
                    String astAnswer = result.getAstAnswer();
                    String answerDate = result.getAnswerDate();

                    // 使用 SpannableString 来标记不同的文本颜色
                    SpannableString questionTtile = new SpannableString("问题: \n");
                    SpannableString questionText = new SpannableString(question + "\n");
                    SpannableString userAnswerTtile = new SpannableString("你的答案: \n");
                    SpannableString userAnswerText = new SpannableString(userAnswer + "\n");
                    SpannableString correctAnswerTtile = new SpannableString("正确答案: \n");
                    SpannableString correctAnswerText = new SpannableString(correctAnswer + "\n");
                    SpannableString astAnswerTtile = new SpannableString("批改建议: \n");
                    SpannableString astAnswerText = new SpannableString(astAnswer + "\n\n");

                    // 添加日期信息
                    SpannableString answerDateText = new SpannableString("回答时间: " + answerDate + "\n");
                    answerDateText.setSpan(new ForegroundColorSpan(Color.BLACK), 0, answerDateText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // 小标题设置为黑色
                    questionTtile.setSpan(new ForegroundColorSpan(Color.BLACK), 0, questionTtile.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    userAnswerTtile.setSpan(new ForegroundColorSpan(Color.BLACK), 0, userAnswerTtile.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    correctAnswerTtile.setSpan(new ForegroundColorSpan(Color.BLACK), 0, correctAnswerTtile.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    astAnswerTtile.setSpan(new ForegroundColorSpan(Color.BLACK), 0, astAnswerTtile.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // 小标题设置加粗
                    answerDateText.setSpan(new StyleSpan(Typeface.BOLD), 0, answerDateText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    questionTtile.setSpan(new StyleSpan(Typeface.BOLD), 0, questionTtile.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    userAnswerTtile.setSpan(new StyleSpan(Typeface.BOLD), 0, userAnswerTtile.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    correctAnswerTtile.setSpan(new StyleSpan(Typeface.BOLD), 0, correctAnswerTtile.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    astAnswerTtile.setSpan(new StyleSpan(Typeface.BOLD), 0, astAnswerTtile.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // 问题也设置为黑色
                    questionText.setSpan(new ForegroundColorSpan(Color.BLACK), 0, questionText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // 问题设置为宋体
                    questionText.setSpan(new TypefaceSpan("serif"), 0, questionText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // 设置颜色：错误答案标红，正确答案标绿
                    if (userAnswer.equals(correctAnswer)) {
                        userAnswerText.setSpan(new ForegroundColorSpan(Color.GREEN), 0, userAnswerText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        userAnswerText.setSpan(new ForegroundColorSpan(Color.RED), 0, userAnswerText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    correctAnswerText.setSpan(new ForegroundColorSpan(Color.GREEN), 0, correctAnswerText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    astAnswerText.setSpan(new ForegroundColorSpan(Color.BLACK), 0, astAnswerText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // 将标记好的文本追加到 TextView 中
                    historyText.append(answerDateText);
                    historyText.append(questionTtile);
                    historyText.append(questionText);
                    historyText.append(userAnswerTtile);
                    historyText.append(userAnswerText);
                    historyText.append(correctAnswerTtile);
                    historyText.append(correctAnswerText);
                    historyText.append(astAnswerTtile);
                    historyText.append(astAnswerText);

//                    Log.d("AnswerResult", "Question: " + question);
//                    Log.d("AnswerResult", "User Answer: " + userAnswer);
//                    Log.d("AnswerResult", "Correct Answer: " + correctAnswer);
//                    Log.d("AnswerResult", "Answer Date: " + answerDate);
                }
            });
        });

        // 显示 Dialog
        dialog.show();
    }


    // HTML 转 SpannableString
    public SpannableString htmlToSpannable(String htmlString) {
        Spanned spanned = Html.fromHtml(htmlString, Html.FROM_HTML_MODE_COMPACT);
        return new SpannableString(spanned);
    }

    private void showAddCardDialog() {
        // 创建一个 Dialog
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_card_add_review);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText editTextTitle = dialog.findViewById(R.id.editTextCardTitle);
        MaterialButton addButton = dialog.findViewById(R.id.btn_add_card);

        addButton.setOnClickListener(v -> {
            String newCardTitle = editTextTitle.getText().toString().trim();
            if (!newCardTitle.isEmpty()) {
                PoemRepository poemRepository = new PoemRepository(dialog.getContext());
                StatisticsRepository statisticsRepository = new StatisticsRepository(dialog.getContext());

                if (containsTitle(visibleCardList, newCardTitle) != null) {
                    MyToastShow(R.drawable.toast_pic3, "重复添加卡片！");
                } else {
//                    poemRepository.getCardDataLiveByTitle(newCardTitle).observeOnce((LifecycleOwner) getContext(), cardData -> {
                    LiveDataUtils.observeOnce(poemRepository.getCardDataLiveByTitle(newCardTitle), getViewLifecycleOwner(), cardData -> {
                        if (cardData != null) {
                            cardAdapter.addCard(cardData);  // 添加卡片
                            MyToastShow(R.drawable.toast_pic3, "新卡片添加成功！");
                            statisticsRepository.updateReviewCardFlag(newCardTitle);  // 更新 reviewCardFlag
                            dialog.dismiss();
                        } else {
                            MyToastShow(R.drawable.toast_pic3, "题目未找到！");
                        }
                    });
                }
            } else {
                MyToastShow(R.drawable.toast_pic3, "请输入新卡片的题目");
            }
        });

        ImageView closeButton = dialog.findViewById(R.id.dialog_close_button_card_add);
        closeButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }


    public CardData containsTitle(List<CardData> visibleCardList, String targetTitle) {
        for (CardData card : visibleCardList) {
            if (card.getTitle().equals(targetTitle)) {
                return card; // 找到匹配的 title
            }
        }
        return null; // 没有找到匹配的 title
    }

    // 启用滑动删除
    private void enableSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                cardAdapter.removeItem(position); // 删除卡片
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void MyToastShow(int iconResId, String message) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View layout = inflater.inflate(R.layout.custom_toast, null);

        ImageView toastIcon = layout.findViewById(R.id.toast_icon);
        toastIcon.setImageResource(iconResId);

        TextView toastText = layout.findViewById(R.id.toast_message);
        toastText.setText(message);

        Toast toast = new Toast(getContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

}