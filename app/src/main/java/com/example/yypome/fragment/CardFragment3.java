package com.example.yypome.fragment;

import static com.example.yypome.myapi.MyApi.BEARER_TOKEN;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yypome.R;
import com.example.yypome.data.CardData;
import com.example.yypome.data.niceSentencesData;
import com.example.yypome.db.PoemRepository;
import com.example.yypome.adapter.PoemSentenceAdapter;
import com.example.yypome.utils.TypewriterEffect;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CardFragment3#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CardFragment3 extends Fragment {

    private static final String TAG = "CardFragment3";
    private View rootView;

    private RecyclerView recyclerView;
    private PoemSentenceAdapter poemAdapter;
    private String poemTitle;
    private String niceSentence;
    private List<String> poemList;
    private String poemOriginText;
    private List<String> sentenceAppreciation;
    private List<niceSentencesData> niceSentencesList = new ArrayList<>();

    private static final String ARG_POEMSTITLE = "arg_poems_title";
    private static final String ARG_POEMS = "arg_poems";
    private static final String ARG_ORIGINTEXT = "arg_origin_text";
    private static final String ARG_NICE_SENTENCES_LIST = "arg_nice_sentences_list";

    private static final String ARG_Appreciation = "正在赏析该名句：";
    private String imageName = "_01pic6";

    private String modelAppreciation;
    private TextView sentenceAppreciationTextView;

    private String poemSentenceEvaluation;
    private TextView modelEvaluation;

    private static final String URL = "https://open.oppomobile.com/agentplatform/app_api/chat";

    private StringBuilder currentAnswer;
    private String myconversation_id;
    private boolean sendMessageNiceSentenceFlag;

    public CardFragment3() {
        // Required empty public constructor
    }

    public static CardFragment3 newInstance(String poemTitle, List<String> poemList, String original_text, List<niceSentencesData> niceSentencesList) {
        CardFragment3 fragment = new CardFragment3();
        Bundle args = new Bundle();
        args.putString(ARG_POEMSTITLE, poemTitle);
        args.putStringArrayList(ARG_POEMS, new ArrayList<>(poemList));
        args.putString(ARG_ORIGINTEXT, original_text);
        args.putSerializable(ARG_NICE_SENTENCES_LIST, new ArrayList<>(niceSentencesList));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            poemTitle = getArguments().getString(ARG_POEMSTITLE);
//            poemList = getArguments().getStringArrayList(ARG_POEMS);
            poemOriginText = getArguments().getString(ARG_ORIGINTEXT);
//            poemList = splitIntoSentences(poemOriginText);
            niceSentencesList = (List<niceSentencesData>) getArguments().getSerializable(ARG_NICE_SENTENCES_LIST);

        } else {
            poemList = new ArrayList<>();
            Log.w(TAG, "No poems provided to the fragment.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_card3, container, false);

        ImageView pen = rootView.findViewById(R.id.write_brush);
        pen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showWritePractiseDialog();
            }
        });

        // 实例化 JSONReader 并加载数据
//        JSONReader jsonReader = new JSONReader();
//        niceSentencesList = jsonReader.loadPoemData(getContext());

        // 遍历数据列表并输出每个标题和句子
        for (niceSentencesData poemData : niceSentencesList) {
            Log.d("PoemData", "Title: " + poemData.getTitle());
            for (Map<String, String> sentenceMap : poemData.getNice_sentences()) {
                for (Map.Entry<String, String> entry : sentenceMap.entrySet()) {
                    Log.d("PoemData", "Sentence: " + entry.getKey() + ", Appreciation: " + entry.getValue());
                }
            }
        }

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

