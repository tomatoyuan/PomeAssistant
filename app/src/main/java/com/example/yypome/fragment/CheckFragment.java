package com.example.yypome.fragment;


import static com.example.yypome.myapi.MyApi.BEARER_TOKEN;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yypome.ImageIdentifyActivity;
import android.Manifest;
import com.example.yypome.R;
import com.example.yypome.Speech.JsonParser;
import com.example.yypome.adapter.CardAdapter;
import com.example.yypome.data.AnswerResult;
import com.example.yypome.data.CardData;
import com.example.yypome.data.Question;
import com.example.yypome.data.RecitationResult;
import com.example.yypome.data.ShortqaAnswerResult;
import com.example.yypome.db.LiveDataUtils;
import com.example.yypome.db.PoemRepository;
import com.example.yypome.db.StatisticsRepository;
import com.example.yypome.myapi.MyApi;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.noties.markwon.Markwon;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;

public class CheckFragment extends Fragment {

    private View rootView;
//    private List<CardData> cardDataList;  // 全部题目数据
    private List<CardData> visibleCardList;  // 仅展示的题目数据（前5条）
    private CardAdapter cardAdapter;
    private RecyclerView recyclerView;
    TextView testTextView;

    private static final int REQUEST_MIC_PERMISSION = 100;
    // 语音听写对象
    private SpeechRecognizer mIat;
    // 语音听写UI
    private RecognizerDialog mIatDialog;
    private EditText mResultText;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<>();

    // 记录历史读入
    private String historyText = "";

    private ListView searchResultsList;
    private ArrayAdapter<String> searchAdapter;
    private List<String> searchResults = new ArrayList<>();
    private String astStrAns;

    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;

    // 声明一个 ActivityResultLauncher 用于接收返回的数据
    private ActivityResultLauncher<Intent> activityResultLauncher;

    private String TAG = "CheckFragment";

    public CheckFragment() {
        // Required empty public constructor
    }

    public static CheckFragment newInstance(String param1) {
        CheckFragment fragment = new CheckFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_check, container, false);

        // 初始化讯飞语音转文字模块
        SpeechUtility.createUtility(getContext(), SpeechConstant.APPID + MyApi.appId);

        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(getContext(), mInitListener);
        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(requireContext(), mInitListener);

        // 初始化卡片数据列表
        visibleCardList = new ArrayList<>();

