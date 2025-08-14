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

import java.util.Objects;

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
 * Use the {@link MethodFragmentExtractIdeas#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MethodFragmentExtractIdeas extends Fragment {

    private View rootView;
    private TextView extractIdeaTextView;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private final String MethodDescribe = "提取观点记忆法：适用于背诵议论文类型的文言文。通过提取论点、论证方式、支持论证实例和事实，分析文章的结构，帮助记忆。";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String TAG = "MethodFragmentExtractIdeas";

    public MethodFragmentExtractIdeas() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MethodFragmentExtractIdeas.
     */
    // TODO: Rename and change types and number of parameters
    public static MethodFragmentExtractIdeas newInstance(String param1, String param2) {
        MethodFragmentExtractIdeas fragment = new MethodFragmentExtractIdeas();
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
        rootView = inflater.inflate(R.layout.fragment_method_extract_ideas, container, false);

        extractIdeaTextView = rootView.findViewById(R.id.extract_idea_text);

        // 为了效果，人工干预
        Log.d(TAG, "onCreateView: title: " + mParam1);
        if (Objects.equals(mParam1, "劝学")) {
            String quanxue = "《劝学》全文论证思路清晰，结构一目了然,可以按照作者的观点将文章划分为以下几个部分：\n\n1、中心论点:学不可以已（开篇点题，总领全篇）。\n2、学习的意义(5 个比喻)--提高或改变自己\n3、学习的作用(5 个比喻)--弥补不足\n4、学习的态度方法(10 个比喻正反对比)--积累、坚持、专一。\n\n《劝学》的行文开篇立论，第一段就提出中心论点--“学不可以已《劝学》\n\n第二段用了五个比喻句，“青取之于蓝而青于蓝”“冰水为之而寒于水”“木直中绳鞣以为轮”“木受绳则直”“金就砺则利”，阐述了事物通过一定变化，可以提高自身或改变原来的状态，推论得出君子(人)必须通过学习和不断参省自身才能达到“知明而行无过”的境地。本段是从学习的重要性(意义)这个角度来论述中心论点的。\n\n《劝学》第三段用了十个比喻句，并且正反对比加以阐述，从“积土”“积水”推论到“人的积德”，正面论述积累的作用，得出学习上的成就是不断积累起来的。“不积跬步”“不积小流”从反面阐述如果不积累就不能达到远大目标。这是本段第一个层次，阐述学习要积累。“骐骥”“驽马”对比，得出主观条件的好坏，不是学习的决定因素，坚持不懈才是学好的关键；“锲而不舍”“锲而舍之”对照，阐述只有坚持不懈、持之以恒，才会有所成就。蚓和蟹两个比喻正反对照，阐述做到积累还要专一。本段是从学习的方法和态度这个角度来论述中心论点的。\n\n《劝学》全文论证思路清晰，结构一目了然。首先是《劝学》运用了大量的比喻，把抽象的道理阐述的浅显易懂、生动形象、深入浅出、容易接受；其次是《劝学》中比喻形式多样，有类比、有对比、有正面、有反面，综合运用；再次就是《劝学》中比喻和说理结合紧密，运用灵活，有的只设喻而把道理隐含其中，有的先设喻再引出要说的道理。";
//            extractIdeaTextView.setText();
            TypewriterEffect typewriter = new TypewriterEffect(extractIdeaTextView, quanxue);
            typewriter.start();
        } else if (Objects.equals(mParam1, "师说")) {
            String shishuo = "《师说》结构鲜明、破例结合、正反对比，可以分为以下几个部分:\n\n中心论点：人必从师，以“道”为师(理论依据)(立)\n 士大夫耻学于师(反面现象) (破)\n（三层对比）孔子从事的言行(正面事例) （立）\n李蟠从师行古道(身边活例)\t（立）\n\n《师说》的行文开篇立论，第一段提出中心论点--“古之学者必有师”，接着阐述师的职能--“师者，所以传道受业解惑也”又指出学者存疑“人非生而知之者，孰能无惑和从师学习的正确途径--“无贵无贱，无长无少，道之所存，师之所存也”。\n\n作者在首段阐述了从师学习的必要性和重要性。\n\n《师说》第二段重点批判了“士大夫”耻于从师的陋习，作者借三组对比：\n1、“古之圣人”与“今之众人”对比，得出“圣益圣，愚益愚，圣人之所以为圣，愚人之所以为愚，其皆出于此乎”的结论，带有疑问语气，简单的说就是造成圣愚的原因是从师与否；\n2、“爱其子，择师而教之”与“于其身也，则耻师焉”对比，得出 “句读之不知，惑之不解，或师焉，或不焉，小学而大遗，吾未见其明也”的结论，带有肯定语气，讽刺了士大夫在从师问题上学小遗大；\n3、“巫医乐师百工之人，不耻相师”与“士大夫之族日师日弟子云者则群聚而笑之”对比，得出“师道之不复，可知矣。巫医乐师百工之人，君子不齿，今其智乃反不能及，其可怪也欤”的结论，带有讽刺语气，讽刺了士大夫之流虚荣自误、迂腐可笑的思想行为。这样层层对比，从后果、行为、心理等方面逐层深入分析，深刻揭露了当时耻学于师的社会陋习，并具有强烈的说服力。\n\n《师说》第三段以圣人孔子为例，运用例证法，进一步阐释了师道、师生关系，从正面论证了自己理论的正确--“弟子不必不如师，师不必贤于弟子”。在人们眼中，孔子是圣人，圣人尚且如此，那一般人就更不必说了，虽用寥寥数语却把孔子的言行写得很具体，因而很有说服力。\n\n《师说》第四段赞扬李蟠“能行古道”，点明作《师说》的原因。";
//            extractIdeaTextView.setText();
            TypewriterEffect typewriter = new TypewriterEffect(extractIdeaTextView, shishuo);
            typewriter.start();
        } else {
            sendMessage();
        }

        TextView extractIdeaMethodTextView = rootView.findViewById(R.id.extract_idea_method);

        extractIdeaMethodTextView.setOnClickListener(new View.OnClickListener() {
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

        String prompt = MethodDescribe + "根据前面的描述，对文章《"+ mParam1 +"》给出提取观点记忆法辅助背诵。文章的全文如下：" + mParam2;

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
                        extractIdeaTextView.setText(currentAnswer.toString()); // 更新消息内容
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
                        markwon.setMarkdown(extractIdeaTextView, currentAnswer.toString());
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