//        if (poemList != null) {
//            poemList.clear();
//        }
        poemList = new ArrayList<>();  // 确保 poemList 已被初始化

        for (niceSentencesData poemData : niceSentencesList) {
//            Log.d(TAG, "onViewCreated: title " + poemData.getTitle());
//            Log.d(TAG, "onViewCreated: poemTitle" + poemTitle);
            if (poemData.getTitle().equals(poemTitle)) {
                // 获取匹配标题的 nice_sentences，并将所有字典的键添加到 poemList
                for (Map<String, String> sentenceMap : poemData.getNice_sentences()) {
                    poemList.addAll(sentenceMap.keySet());
                }
                break; // 找到后可以跳出循环
            }
        }

        PoemRepository repository = new PoemRepository(getActivity());
        // 从数据库中获取 nice_sentence
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {

//            List<CardData> tmpCard = repository.searchCardDataByTitle("池上");
//            CardData tmp = tmpCard.get(0);
//            Log.d(TAG, "onCreate: tmp:" + tmp.getNice_sentence());
            CardData poemCard = repository.getCardDataByTitle(poemTitle);
            if (poemList == null | poemList.isEmpty()) {
                poemList = poemCard.getNice_sentence();
            }

//            poemList = repository.getNiceSentencesByTitle(poemTitle);
//            Log.d(TAG, "onViewCreated: from db poemList:" + poemList);
//            Log.d(TAG, "onViewCreated: from db poemList:" + poemList.get(0) + " bool:" + poemList.get(0).equals(""));
            if (poemList == null || poemList.isEmpty() || poemList.get(0).equals("")) {
//                Log.d(TAG, "onViewCreated: Step1");
//                sendMessageNiceSentence(poemOriginText); // 获取niceSentence
                // 使用回调获取 niceSentence
                sendMessageNiceSentenceManager(poemOriginText, new OnNiceSentenceReceivedListener() {
                    @Override
                    public void onNiceSentenceReceived(String niceSentence) {
//                        Log.d(TAG, "onNiceSentenceReceived: niceSentence:" + niceSentence);
                        poemList = splitNiceSentences(niceSentence);

                        // 使用 ExecutorService 在后台线程中执行数据库查询操作
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.execute(() -> {
                            List<CardData> newCard = repository.searchCardDataByTitle(poemTitle);
                            if (!newCard.isEmpty()) {
                                CardData cardData = newCard.get(0);  // 获取第一条匹配的数据
                                cardData.setNice_sentence(poemList);  // 设置 nice_sentence
//                                Log.d(TAG, "onCreate: poemList1:" + poemList);
                                // 将更新后的数据插入数据库
                                repository.updateCardData(cardData);
                            } else {
                                // 如果数据库没有此条数据，可以插入新的 CardData
                                CardData newCardData = new CardData();
                                newCardData.setTitle(poemTitle);  // 假设 poemOriginText 是标题
                                newCardData.setNice_sentence(poemList);  // 设置新的 nice_sentence
                                repository.saveCardData(newCardData);
                            }

                            // 回到主线程更新 RecyclerView
                            getActivity().runOnUiThread(() -> updateRecyclerView(view, poemList));
                        });
                    }
                });

            } else {
                // 如果已经有 nice_sentence，直接使用

                // 回到主线程更新 UI
                getActivity().runOnUiThread(() -> {
//                    Log.d(TAG, "onViewCreated: Step2");
                    // 更新 RecyclerView
                    updateRecyclerView(view, poemList);
                });
            }

            // 回到主线程更新 UI
            getActivity().runOnUiThread(() -> {
//                Log.d(TAG, "PoemList:" + poemList);
                // 更新 UI，使用 poemList
            });
        });

    }

    private void updateRecyclerView(View view, List<String> poemList) {
        // 生成卡片
        recyclerView = view.findViewById(R.id.recycler_view_images);

        // 设置布局管理器为横向滚动
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        // 初始化适配器
        poemAdapter = new PoemSentenceAdapter(getActivity(), poemList, new PoemSentenceAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(String poemSentence, String poemSentenceAppreciation){
//                Log.d(TAG, "Item clicked: " + poemSentence);
                // 点击事件处理，显示图片弹出框
                showImageDialog(poemSentence, poemSentenceAppreciation);
            }
        });
        recyclerView.setAdapter(poemAdapter);
        poemAdapter.notifyDataSetChanged(); // 确保适配器更新

        // 可选：使用 PagerSnapHelper 实现分页效果
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * 显示包含图片的弹出对话框
     */
    private void showImageDialog(String poemSentence, String poemSentenceAppreciation) {
        // 创建一个 Dialog
        android.app.Dialog dialog = new android.app.Dialog(getContext());
        dialog.setContentView(R.layout.dialog_image);

        // 设置 Dialog 的大小（可选）
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // 获取句子内容
        TextView sentenceTitle = dialog.findViewById(R.id.sentence_title);
        sentenceTitle.setText(poemSentence);

        int appreciationFlag = 0;
        sentenceAppreciationTextView = dialog.findViewById(R.id.sentence_appreciation);
        // 载入已有的赏析
        for (niceSentencesData poemData : niceSentencesList) {
//            Log.d(TAG, "onViewCreated: title " + poemData.getTitle());
//            Log.d(TAG, "onViewCreated: poemTitle" + poemTitle);
            if (poemData.getTitle().equals(poemTitle)) {
                // 获取匹配标题的 nice_sentences，并将所有字典的键添加到 poemList
                for (Map<String, String> sentenceMap : poemData.getNice_sentences()) {
                    if (sentenceMap.containsKey(poemSentence)) {
                        // poemSentenceAppreciation 赋值为 poemSentence 对应的赏析
                        poemSentenceAppreciation = sentenceMap.get(poemSentence);
//                        sentenceAppreciationTextView.setText(poemSentenceAppreciation);
                        TypewriterEffect typewriter = new TypewriterEffect(sentenceAppreciationTextView, poemSentenceAppreciation);
                        typewriter.start();
                        appreciationFlag = 1;
                        break;
                    }
                }
                break; // 找到后可以跳出循环
            }
        }

        // 获取句子赏析
        if (appreciationFlag == 0) {
            // 没有赏析就获取赏析
            sentenceAppreciationTextView.setText(poemSentenceAppreciation);
            sendMessageAppreciation(poemSentence);
        }

        // 获取关闭按钮并设置点击事件
        ImageView closeButton = dialog.findViewById(R.id.dialog_close_button);
        closeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                dialog.dismiss();
            }
        });

        // 显示 Dialog
        dialog.show();
    }

    private void showWritePractiseDialog() {
        // 创建一个 Dialog
        android.app.Dialog dialog = new android.app.Dialog(getContext());
        dialog.setContentView(R.layout.dialog_practise);

        // 设置 Dialog 背景透明
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // 设置 Dialog 的大小（可选）
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // 点击确认按钮获取用户输入信息
        MaterialButton confirmButton = dialog.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getContext(), "已确认您的输入", Toast.LENGTH_SHORT).show();
                MyToastShow(R.drawable.toast_pic3, "已确认您的输入");
                // 在这里处理点击事件，比如获取 EditText 的内容
                EditText editText = dialog.findViewById(R.id.input_edit_text);
                String inputText = editText.getText().toString().trim();
                // 处理确认逻辑
                sendMessage(inputText);
            }
        });

        // 获取模型批改结果
        modelEvaluation = dialog.findViewById(R.id.evaluation_text);
        String modelEvalText = "模型评价结果";
        modelEvaluation.setText(modelEvalText);

        // 获取关闭按钮并设置点击事件
        ImageView closeButton = dialog.findViewById(R.id.dialog_close_button);
        closeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                dialog.dismiss();
            }
        });

        // 显示 Dialog
        dialog.show();

    }

    private void MyToastShow(int iconResId, String message) {
        // 获取布局服务
        LayoutInflater inflater = LayoutInflater.from(getContext());
        // 使用自定义布局
        View layout = inflater.inflate(R.layout.custom_toast, null);

        // 设置图标和文本
        ImageView toastIcon = layout.findViewById(R.id.toast_icon);
        toastIcon.setImageResource(iconResId); // 设置你想要的图标

        TextView toastText = layout.findViewById(R.id.toast_message);
        toastText.setText(message);

        // 创建并显示自定义的Toast
        Toast toast = new Toast(getContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    private void sendMessage(String messageText) {

        String prompt = "下面是我对《" + poemTitle + "》这篇古文中的名句做的造句练习，请帮我检查一下这个句子使用名句是否正确，并给出点评或修改建议：";
        prompt = prompt + messageText;

        if (!messageText.isEmpty() && BEARER_TOKEN != null) {

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

            currentAnswer = new StringBuilder();  // 初始化当前答案
            receiveMessage(request);
        }
    }

    private void receiveMessage(Request request) {
        OkHttpClient client = new OkHttpClient();

        // 初始化 currentAnswer，用于累积回复内容
        currentAnswer = new StringBuilder();

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
//                Log.d(TAG, "onEvent received data: " + data);

                // 过滤掉心跳消息，例如"ping"
                if ("ping".equalsIgnoreCase(data.trim())) {
//                    Log.d(TAG, "Received ping, ignoring.");
                    return; // 不处理心跳消息
                }

                // 处理接收到的数据
                processResponseLine(data);

                // 在主线程中更新 UI
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        poemSentenceEvaluation = currentAnswer.toString();
                        modelEvaluation.setText(poemSentenceEvaluation);
                    });
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                // 连接已关闭，意味着接收完毕
                Log.d(TAG, "EventSource closed");

                // 连接关闭时，确保 final 文本被正确保留
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        poemSentenceEvaluation = currentAnswer.toString();
                        modelEvaluation.setText(poemSentenceEvaluation);
                    });
                }
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                // 处理错误
                Log.e(TAG, "Error: " + t.getMessage());
            }
        });
    }

    private void processResponseLine(String jsonString) {
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

    private void sendMessageAppreciation(String messageText) {

        String prompt = "“" + messageText + "”是《" + poemTitle + "》这篇古文中的名句。请结合数据库检索和百度检索的结果给出其白话文翻译，并赏析该句子。";
//        prompt = prompt + messageText;

        if (!messageText.isEmpty() && BEARER_TOKEN != null) {

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

            currentAnswer = new StringBuilder();  // 初始化当前答案
            receiveMessageAppreciation(request);
        }
    }

    private void receiveMessageAppreciation(Request request) {
        OkHttpClient client = new OkHttpClient();

        // 初始化 currentAnswer，用于累积回复内容
        currentAnswer = new StringBuilder();

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
//                Log.d(TAG, "onEvent received data: " + data);

                // 过滤掉心跳消息，例如"ping"
                if ("ping".equalsIgnoreCase(data.trim())) {
                    Log.d(TAG, "Received ping, ignoring.");
                    return; // 不处理心跳消息
                }

                // 处理接收到的数据
                processResponseLine(data);

                // 在主线程中更新 UI
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        modelAppreciation = currentAnswer.toString();
                        sentenceAppreciationTextView.setText(modelAppreciation);
                    });
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                // 连接已关闭，意味着接收完毕
                Log.d(TAG, "EventSource closed");

                // 连接关闭时，确保 final 文本被正确保留
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        modelAppreciation = currentAnswer.toString();
                        sentenceAppreciationTextView.setText(modelAppreciation);
                    });
                }
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                // 处理错误
                Log.e(TAG, "Error: " + t.getMessage());
            }
        });
    }

    public void sendMessageNiceSentenceManager(String poemOriginText, OnNiceSentenceReceivedListener listener) {
        // 模拟异步操作
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            // 模拟耗时操作
            sendMessageNiceSentence(poemOriginText, listener);
//            if (sendMessageNiceSentenceFlag) {
//                listener.onNiceSentenceReceived(niceSentence);  // 回调返回结果
//            }
        });
    }

    // 创建回调接口
    public interface OnNiceSentenceReceivedListener {
        void onNiceSentenceReceived(String niceSentence);
    }


    private void sendMessageNiceSentence(String messageText, OnNiceSentenceReceivedListener listener) {

        String prompt = "以上是《" + poemTitle + "》的原文，请告诉我原文中的名句有哪些，列出名句，每个名句都用引号包围，不需要赏析等任何额外描述。";
        prompt = messageText + prompt;

        if (!messageText.isEmpty() && BEARER_TOKEN != null) {
            sendMessageNiceSentenceFlag = false;

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

            currentAnswer = new StringBuilder();  // 初始化当前答案
            receiveMessageNiceSentence(request, listener);
        }
    }

    private void receiveMessageNiceSentence(Request request, OnNiceSentenceReceivedListener listener) {
        OkHttpClient client = new OkHttpClient();

        // 初始化 currentAnswer，用于累积回复内容
        currentAnswer = new StringBuilder();

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
//                Log.d(TAG, "onEvent received data: " + data);

                // 过滤掉心跳消息，例如"ping"
                if ("ping".equalsIgnoreCase(data.trim())) {
                    Log.d(TAG, "Received ping, ignoring.");
                    return; // 不处理心跳消息
                }

                // 处理接收到的数据
                processResponseLine(data);

            }

            @Override
            public void onClosed(EventSource eventSource) {
                // 连接已关闭，意味着接收完毕
                Log.d(TAG, "EventSource closed");

                niceSentence = currentAnswer.toString();
//                Log.d(TAG, "onClosed: currentAnswer niceSentence:" + niceSentence);
//                sendMessageNiceSentenceFlag = true;
                // 直接在onClosed中触发回调
                getActivity().runOnUiThread(() -> {
                    listener.onNiceSentenceReceived(niceSentence);
                });
//                // 连接关闭时，确保 final 文本被正确保留
//                if (getActivity() != null) {
//                    getActivity().runOnUiThread(() -> {
//                        niceSentence = currentAnswer.toString();
//                    });
//                }
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                // 处理错误
                Log.e(TAG, "Error: " + t.getMessage());
            }
        });
    }

    //
    public static List<String> splitNiceSentences(String niceSentence) {
        // 你提供的文本
//        String text = "《桃花源记》中的名句有：\n\n" +
//                "\"土地平旷，屋舍俨然。\"\n" +
//                "\"有良田美池桑竹之属。\"\n" +
//                "\"阡陌交通，鸡犬相闻。\"\n" +
//                "\"其中往来种作，男女衣着，悉如外人。\"\n" +
//                "\"黄发垂髫，并怡然自乐。\"\n" +
//                "这些都是《桃花源记》中描述桃花源美好景象的名句。";

        // 正则表达式，匹配双引号之间的内容
        Pattern pattern = Pattern.compile("\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(niceSentence);

        // 用于存储提取出来的句子
        List<String> sentences = new ArrayList<>();

        // 循环查找所有匹配的句子
        while (matcher.find()) {
            // 获取引号中的内容
            sentences.add(matcher.group(1));
        }

        // 打印所有提取出来的句子
        for (String sentence : sentences) {

            System.out.println(sentence);
        }

        return sentences;
    }

    /**
     * 将输入的中文文本拆分成独立的句子。
     *
     * @param text 输入的中文文本。
     * @return 拆分后的句子列表。
     */
    public static List<String> splitIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return sentences;
        }

        // 定义中文句子结束符的正则表达式
        // 包含句号、感叹号、问号、分号等
        // 这里使用非贪婪匹配，确保匹配最短可能的句子
        String regex = "[^。！？；“”]+[。！？；]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        int lastMatchEnd = 0;
        boolean foundMatch = false;

        while (matcher.find()) {
            foundMatch = true;
            String sentence = matcher.group().trim();
            if (!sentence.isEmpty()) {
                sentences.add(sentence);
//                System.out.println("splitIntoSentences: " + sentence); // 控制台输出，您可以根据需要使用 Log.d(TAG, ...) 在 Android 中记录日志
                lastMatchEnd = matcher.end();
            }
        }

        // 处理最后一句可能不以标点符号结尾的情况
        if (foundMatch && lastMatchEnd < text.length()) {
            String remaining = text.substring(lastMatchEnd).trim();
            if (!remaining.isEmpty()) {
                sentences.add(remaining);
//                System.out.println("splitIntoSentences: " + remaining);
            }
        } else if (!foundMatch) {
            // 如果没有找到任何匹配，整个文本作为一个句子
            sentences.add(text.trim());
//            System.out.println("splitIntoSentences: " + text.trim());
        }

        return sentences;

    }

}