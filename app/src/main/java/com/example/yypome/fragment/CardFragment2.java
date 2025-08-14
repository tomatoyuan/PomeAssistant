package com.example.yypome.fragment;

import static com.example.yypome.myapi.MyApi.BEARER_TOKEN;

import android.graphics.Rect;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.yypome.R;
import com.example.yypome.adapter.ChatAdapter;
import com.example.yypome.data.ChatMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
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
 * Use the {@link CardFragment2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CardFragment2 extends Fragment {

    private View rootView;

    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener; // 声明监听器对象(控制编辑框与输入法的相对位置)

    private RecyclerView recyclerView;
    private EditText editTextMessage;
    private ImageButton buttonSend;

    private ArrayList<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;

    private static final String URL = "https://open.oppomobile.com/agentplatform/app_api/chat";
    private StringBuilder currentAnswer;
    private String myconversation_id;

    private static final String TAG = "CardFragment2";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CardFragment2() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CardFragment2.
     */
    // TODO: Rename and change types and number of parameters
    public static CardFragment2 newInstance(String param1, String param2) {
        CardFragment2 fragment = new CardFragment2();
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
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_card2, container, false);
        }

        initView(rootView);

        return rootView;
    }

    private void initView(View rv) {
        recyclerView = rv.findViewById(R.id.recycler_view);
        editTextMessage = rv.findViewById(R.id.edit_text_message);
        buttonSend = rv.findViewById(R.id.button_send);

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, rv.getContext());

        recyclerView.setAdapter(chatAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(rv.getContext()));

        recyclerView.setItemAnimator(null);

        ChatMessage initChatMessage = new ChatMessage("你好，我是你的古诗文背诵助手，请问有什么可以帮你的？", false, System.currentTimeMillis());
        chatMessages.add(initChatMessage);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }

            private void sendMessage() {
                String messageText = editTextMessage.getText().toString().trim();
                if (!messageText.isEmpty() && BEARER_TOKEN != null) {
                    ChatMessage chatMessage = new ChatMessage(messageText, true, System.currentTimeMillis());
                    chatMessages.add(chatMessage);
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    recyclerView.scrollToPosition(chatMessages.size() - 1);
                    editTextMessage.setText("");

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("query", messageText);
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

                // 初始化一个 ChatMessage，用于显示模型的回复
                ChatMessage chatMessage = new ChatMessage("", false, System.currentTimeMillis());
                chatMessages.add(chatMessage);
                int messagePosition = chatMessages.size() - 1;

                // 在主线程中通知适配器插入了新消息，并滚动到最新位置
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        chatAdapter.notifyItemInserted(messagePosition);
                        recyclerView.scrollToPosition(messagePosition);
                    });
                }

                // 创建 EventSource 工厂
                EventSource.Factory factory = EventSources.createFactory(client);
                EventSource eventSource = factory.newEventSource(request, new EventSourceListener() {
                    @Override
                    public void onOpen(EventSource eventSource, Response response) {
                        // 连接已打开
//                        Log.d(TAG, "EventSource opened");
                    }

                    @Override
                    public void onEvent(EventSource eventSource, String id, String type, String data) {
//                        Log.d(TAG, "onEvent received data: " + data);

                        // 过滤掉心跳消息，例如"ping"
                        if ("ping".equalsIgnoreCase(data.trim())) {
//                            Log.d(TAG, "Received ping, ignoring.");
                            return; // 不处理心跳消息
                        }

                        // 处理接收到的数据
                        processResponseLine(data, chatMessage);

                        // 在主线程中更新 UI
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                chatMessage.setText(currentAnswer.toString()); // 更新消息内容
                                chatAdapter.notifyItemChanged(chatMessages.size() - 1); // 通知 RecyclerView 更新当前消息
//                                chatAdapter.notifyDataSetChanged();
                                recyclerView.scrollToPosition(chatMessages.size() - 1); // 滚动到最新位置
                            });
                        }
                    }

                    @Override
                    public void onClosed(EventSource eventSource) {
                        // 连接已关闭，意味着接收完毕
//                        Log.d(TAG, "EventSource closed");

                        // 连接关闭时，确保 final 文本被正确保留
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                chatMessage.setText(currentAnswer.toString());  // 再次确认文本内容

//                                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(chatMessages.size() - 1);
//
//                                if (viewHolder instanceof ChatAdapter.ReceivedMessageViewHolder) {
//                                    ChatAdapter.ReceivedMessageViewHolder receivedViewHolder = (ChatAdapter.ReceivedMessageViewHolder) viewHolder;
//
//                                    // 渲染 Markdown 文本
//                                    receivedViewHolder.renderMarkdown(currentAnswer.toString());
//                                }

                                chatAdapter.notifyItemChanged(chatMessages.size() - 1);  // 通知 RecyclerView 更新当前消息
                                recyclerView.scrollToPosition(chatMessages.size() - 1);  // 滚动到最新位置
                            });
                        }
                    }

                    @Override
                    public void onFailure(EventSource eventSource, Throwable t, Response response) {
                        // 处理错误
//                        Log.e(TAG, "Error: " + t.getMessage());
                    }
                });
            }


            private String extractImageUrl(String text) {
                // 去除文本中的换行符，使URL连续
                String textWithoutLineBreaks = text.replace("\n", "");

                // 正则表达式匹配图片URL
                String urlPattern = "(https?://[\\w\\-\\.\\~:/%\\?#\\[\\]@!\\$&'\\(\\)\\*\\+,;=]+\\.(png|jpg|jpeg|gif))";

                Pattern pattern = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(textWithoutLineBreaks);

                if (matcher.find()) {
                    return matcher.group(1);
                } else {
                    return null;
                }
            }

            private void processResponseLine(String jsonString, ChatMessage chatMessage) {
                try {
                    JsonObject jsonObject = new Gson().fromJson(jsonString, JsonObject.class);

                    // 检查响应中是否包含 answer 字段
                    if (jsonObject.has("answer")) {
                        String answer = jsonObject.get("answer").getAsString();
                        currentAnswer.append(answer);
                        chatMessage.setText(currentAnswer.toString());

                        // 尝试提取图像URL（假设返回的文本中有图片链接）
                        String potentialImageUrl = extractImageUrl(currentAnswer.toString());

                        if (potentialImageUrl != null) {
                            chatMessage.setImageUrl(potentialImageUrl);  // 将图片链接存储在 ChatMessage 对象中
//                            Log.d(TAG, "Extracted Image URL: " + potentialImageUrl);
                        }
                    }

                    // 获取 conversation_id 并更新
                    if (myconversation_id == null && jsonObject.has("conversation_id")) {
                        myconversation_id = jsonObject.get("conversation_id").getAsString();
                    }

                } catch (Exception e) {
                    // 如果JSON解析失败，将响应视为纯文本
                    currentAnswer.append(jsonString);
                    chatMessage.setText(currentAnswer.toString());
                }
            }

        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final View rootViewTemp = view.getRootView(); // 获取根布局视图

        globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootViewTemp.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootViewTemp.getHeight();

                // 计算键盘高度
                int keypadHeight = screenHeight - r.bottom;
                if (keypadHeight > screenHeight * 0.15) { // 如果键盘高度超过屏幕的15%
                    // 键盘显示，滚动到适合的区域
                    rootViewTemp.scrollTo(0, keypadHeight);
                } else {
                    // 键盘隐藏，恢复原始状态
                    rootViewTemp.scrollTo(0, 0);
                }
            }
        };

        // 注册监听器
        rootViewTemp.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        View rootViewTemp = getView();
        if (rootViewTemp != null && globalLayoutListener != null) {
            // 移除监听器
            rootViewTemp.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
        }
    }
}
