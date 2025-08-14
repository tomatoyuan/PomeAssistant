package com.example.yypome.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yypome.R;
import com.example.yypome.adapter.SentenceAdapter;
import com.example.yypome.data.Sentence;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MethodFragmentAssociation#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MethodFragmentAssociation extends Fragment {

    private View rootView;

    private RecyclerView recyclerViewSentences;
    private SentenceAdapter sentenceAdapter;
    private ArrayList<Sentence> sentenceArrayList;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "title";
    private static final String ARG_PARAM2 = "origin_text";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private static String TAG = "MethodFragmentAssociation";

    public MethodFragmentAssociation() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MethodFragmentAssociation.
     */
    // TODO: Rename and change types and number of parameters
    public static MethodFragmentAssociation newInstance(String param1, String param2) {
        MethodFragmentAssociation fragment = new MethodFragmentAssociation();
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
        rootView = inflater.inflate(R.layout.fragment_method_association, container, false);

        // 拆分句子
        List<String> sentences = splitIntoSentences(mParam2);
        int imageId = R.drawable.toast_pic1;

        // 初始化 RecyclerView
        recyclerViewSentences = rootView.findViewById(R.id.recyclerViewSentences);
        recyclerViewSentences.setLayoutManager(new LinearLayoutManager(getActivity()));

        // 初始化数据列表
        sentenceArrayList = new ArrayList<>();

        for (int i = 0; i < sentences.size(); i++) {
            sentenceArrayList.add(new Sentence(sentences.get(i), null));
        }

        // 初始化适配器
        sentenceAdapter = new SentenceAdapter(getActivity(), sentenceArrayList, mParam1);
        recyclerViewSentences.setAdapter(sentenceAdapter);

        return rootView;
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