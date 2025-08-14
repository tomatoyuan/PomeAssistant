package com.example.yypome.fragment;

import static com.example.yypome.myapi.MyApi.BEARER_TOKEN;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.yypome.R;
import com.example.yypome.utils.TypewriterEffect;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
 * Use the {@link MethodFragmentRule#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MethodFragmentRule extends Fragment {

    private View rootView;
    private TextView ruleTextView;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private final String ruleMethodDiscription = "规律记忆法是一种背诵方法，它利用文本中的规律性结构来帮助记忆。对于具有“重章叠唱”特点的课文，这种方法特别有效。例如，在背诵《诗经·周南》中的《芣苢》时，只需在固定的句式中替换表示采集中不同动作的动词即可。这种方法通过识别和记忆文本中的变化部分，从而简化了背诵过程。";
    private String TAG = "MethodFragmentRule";

    public MethodFragmentRule() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MethodFragmentRule.
     */
    // TODO: Rename and change types and number of parameters
    public static MethodFragmentRule newInstance(String param1, String param2) {
        MethodFragmentRule fragment = new MethodFragmentRule();
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
        rootView = inflater.inflate(R.layout.fragment_method_rule, container, false);

        ruleTextView = rootView.findViewById(R.id.rule_text);

        if (mParam1.equals("蒹葭")) {
            String jianjia = "使用规律记忆法背诵《蒹葭》：我们观察这首诗的结构。它分为三节，每节四句，句式和内容都有一定的规律性。\n每节的的第一句都是“蒹葭”开头，描述了蒹葭的不同状态，分别是“苍苍”，“萋萋”，“采采”，表现了时间的推移。\n每节的第二句都是“白露”开头，描述了露水的状态，分别是“为霜”，“未晞”，“未已”，也表现了时间的推移。\n每节的第三句都是“所谓伊人，在水一方”，只是在“水”的方位上有所不同，分别是“水中央”，“水之湄”，“水之涘”，这也是一种规律。\n 每节的第四句都是描述追寻伊人的困难和遥远，分别是“道阻且长”，“道阻且跻”，“道阻且右”。\n 我们可以根据这个规律来记忆这首诗。首先记住每节的第一句，描述蒹葭的状态。然后记住每节的第二句，描述露水的状态。接着记住每节的第三句，只是改变“水”的方位。最后记住每节的第四句，描述追寻的困难。我们可以开始练习背诵了。";
//            ruleTextView.setText();
            TypewriterEffect typewriter = new TypewriterEffect(ruleTextView, jianjia);
            typewriter.start();
        } else if (mParam1.equals("芣苢")) {
            String yuyi = "《芣苢》的行文规律非常鲜明，通过重复和动词变化的形式，增强了诗歌的节奏感和韵律感。\n重复结构记忆：每一句的前四个字都是“采采芣苢”，只需要记住这一点，后面的句子自然就会跟随这个模式。每一句的第五个字都是“薄言”，这也是一个固定的短语，容易记忆。\n 动词变化记忆：将动词按照顺序排列：采之、有之、掇之、捋之、袺之、襭之。可以通过动作来记忆这些动词，想象自己在采摘芣苢的过程：\n采之：用手采摘。有之：将采摘的芣苢放入篮子。掇之：弯腰拾取地上的芣苢。捋之：用手轻轻摘取叶子。袺之：用手提着衣服的衣襟来装芣苢。襭之：用手提起衣服的下摆来装芣苢。";
//            ruleTextView.setText();
            TypewriterEffect typewriter = new TypewriterEffect(ruleTextView, yuyi);
            typewriter.start();
        } else {
            sendMessage();
        }

        TextView ruleMethodTextView = rootView.findViewById(R.id.rule_method);

        ruleMethodTextView.setOnClickListener(new View.OnClickListener() {
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

        String prompt = ruleMethodDiscription + "根据前面的描述，对文章《"+ mParam1 +"》给出规则记忆法辅助背诵。文章的全文如下：" + mParam2;

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
                        ruleTextView.setText(currentAnswer.toString()); // 更新消息内容
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
                        markwon.setMarkdown(ruleTextView, currentAnswer.toString());
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