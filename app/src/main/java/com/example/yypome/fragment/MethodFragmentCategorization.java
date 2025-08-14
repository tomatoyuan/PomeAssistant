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

import org.w3c.dom.Text;

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
 * Use the {@link MethodFragmentCategorization#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MethodFragmentCategorization extends Fragment {

    private View rootView;
    private TextView categoryTextView;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String TAG = "MethodFragmentCategory";
    private final String MethodDescribe = "分类记忆法：赏析文章的写作手法，并列举出类似写作手法的古诗文，通过对写作手法的了解，以及同类型古诗文的联动记忆，达到更好的记忆效果。";


    public MethodFragmentCategorization() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MethodFragmentCategorization.
     */
    // TODO: Rename and change types and number of parameters
    public static MethodFragmentCategorization newInstance(String param1, String param2) {
        MethodFragmentCategorization fragment = new MethodFragmentCategorization();
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
        rootView = inflater.inflate(R.layout.fragment_method_categorization, container, false);


        TextView poemTitleView, poemOriginTextView;
        poemTitleView = rootView.findViewById(R.id.poem_title);
        poemOriginTextView = rootView.findViewById(R.id.original_text);

        poemTitleView.setText(mParam1);
        poemOriginTextView.setText(mParam2);

        categoryTextView = rootView.findViewById(R.id.category_text);

        if (mParam1.equals("石灰吟")) {
            String shihuiying = "当背诵《石灰吟》时，作者运用托物言志的手法，借吟石灰的锻炼过程，表现了作者不避千难万险，勇于自我牺牲，以保持忠诚清白品格的可贵精神。从托物言志的写作手法出发，我们可以同时去回忆郑燮的《竹石》(借竹子和石头的形象，表达了作者坚贞不屈、刚毅不拔的精神和高尚的品德)、于谦的《咏煤炭》（借煤炭的形象，表达了作者无私奉献、甘愿为国为民的精神。）、王冕的《白梅》（借白梅的形象，表达了作者高洁的品格和超凡脱俗的情怀）等，将这些故事放在一起，在极强记忆的同时加深对托物言志这一写作手法的理解。";
//            categoryTextView.setText("");
            TypewriterEffect typewriter = new TypewriterEffect(categoryTextView, shihuiying);
            typewriter.start();
        } else if (mParam1.equals("渔家傲·秋思")) {
            String yujiaao = "《渔家傲·秋思》通过借景抒情的手法，将边塞的自然景色与戍边将士的内心情感紧密结合，生动地表达了将士们的思乡之情和对边疆生活的感慨。范仲淹通过丰富的意象和深刻的情感表达，使读者能够感受到边塞生活的艰苦和戍边将士的思乡之情。从借景抒情的写作手法出发，我们可以回忆到王维《山居秋暝》（通过山间景色的描写表达了作者对山林秋景的喜爱和内心的宁静，以及对山居生活的向往和赞美。）、柳永《雨霖铃》（通过描绘秋日黄昏的凄凉景象、骤雨初歇、长亭送别、杨柳岸边的晨风和残月等意象，营造出一种离别的悲凉氛围。表达了作者对离别的伤感和对未来的不确定，以及对亲人的思念和孤独。）、马致远《天净沙·秋思》（通过对秋天景色的描绘，表达了作者对漂泊生活的孤独和思乡之情。）";
//            categoryTextView.setText();
            TypewriterEffect typewriter = new TypewriterEffect(categoryTextView, yujiaao);
            typewriter.start();
        } else {
            sendMessage();
        }

        TextView categoryMethodTextView = rootView.findViewById(R.id.category_method);

        categoryMethodTextView.setOnClickListener(new View.OnClickListener() {
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

        String prompt = MethodDescribe + "根据前面的描述，对文章《"+ mParam1 +"》给出分类记忆法辅助背诵。文章的全文如下：" + mParam2;

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
                        categoryTextView.setText(currentAnswer.toString()); // 更新消息内容
                    });
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                // 连接已关闭，意味着接收完毕
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // markdown渲染
                        // 创建 Markwon 实例
                        Markwon markwon = Markwon.create(rootView.getContext());
                        // 使用 Markwon 渲染 Markdown 到 TextView
                        markwon.setMarkdown(categoryTextView, currentAnswer.toString());

//                        categoryTextView.setText(currentAnswer.toString()); // 更新消息内容
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