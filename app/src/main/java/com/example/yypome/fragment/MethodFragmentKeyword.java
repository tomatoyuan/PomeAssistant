package com.example.yypome.fragment;

import static com.example.yypome.myapi.MyApi.BEARER_TOKEN;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.yypome.R;
import com.example.yypome.adapter.SentenceAdapter;
import com.example.yypome.data.Sentence;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.noties.markwon.Markwon;
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
 * Use the {@link MethodFragmentKeyword#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MethodFragmentKeyword extends Fragment {

    private View rootView;
    private TextView keywordTextView;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String TAG = "MethodFragmentKeyword";

    private final String MethodDescribe = "关键词记忆法：重点强调关键字帮助记忆，基于关键词联系文章内容与结构。关键词记忆法是一种背诵方法，它通过抓住文章中的关键性动词、名词等来帮助记忆。";

    public MethodFragmentKeyword() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MethodFragmentKeyword.
     */
    // TODO: Rename and change types and number of parameters
    public static MethodFragmentKeyword newInstance(String param1, String param2) {
        MethodFragmentKeyword fragment = new MethodFragmentKeyword();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_method_keyword, container, false);

        sendMessage();

        keywordTextView = rootView.findViewById(R.id.keyword_text);

        TextView keywordMethodTextView = rootView.findViewById(R.id.keyword_method);

        keywordMethodTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        return rootView;
    }

    private static final String URL = "https://open.oppomobile.com/agentplatform/app_api/chat";
    private String myconversation_id;


    private void sendMessage() {

        String prompt = MethodDescribe + "根据前面的描述，对文章《"+ mParam1 +"》给出关键词记忆法辅助背诵。文章的全文如下：" + mParam2;

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

            receiveMessage(request);
        }
    }

    private void receiveMessage(Request request) {
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
                        keywordTextView.setText(currentAnswer.toString()); // 更新消息内容
                    });
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                // 连接已关闭，意味着接收完毕
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
//                        keywordTextView.setText(currentAnswer.toString()); // 更新消息内容

                        // markdown渲染
                        // 创建 Markwon 实例
                        Markwon markwon = Markwon.create(rootView.getContext());
                        // 使用 Markwon 渲染 Markdown 到 TextView
                        markwon.setMarkdown(keywordTextView, currentAnswer.toString());
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

}