        // 设置 RecyclerView
        recyclerView = rootView.findViewById(R.id.card_container);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        cardAdapter = new CardAdapter(visibleCardList, getContext(), new CardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CardData data) {
//                Log.d(TAG, "onClick: SUCESSFUL");
                showImageDialog(data);
            }
        }, new CardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CardData data) {
                showSpeechDialog(data);
            }
        }, new CardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CardData data) {
                // 设置按钮点击事件，启动Activity并传递数据

                Intent intent = new Intent(getActivity(), ImageIdentifyActivity.class);
                intent.putExtra("poem_text",  data.getOriginal_text());
                activityResultLauncher.launch(intent);  // 启动Activity并等待返回结果

            }
        }, new CardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CardData data) {
                showQaDialog(data);
            }
        }, new CardAdapter.AddCardOnItemClickListener() {
            @Override
            public void onItemClick() {
                showAddCardDialog();
            }
        });

        recyclerView.setAdapter(cardAdapter);

        // 添加滑动删除功能
        enableSwipeToDelete();

        // 加载已添加的检查卡片
        loadVisibleCardList();

        // 设置搜索标题，跳转对应卡片功能
        EditText searchEditText = rootView.findViewById(R.id.search_edit_text);
        searchResultsList = rootView.findViewById(R.id.search_results_list);
        ImageView imgSearch = rootView.findViewById(R.id.img_search);

        searchAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, searchResults);
        searchResultsList.setAdapter(searchAdapter);

        // 监听搜索框变化
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String query = charSequence.toString().toLowerCase();
                searchResults.clear();

                findCardsEdit(query);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // 点击搜索
        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = searchEditText.getText().toString().toLowerCase();
                searchResults.clear();

                findCards(query);
            }
        });

        searchResultsList.setOnItemClickListener((parent, view, position, id) -> {
            String selectedTitle = searchResults.get(position);
            if (!selectedTitle.equals("没有找到😣")) {
                filterCards(selectedTitle);
            }

            searchResultsList.setVisibility(View.GONE); // 点击后隐藏结果
        });

        // 跳转手写文字识别接口
        // 初始化 ActivityResultLauncher
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // 处理从 Activity 返回的结果
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
//                        String returnData = result.getData().getStringExtra("result_key");
//                        resultTextView.setText(returnData);
                    }
                }
        );

        return rootView;
    }

    private void loadVisibleCardList() {
        StatisticsRepository statisticsRepository = new StatisticsRepository(getContext());
        PoemRepository repository = new PoemRepository(getContext());

        // 获取 checkCardFlag 为 1 的卡片标题
        statisticsRepository.getTitlesWithCheckFlag().observe(getViewLifecycleOwner(), checkTitles -> {
            visibleCardList.clear();  // 确保每次加载前清空列表
            if (checkTitles != null && !checkTitles.isEmpty()) {
                for (String title : checkTitles) {

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

    // 检查麦克风权限并请求
    private void checkMicPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // 请求麦克风权限
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MIC_PERMISSION);
        } else {
            // 如果已有权限，则调用 speechDemo()
            speechDemo();
        }
    }

    private void findCardsEdit(String query) {
        for (CardData cardData : visibleCardList) {
            if (cardData.getTitle().toLowerCase().contains(query) && !query.isEmpty()) {
                searchResults.add(cardData.getTitle());
            }
        }

        if (query.isEmpty()) {
            searchResultsList.setVisibility(View.GONE);
        } else {
            if (searchResults.isEmpty()) {
                searchResultsList.setVisibility(View.VISIBLE);
                searchResults.add("没有找到😣");
            } else {
                searchResultsList.setVisibility(View.VISIBLE);
            }
        }

        searchAdapter.notifyDataSetChanged();
    }

    private void findCards(String query) {
        for (CardData cardData : visibleCardList) {
            if (cardData.getTitle().toLowerCase().contains(query) && !query.isEmpty()) {
                searchResults.add(cardData.getTitle());
            }
        }

        if (query.isEmpty()) {
            Toast.makeText(getContext(), "请输入标题！", Toast.LENGTH_SHORT).show();
            searchResultsList.setVisibility(View.GONE);
        } else {
            if (searchResults.isEmpty()) {
                searchResultsList.setVisibility(View.VISIBLE);
                searchResults.add("没有找到😣");
            } else {
                searchResultsList.setVisibility(View.VISIBLE);
            }
        }

        searchAdapter.notifyDataSetChanged();
    }

    private void filterCards(String query) {

        for (int i = 0; i < visibleCardList.size(); i++) {
            if (visibleCardList.get(i).getTitle().toLowerCase().contains(query.toLowerCase())) {
//                recyclerView.smoothScrollToPosition(i); // 滚动到匹配项
                recyclerView.scrollToPosition(i); // 快速跳转
                return; // 找到后跳出
            }
        }
        Toast.makeText(getContext(), "未添加该卡片！", Toast.LENGTH_SHORT).show();
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                Toast.makeText(getContext(), "初始化失败，错误码：" + code, Toast.LENGTH_SHORT).show();
            }
        }
    };

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
//                visibleCardList.remove(position);
//                cardAdapter.notifyDataSetChanged();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void showAddCardDialog() {
        // 创建一个 Dialog
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_card_add);
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
                    LiveDataUtils.observeOnce(poemRepository.getCardDataLiveByTitle(newCardTitle), getViewLifecycleOwner(), cardData -> {
                        if (cardData != null) {
                            cardAdapter.addCard(cardData);  // 添加卡片
                            MyToastShow(R.drawable.toast_pic3, "新卡片添加成功！");
                            statisticsRepository.updateCheckCardFlag(newCardTitle);  // 更新 checkCardFlag
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


    private void showImageDialog(CardData data) {

        List<Question> questionList = data.getQuestions();

        // 创建一个 Dialog
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_dictate);

        // 设置 Dialog 背景透明
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // 获取容器 LinearLayout
        LinearLayout container = dialog.findViewById(R.id.scroll_container);  // 这是你要添加视图的 LinearLayout

        LayoutInflater inflater = LayoutInflater.from(getContext());

        // 检查 questionList 是否为空
        if (questionList == null || questionList.isEmpty()) {
            // 动态创建 TextView 显示 "暂无习题"
            TextView noQuestionsTextView = new TextView(getContext());
            noQuestionsTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics())));
            noQuestionsTextView.setText("暂无习题");
            noQuestionsTextView.setTextSize(18);
            noQuestionsTextView.setGravity(Gravity.CENTER);  // 居中显示
            container.addView(noQuestionsTextView);
        } else {
            // 创建一个列表来存储所有问题的 EditText 和 ImageView
            List<Pair<EditText, Question>> editTextQuestionPairs = new ArrayList<>();

            // 动态添加问题和答案的视图
            for (Question question : questionList) {
                // 使用布局填充器加载自定义的 XML 布局
                View questionAnswerView = inflater.inflate(R.layout.question_answer_item, container, false);

                // 设置问题文本
                TextView questionTextView = questionAnswerView.findViewById(R.id.question_text);
                questionTextView.setText(question.getQuestion());

                // 获取 EditText 和 ImageView
                EditText answerEditText = questionAnswerView.findViewById(R.id.answer_edit_text);
                ImageView checkImageView = questionAnswerView.findViewById(R.id.check_image_view);

                // 将 EditText 和对应的 Question 放入列表中
                editTextQuestionPairs.add(new Pair<>(answerEditText, question));

                // 将填充好的布局添加到容器中
                container.addView(questionAnswerView);
            }

            // 点击确认，检查所有问题的答案
            MaterialButton confirmBtn = dialog.findViewById(R.id.confirm_button);
            confirmBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // 获取当前日期
                    String currentDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());
                    PoemRepository poemRepository = new PoemRepository(dialog.getContext());

                    ExecutorService executor = Executors.newSingleThreadExecutor(); // 创建一个 ExecutorService
                    Handler mainHandler = new Handler(Looper.getMainLooper());  // 获取主线程的 Handler
                    executor.execute(() -> {
                        // 遍历所有 EditText 和 Question，检查用户输入的答案是否正确
                        for (Pair<EditText, Question> pair : editTextQuestionPairs) {
                            EditText editText = pair.first;
                            Question question = pair.second;

                            // 获取用户输入的答案
                            String userAns = editText.getText().toString().trim();
                            String correctAns = question.getAnswer().trim();

                            // 获取对应的 ImageView
                            ImageView ansCirticism = ((ViewGroup) editText.getParent()).findViewById(R.id.check_image_view);

                            // 检查答案是否正确
                            if (userAns.equals(correctAns)) {
                                // 在主线程更新 UI
                                mainHandler.post(() -> {
                                    ansCirticism.setVisibility(View.VISIBLE);
                                    ansCirticism.setImageResource(R.drawable.ic_correct);
                                });

                                poemRepository.incrementCounts(currentDate, false, true, false);

                            } else if (userAns.isEmpty()) {
                                // 没有作答的习题，忽略即可

                            }else {
                                // 答错的情况下在主线程更新 UI，并在后台保存错误记录
                                mainHandler.post(() -> {
                                    ansCirticism.setVisibility(View.VISIBLE);
                                    ansCirticism.setImageResource(R.drawable.ic_error);
                                });
                                // 将错误回答存入数据库
                                AnswerResult answerResult = new AnswerResult(
                                        data.getTitle(),
                                        question.getQuestion(),
                                        userAns,
                                        correctAns,
                                        currentDate
                                );

                                // 在保存答案后再进行计数更新
                                poemRepository.saveAnswerError(answerResult, () -> {
                                    poemRepository.incrementCounts(currentDate, false, true, false);
                                });
                            }
                        }
                    });
                    executor.shutdown();  // 关闭 ExecutorService
                }
            });
        }

        // 获取关闭按钮并设置点击事件
        ImageView closeButton = dialog.findViewById(R.id.dialog_close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // 显示 Dialog
        dialog.show();
    }

    private void showQaDialog(CardData data) {

        List<Question> questionList = data.getShort_answer_question();

        // 创建一个 Dialog
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_shortqa);

        // 设置 Dialog 背景透明
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // 获取容器 LinearLayout
        LinearLayout container = dialog.findViewById(R.id.scroll_container);  // 这是你要添加视图的 LinearLayout

        LayoutInflater inflater = LayoutInflater.from(getContext());

        // 检查 questionList 是否为空
        if (questionList == null || questionList.isEmpty()) {
            // 动态创建 TextView 显示 "暂无习题"
            TextView noQuestionsTextView = new TextView(getContext());
            noQuestionsTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics())));
            noQuestionsTextView.setText("暂无习题");
            noQuestionsTextView.setTextSize(18);
            noQuestionsTextView.setGravity(Gravity.CENTER);  // 居中显示
            container.addView(noQuestionsTextView);
        } else {
            // 创建一个列表来存储所有问题的 EditText 和 ImageView
            List<Pair<EditText, Question>> editTextQuestionPairs = new ArrayList<>();

            // 动态添加问题和答案的视图
            for (Question question : questionList) {
                // 使用布局填充器加载自定义的 XML 布局
                View questionAnswerView = inflater.inflate(R.layout.shortqa_item, container, false);

                // 设置问题文本
                TextView questionTextView = questionAnswerView.findViewById(R.id.question_text);
                questionTextView.setText(question.getQuestion());

                // 获取 EditText 和 ImageView
                EditText answerEditText = questionAnswerView.findViewById(R.id.answer_edit_text);
                TextView correctTitle = questionAnswerView.findViewById(R.id.correctTitle);
                TextView correctAnswerTextView = questionAnswerView.findViewById(R.id.correct_answer_text);
                TextView aiAnswerTitle = questionAnswerView.findViewById(R.id.aiTitle);
                TextView aiAnswerTextView = questionAnswerView.findViewById(R.id.ai_answer_text);

                // 将 EditText 和对应的 Question 放入列表中
                editTextQuestionPairs.add(new Pair<>(answerEditText, question));

                SpannableString correctAnswerTtile = new SpannableString("正确答案: ");
                correctAnswerTtile.setSpan(new ForegroundColorSpan(Color.BLACK), 0, correctAnswerTtile.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                correctAnswerTtile.setSpan(new StyleSpan(Typeface.BOLD), 0, correctAnswerTtile.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                correctTitle.setText(correctAnswerTtile);
                correctAnswerTextView.setText(question.getAnswer());

                SpannableString aiAnswerTtile = new SpannableString("批改情况: ");
                aiAnswerTtile.setSpan(new StyleSpan(Typeface.BOLD), 0, aiAnswerTtile.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                aiAnswerTtile.setSpan(new ForegroundColorSpan(Color.BLACK), 0, aiAnswerTtile.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                aiAnswerTitle.setText(aiAnswerTtile);

                // 将填充好的布局添加到容器中
                container.addView(questionAnswerView);
            }

            // 点击确认，检查所有问题的答案
            MaterialButton confirmBtn = dialog.findViewById(R.id.confirm_button);
            confirmBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // 获取当前日期
                    String currentDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());
                    PoemRepository poemRepository = new PoemRepository(dialog.getContext());

                    ExecutorService executor = Executors.newSingleThreadExecutor(); // 创建一个 ExecutorService
                    Handler mainHandler = new Handler(Looper.getMainLooper());  // 获取主线程的 Handler
                    executor.execute(() -> {
                        // 遍历所有 EditText 和 Question，检查用户输入的答案是否正确
                        for (Pair<EditText, Question> pair : editTextQuestionPairs) {
                            EditText editText = pair.first;
                            Question question = pair.second;

                            // 获取用户输入的答案
                            String userAns = editText.getText().toString().trim();
                            String correctAns = question.getAnswer().trim();
                            // 获取ai批改结果
                            // 获取 `questionAnswerView` 的根布局来查找 `aiAnswerTextView` 和 `correctAnswerTextView`
                            View questionAnswerView = (View) editText.getParent().getParent();
                            TextView aiAnswerTitle = questionAnswerView.findViewById(R.id.aiTitle);
                            TextView aiAnswerTextView = questionAnswerView.findViewById(R.id.ai_answer_text);
                            TextView correctTitle = questionAnswerView.findViewById(R.id.correctTitle);
                            TextView correctAnswerTextView = questionAnswerView.findViewById(R.id.correct_answer_text);

                            if (userAns.isEmpty()) {
                                //用户没有作答的问题不用处理
                            } else {
                                // 使用 Handler 切换到主线程更新 UI
                                mainHandler.post(() -> {
                                    // 设置正确答案文本并显示
                                    correctTitle.setVisibility(View.VISIBLE);
//                                    correctAnswerTextView.setText("正确答案: " + correctAns);
                                    correctAnswerTextView.setVisibility(View.VISIBLE);

                                    // 设置 AI 答案文本可见，并调用 `sendMessage` 获取 AI 答案
                                    aiAnswerTitle.setVisibility(View.VISIBLE);
                                    aiAnswerTextView.setText("正在批改...");
                                    aiAnswerTextView.setVisibility(View.VISIBLE);
                                });

                                Log.d(TAG, "onClick: userAns: " + userAns);
                                sendMessage(data.getTitle(), data.getOriginal_text(), question, userAns, correctAns, aiAnswerTextView, poemRepository, currentDate);
                            }
                        }

                    });
                    executor.shutdown();  // 关闭 ExecutorService
                }
            });
        }

        // 获取关闭按钮并设置点击事件
        ImageView closeButton = dialog.findViewById(R.id.dialog_close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // 显示 Dialog
        dialog.show();
    }

    private void showSpeechDialog(CardData data) {
        // 创建一个 Dialog
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.speech_ui);

        // 设置 Dialog 背景透明
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // 清空历史读入信息
        historyText = "";
