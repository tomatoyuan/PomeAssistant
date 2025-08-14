package com.example.yypome.fragment;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.yypome.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MethodFragmentOcclusion#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MethodFragmentOcclusion extends Fragment {

    private View rootView;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView textView, tvPercentage;
    private SeekBar seekBar;
    private List<Integer> hiddenIndexes = new ArrayList<>();  // 被遮挡的句子或词的索引列表
    private List<Boolean> visibilityFlags = new ArrayList<>();  // 记录每个遮挡位置的可见性
    private List<String> splitText;  // 分割后的文本列表
    private List<String> nonPunctuationWords;  // 只包含非标点符号的词汇
    private float currentOcclusionRate = 0.5f;  // 默认遮挡率为50%
    private String TAG = "MethodFragmentOcclusion";

    public MethodFragmentOcclusion() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MethodFragmentOcclusion.
     */
    // TODO: Rename and change types and number of parameters
    public static MethodFragmentOcclusion newInstance(String param1, String param2) {
        MethodFragmentOcclusion fragment = new MethodFragmentOcclusion();
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
        rootView = inflater.inflate(R.layout.fragment_method_occlusion, container, false);

        textView = rootView.findViewById(R.id.textView);
        tvPercentage = rootView.findViewById(R.id.tv_percentage);
        seekBar = rootView.findViewById(R.id.seekBar);

        // 分割文本为独立的文字和标点符号
        splitText = splitTextAndPunctuation(mParam2);

        // 过滤掉标点符号，生成 nonPunctuationWords
        nonPunctuationWords = new ArrayList<>();
        for (String word : splitText) {
            if (!isPunctuation(word)) {
                nonPunctuationWords.add(word);  // 只保留非标点符号的单词或句子
            }
        }

        // 初始化遮挡
        initOcclusion();

        // SeekBar 监听器，根据用户拖动修改遮挡率
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 将 SeekBar 的进度值（0-100）转换为 0-1.0 的遮挡率
                currentOcclusionRate = progress / 100.0f;
                tvPercentage.setText(String.format("遮挡率: %d%%", progress));

                // 重新初始化遮挡
                initOcclusion();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No action needed
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // No action needed
            }
        });

        return rootView;
    }

    // 初始化遮挡，根据当前的遮挡率设置
    private void initOcclusion() {
        hiddenIndexes.clear();
        visibilityFlags.clear();

        Random random = new Random();
        int numberOfHiddenWords = (int) (nonPunctuationWords.size() * currentOcclusionRate);  // 根据遮挡率计算需要遮挡的非标点符号的单词或短句数量

        Log.d(TAG, "initOcclusion: NUMWORDS" + numberOfHiddenWords);

        // 随机选择非标点符号的单词进行遮挡
        List<Integer> occlusionCandidates = new ArrayList<>();
        for (int i = 0; i < splitText.size(); i++) {
            if (!isPunctuation(splitText.get(i))) {
                occlusionCandidates.add(i);  // 将非标点符号的词加入候选列表
            }
        }

        // 使用 HashSet 来存储唯一的遮挡索引
        Set<Integer> uniqueHiddenIndexes = new HashSet<>();

        // 随机选取不重复的遮挡索引
        while (uniqueHiddenIndexes.size() < numberOfHiddenWords) {
            int randomIndex = occlusionCandidates.get(random.nextInt(occlusionCandidates.size()));
            uniqueHiddenIndexes.add(randomIndex);  // HashSet 保证唯一性
        }

        // 将唯一的遮挡索引保存到 hiddenIndexes 中
        hiddenIndexes.addAll(uniqueHiddenIndexes);

        // 初始化可见性标志位，默认隐藏
        for (int i = 0; i < splitText.size(); i++) {
            visibilityFlags.add(hiddenIndexes.contains(i) ? false : true);
        }

        // 更新文本内容
        updateText();
    }

    private void updateText() {
        StringBuilder modifiedText = new StringBuilder();

        for (int i = 0; i < splitText.size(); i++) {
            String word = splitText.get(i);

            // 如果是标点符号或换行符，直接添加
            if (isPunctuation(word) || word.equals("\n")) {
                modifiedText.append(word);
            }
            // 如果该单词/句子需要被遮挡
            else if (!visibilityFlags.get(i)) {
                modifiedText.append(generateUnderlines(word));  // 使用全角下划线替换
            } else {
                modifiedText.append(word);  // 显示正常的文字
            }
        }

        // 将修改后的文本转换为SpannableString
        SpannableString spannableString = new SpannableString(modifiedText.toString());

        int startIndex = 0;
        for (int i = 0; i < splitText.size(); i++) {
            final int index = i;
            String word = splitText.get(i);
            String displayText = visibilityFlags.get(i) ? word : generateUnderlines(word);
            int endIndex = startIndex + displayText.length();

            // 点击事件处理，自定义ClickableSpan移除默认蓝色和下划线
            if (!isPunctuation(word) && !word.equals("\n")) {  // 只对非标点符号部分设置点击事件
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        // 切换可见性
                        boolean currentVisibility = visibilityFlags.get(index);
                        visibilityFlags.set(index, !currentVisibility);

                        // 更新文本显示
                        updateText();
                    }

                    @Override
                    public void updateDrawState(android.text.TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(false); // 不显示下划线
                        ds.setColor(ContextCompat.getColor(getActivity(), android.R.color.black)); // 黑色字体
                    }
                };

                spannableString.setSpan(clickableSpan, startIndex, endIndex, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            // 计算下一个单词的起始位置
            startIndex += displayText.length();
        }

        // 设置Spannable文本到TextView
        textView.setText(spannableString);
        textView.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
    }


    // 生成与单词长度相同的全角下划线，保持与中文字符相同的宽度，保留换行符
    private String generateUnderlines(String text) {
        StringBuilder underlines = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                underlines.append('\n');  // 保留换行符
            } else {
                underlines.append("\uFF3F");  // 全角下划线，保证与中文字符宽度一致
            }
        }
        return underlines.toString();
    }


    // 判断一个字符串是否是标点符号
    private boolean isPunctuation(String word) {
        // 正则表达式匹配中文标点符号和常见英文标点
        Pattern punctuationPattern = Pattern.compile("[\\p{Punct}，。？！；：、‘’“”]");
        return punctuationPattern.matcher(word).matches();
    }

    // 分割文本为文字、标点符号和换行符的独立列表
    private List<String> splitTextAndPunctuation(String text) {
        List<String> result = new ArrayList<>();
        // 正则表达式匹配标点符号、换行符或单词
        Matcher matcher = Pattern.compile("[\\w]+|[\\p{Punct}，。？！；：、‘’“”]|\\n").matcher(text);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }


}