//        LayoutInflater inflater = LayoutInflater.from(getContext());

        ImageView micro_click = dialog.findViewById(R.id.micro_click);
        MaterialButton confirmButton = dialog.findViewById(R.id.confirm_button);
        MaterialButton cleanButton = dialog.findViewById(R.id.clean_button);
        mResultText = dialog.findViewById(R.id.result);

        TextView ansShowTextView = dialog.findViewById(R.id.ansShow);

        // 调用语音模块
        micro_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // 调用语音识别
//                    speechDemo();
                    checkMicPermission();
                } catch (Exception e) {
                    // 捕获异常，避免闪退
                    e.printStackTrace();
                    Toast.makeText(getContext(), "语音识别初始化失败", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 点击确定按钮，开始批阅背诵结果
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 批阅背诵结果并显示
                String originText = data.getOriginal_text().trim();
//                String originText = "这是原文内容。";
                String userAns = mResultText.getText().toString().trim();
                mResultText.setText("");

                SpannableString resultText = compareTextsV2(originText, userAns);

                String htmlResultText = spannableToHtml(resultText);

                // 获取当前日期
                String currentDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());

                // 保存到数据库
                PoemRepository poemRepository = new PoemRepository(getContext());
                RecitationResult recitationResult = new RecitationResult(data.getTitle(), htmlResultText, currentDate);

                // 使用 ExecutorService 在后台线程中执行数据库操作
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    poemRepository.saveRecitationError(recitationResult);
                    Log.d(TAG, "onClick: ");
                    poemRepository.incrementCounts(currentDate, false, true, false);
                });
                executor.shutdown();  // 关闭 ExecutorService

                mResultText.setVisibility(View.GONE);
                ansShowTextView.setText(resultText);

            }
        });

        // 清空背诵内容
        cleanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                historyText = "";
                mResultText.setText("");
                ansShowTextView.setText("");
                mResultText.setVisibility(View.VISIBLE);
            }
        });

        // 获取关闭按钮并设置点击事件
        ImageView closeButton = dialog.findViewById(R.id.speech_dialog_close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                historyText = "";
                dialog.dismiss();
            }
        });

        // 显示 Dialog
        dialog.show();
    }

    // SpannableString 转 HTML
    public String spannableToHtml(SpannableString spannableString) {
        return Html.toHtml(spannableString, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
    }

    // HTML 转 SpannableString
    public SpannableString htmlToSpannable(String htmlString) {
        Spanned spanned = Html.fromHtml(htmlString, Html.FROM_HTML_MODE_COMPACT);
        return new SpannableString(spanned);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_MIC_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户授予了权限，继续执行 speechDemo()
                speechDemo();
            } else {
                // 用户拒绝了权限，显示提示信息
                Toast.makeText(getContext(), "需要麦克风权限才能进行语音识别", Toast.LENGTH_SHORT).show();
            }
        }
    }


    // 去除换行符后进行匹配
    public SpannableString compareTextsV1(String original, String recited) {
        // 去除换行符用于比较
        String cleanOriginal = original.replace("\n", "").replace("\r", "");
        String cleanRecited = recited.replace("\n", "").replace("\r", "");

        SpannableString spannableString = new SpannableString(original); // 使用原始文本用于显示
//        Log.d(TAG, "compareTexts: original:" + original);
//        Log.d(TAG, "compareTexts: recited:" + recited);
//        Log.d(TAG, "compareTexts: Coriginal:" + cleanOriginal);
//        Log.d(TAG, "compareTexts: Crecited:" + cleanRecited);

        int originalIndex = 0; // 用于追踪原文的索引

        for (int i = 0; i < cleanOriginal.length(); i++) {
            // 跳过换行符
            while (originalIndex < original.length() && original.charAt(originalIndex) == '\n') {
                originalIndex++;
            }

            if (i >= cleanRecited.length() || cleanOriginal.charAt(i) != cleanRecited.charAt(i)) {
//                Log.d(TAG, "compareTexts: mismatch: " + cleanOriginal.charAt(i) + " recited: " + (i < cleanRecited.length() ? cleanRecited.charAt(i) : "null"));
                // 标记错误的字为红色
                spannableString.setSpan(new ForegroundColorSpan(Color.RED), originalIndex, originalIndex + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            originalIndex++; // 更新原文索引
        }

        // 标记原文中剩余的字符（如果有）
        while (originalIndex < original.length()) {
            if (original.charAt(originalIndex) != '\n') { // 不标记换行
                spannableString.setSpan(new ForegroundColorSpan(Color.RED), originalIndex, originalIndex + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            originalIndex++;
        }

        return spannableString;
    }

    // 增加漏句匹配
    public SpannableString compareTextsV2(String original, String recited) {
        // 去除换行符用于比较
        String cleanOriginal = original.replace("\n", "").replace("\r", "");
        String cleanRecited = recited.replace("\n", "").replace("\r", "");

        SpannableString spannableString = new SpannableString(original); // 使用原始文本用于显示

        int originalIndex = 0; // 原文索引
        int recitedIndex = 0; // 背诵文本索引

        while (originalIndex < original.length()) {
            // 跳过原文中的换行符
            while (originalIndex < original.length() && original.charAt(originalIndex) == '\n') {
                originalIndex++;
            }

            if (recitedIndex < cleanRecited.length()) {
                // 如果当前字符不匹配，尝试向后查找匹配
                if (originalIndex < original.length() && original.charAt(originalIndex) == cleanRecited.charAt(recitedIndex)) {
                    recitedIndex++; // 匹配成功，继续下一个
                } else {
                    // 查找下一个匹配字符
                    boolean foundMatch = false;
                    for (int j = originalIndex + 1; j < original.length(); j++) {
                        if (original.charAt(j) == '\n') {
                            continue;
                        }
                        if (original.charAt(j) == cleanRecited.charAt(recitedIndex)) {
                            // 匹配上连续两个字才行
                            if (j < original.length() - 1 && recitedIndex < cleanRecited.length() - 1 && original.charAt(j+1) != cleanRecited.charAt(recitedIndex+1)) {
                                continue;
                            }

                            foundMatch = true;
                            // 这里可以标记之前的字符为红色
                            for (int k = originalIndex; k < j; k++) {
                                if (original.charAt(k) != '\n') { // 不标记换行
                                    spannableString.setSpan(new ForegroundColorSpan(Color.RED), k, k + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                            }
                            originalIndex = j; // 更新原文索引到找到的匹配
                            break; // 退出查找循环
                        }
                    }

                    if (!foundMatch) {
                        // 如果没有找到匹配，标记当前字符
                        spannableString.setSpan(new ForegroundColorSpan(Color.RED), originalIndex, originalIndex + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    recitedIndex++;
                }

            } else {
                // 如果背诵文本已经结束，标记剩余原文字符
                spannableString.setSpan(new ForegroundColorSpan(Color.RED), originalIndex, originalIndex + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            originalIndex++; // 更新原文索引
        }

        return spannableString;
    }

    private void speechDemo() {
        if (null == mIat) {
            // 创建单例失败，与 21001 错误为同样原因，
            // 参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            Toast.makeText(getContext(), "创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化", Toast.LENGTH_SHORT).show();
            return;
        }

        // 设置参数
        setParam();

        // 显示听写对话框
        mIatDialog.setListener(mRecognizerDialogListener);
        mIatDialog.show();
        //提示语为空，不显示提示语
        TextView txt = mIatDialog.getWindow().getDecorView().findViewWithTag("textlink");
        txt.setText("古诗文背诵中...");
        txt.setPaintFlags(txt.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG)); // 去掉下划线
        txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击后想做的事件
            }
        });

//        Toast.makeText(getContext(), "开始听写", Toast.LENGTH_SHORT).show();
    }

    /**
     * 听写参数设置
     */
    public void setParam() {
//        // 清空参数
//        mIat.setParameter(SpeechConstant.PARAMS, null);
//        // 设置听写引擎类型
//        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
//        // 设置返回结果格式【目前支持json,xml以及plain 三种格式，其中plain为纯听写文本内容】
//        mIat.setParameter(SpeechConstant.RESULT_TYPE, resultType);
//        //目前Android SDK支持zh_cn：中文、en_us：英文、ja_jp：日语、ko_kr：韩语、ru-ru：俄语、fr_fr：法语、es_es：西班牙语、
//        // 注：小语种若未授权无法使用会报错11200，可到控制台-语音听写（流式版）-方言/语种处添加试用或购买。
//        mIat.setParameter(SpeechConstant.LANGUAGE, language);
//        // 设置语言区域、当前仅在LANGUAGE为简体中文时，支持方言选择，其他语言区域时，可把此参数值设为mandarin。
//        // 默认值：mandarin，其他方言参数可在控制台方言一栏查看。
//        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
//        //获取当前语言（同理set对应get方法）
//        Log.e(TAG, "last language:" + mIat.getParameter(SpeechConstant.LANGUAGE));
        //此处用于设置dialog中不显示错误码信息
        //mIat.setParameter("view_tips_plain","false");
        //开始录入音频后，音频后面部分最长静音时长，取值范围[0,10000ms]，默认值5000ms
        mIat.setParameter(SpeechConstant.VAD_BOS, "3000");
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音取值范围[0,10000ms]，默认值1800ms。
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");
//        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
//        mIat.setParameter(SpeechConstant.ASR_PTT, "1");
//        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
//        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
//        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/helloword.wav");
    }


    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        /**
         * 识别回调成功
         */
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results);
            if (isLast) {
                historyText = mResultText.getText().toString().trim();
            }
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            Toast.makeText(getContext(), (error.getPlainDescription(true)), Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * 打印听写结果
     */
    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mIatResults.put(sn, text);
        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
//        String historyText = mResultText.getText().toString().trim();
        String newText = historyText + resultBuffer.toString();
        mResultText.setText(newText);
//        mResultText.setText(resultBuffer.toString());
        mResultText.setSelection(mResultText.length());
    }

    private static final String URL = "https://open.oppomobile.com/agentplatform/app_api/chat";
    private String myconversation_id;


    private void sendMessage(String title, String originalText, Question question, String userAns, String correctAns, TextView aiAnswerTextView, PoemRepository poemRepository, String currentDate) {

        String prompt = "《" + title + "》" + "的原文是：" + originalText + "\n" + "对于课后题：" + question + "\n标准正确答案是：" + correctAns + "\n用户的回答是：" + userAns + "\n";
        prompt +=  "请对用户做出的回答进行批改。";

        if (!prompt.isEmpty() && BEARER_TOKEN.trim().length() > "Bearer".length()) {

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("query", prompt);
            jsonObject.addProperty("response_mode", "streaming");
            jsonObject.addProperty("conversation_id", myconversation_id);
            jsonObject.addProperty("user", "8f3d226a-34c7-486c-b760-c8e8ada22ba4");

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    jsonObject.toString()
            );

            Request request = new Request.Builder()
                    .url(URL)
                    .post(body)
                    .addHeader("Authorization", BEARER_TOKEN)
                    .addHeader("Content-Type", "application/json")
                    .build();

            receiveMessage(request, aiAnswerTextView, title, question, userAns, correctAns, currentDate, poemRepository);
        }
    }

    private void receiveMessage(Request request, TextView aiAnswerTextView, String title, Question question, String userAns, String correctAns, String currentDate, PoemRepository poemRepository) {
        OkHttpClient client = new OkHttpClient();

        // 初始化 currentAnswer，用于累积回复内容
        final StringBuilder currentAnswer = new StringBuilder();

        // 创建 EventSource 工厂
        EventSource.Factory factory = EventSources.createFactory(client);
        EventSource eventSource = factory.newEventSource(request, new EventSourceListener() {
            @Override
            public void onOpen(EventSource eventSource, Response response) {
                // 连接已打开
                Log.d(TAG, "EventSource opened");
            }

            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                Log.d(TAG, "onEvent received data: " + data);

                // 过滤掉心跳消息，例如"ping"
                if ("ping".equalsIgnoreCase(data.trim())) {
                    Log.d(TAG, "Received ping, ignoring.");
                    return; // 不处理心跳消息
                }

                // 处理接收到的数据
                processResponseLine(data, currentAnswer);

                // 在主线程中更新 UI
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        aiAnswerTextView.setText(currentAnswer.toString()); // 更新消息内容
                    });
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                // 连接已关闭，意味着接收完毕
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
//                        ruleTextView.setText(currentAnswer.toString()); // 更新消息内容

                        // markdown渲染
                        // 创建 Markwon 实例
                        Markwon markwon = Markwon.create(rootView.getContext());
                        // 使用 Markwon 渲染 Markdown 到 TextView
                        markwon.setMarkdown(aiAnswerTextView, currentAnswer.toString());

                        ShortqaAnswerResult answerResult = new ShortqaAnswerResult(
                                title,
                                question.getQuestion(),
                                userAns,
                                correctAns,
                                currentAnswer.toString(),
                                currentDate
                        );

                        poemRepository.saveQaAnswerError(answerResult, () -> {
                            poemRepository.incrementCounts(currentDate, false, true, false);
                        });
                    });
                }

                Log.d(TAG, "EventSource closed");
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                // 处理错误
                Log.e(TAG, "Error: " + t.getMessage());

            }
        });
    }

    private void processResponseLine(String jsonString, StringBuilder currentAnswer) {
        try {
            JsonObject jsonObject = new Gson().fromJson(jsonString, JsonObject.class);

            // 检查响应中是否包含 answer 字段
            if (jsonObject.has("answer")) {
                String answer = jsonObject.get("answer").getAsString();
                currentAnswer.append(answer);
            }

            // 获取 conversation_id 并更新
            if (myconversation_id == null && jsonObject.has("conversation_id")) {
                myconversation_id = jsonObject.get("conversation_id").getAsString();
            }

        } catch (Exception e) {
            // 如果JSON解析失败，将响应视为纯文本
            currentAnswer.append(jsonString);
        }
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
