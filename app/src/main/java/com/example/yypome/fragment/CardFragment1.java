package com.example.yypome.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.yypome.R;
import com.google.android.material.imageview.ShapeableImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CardFragment1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CardFragment1 extends Fragment {

    private View rootView;
    TextView[] contentTextViews = new TextView[8];  // 用于存储四个TextView
    ImageView[] expandIcons = new ImageView[8];  // 用于存储四个ImageView
    ShapeableImageView imageView;

    private SparseBooleanArray expandState = new SparseBooleanArray(); // 用于记录每个item的展开状态

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "str_original_text";
    private static final String ARG_PARAM2 = "str_translation_text";
    private static final String ARG_PARAM3 = "str_comment_text";
    private static final String ARG_PARAM4 = "str_author_text";
    private static final String ARG_PARAM5 = "image";
    private static final String ARG_PARAM6 = "work_introduction";
    private static final String ARG_PARAM7 = "creative_bg";
    private static final String ARG_PARAM8 = "appreciation";
    private static final String ARG_PARAM9 = "approach";
    private static final String ARG_PARAM10 = "style";

    // TODO: Rename and change types of parameters
    private String str_original_text;
    private String str_translation_text;
    private String str_comment_text;
    private String str_author_text;
    private String image;
    private String str_work_intro;
    private String str_creative_bg;
    private String str_appreciation;
    private String str_approach;
    private String str_style;

    public CardFragment1() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1            Parameter 1.
     * @param param2            Parameter 2.
     * @param work_introduction
     * @param createtive_bg
     * @param work_appreciation
     * @return A new instance of fragment CardFragment1.
     */
    // TODO: Rename and change types and number of parameters
    public static CardFragment1 newInstance(String param1, String param2, String param3, String param4, String param5, String work_introduction, String createtive_bg, String work_appreciation, String str_approach, String str_style) {
        CardFragment1 fragment = new CardFragment1();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString(ARG_PARAM3, param3);
        args.putString(ARG_PARAM4, param4);
        args.putString(ARG_PARAM5, param5);
        args.putString(ARG_PARAM6, work_introduction);
        args.putString(ARG_PARAM7, createtive_bg);
        args.putString(ARG_PARAM8, work_appreciation);
        args.putString(ARG_PARAM9, str_approach);
        args.putString(ARG_PARAM10, str_style);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            str_original_text = getArguments().getString(ARG_PARAM1);
            str_translation_text = getArguments().getString(ARG_PARAM2);
            str_comment_text = getArguments().getString(ARG_PARAM3);
            str_author_text = getArguments().getString(ARG_PARAM4);
            image = getArguments().getString(ARG_PARAM5);
            str_work_intro = getArguments().getString(ARG_PARAM6);
            str_creative_bg = getArguments().getString(ARG_PARAM7);
            str_appreciation = getArguments().getString(ARG_PARAM8);
            str_approach = getArguments().getString(ARG_PARAM9);
            str_style = getArguments().getString(ARG_PARAM10);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_card1, container, false);

        // 初始化 TextView 和 ImageView 数组
        contentTextViews[0] = rootView.findViewById(R.id.original_text);
        contentTextViews[1] = rootView.findViewById(R.id.translation_text);
        contentTextViews[2] = rootView.findViewById(R.id.comment_text);
        contentTextViews[3] = rootView.findViewById(R.id.approach_text);
        contentTextViews[4] = rootView.findViewById(R.id.author_text);
        contentTextViews[5] = rootView.findViewById(R.id.work_intro_text);
        contentTextViews[6] = rootView.findViewById(R.id.creative_bg_text);
        contentTextViews[7] = rootView.findViewById(R.id.appreciation_text);
        imageView = rootView.findViewById(R.id.detailImageView);

        expandIcons[0] = rootView.findViewById(R.id.expand_icon0);
        expandIcons[1] = rootView.findViewById(R.id.expand_icon1);
        expandIcons[2] = rootView.findViewById(R.id.expand_icon2);
        expandIcons[3] = rootView.findViewById(R.id.expand_icon9);
        expandIcons[4] = rootView.findViewById(R.id.expand_icon3);
        expandIcons[5] = rootView.findViewById(R.id.expand_icon4);
        expandIcons[6] = rootView.findViewById(R.id.expand_icon5);
        expandIcons[7] = rootView.findViewById(R.id.expand_icon6);

        // 加载图片
        if (image != null && !image.isEmpty()) {
            int imageId = getResources().getIdentifier(image, "drawable", getContext().getPackageName());
            imageView.setImageResource(imageId);
        } else {
            // 保持默认图片
        }

        // 设置折叠的具体内容
        contentTextViews[0].setText(str_original_text);
        contentTextViews[1].setText(str_translation_text);
        contentTextViews[2].setText(str_comment_text);
        contentTextViews[3].setText(str_approach);;
        contentTextViews[4].setText(str_author_text);
        contentTextViews[5].setText(str_work_intro);
        contentTextViews[6].setText(str_creative_bg);
        contentTextViews[7].setText(str_appreciation);

        // 默认所有项都是折叠状态
        int item_numbers = 8;
        for (int i = 0; i < item_numbers; i++) {
            expandState.put(i, false);
        }

        // 点击事件，展开或折叠内容：原文
        LinearLayout original_linearlayout = rootView.findViewById(R.id.original_linearlayout);
        original_linearlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleExpandState(0);
            }
        });

        // 点击事件，展开或折叠内容：译文
        LinearLayout translation_linearlayout = rootView.findViewById(R.id.translation_linearlayout);
        translation_linearlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleExpandState(1);
            }
        });

        // 点击事件，展开或折叠内容：注释
        LinearLayout comment_linearlayout = rootView.findViewById(R.id.comment_linearlayout);
        comment_linearlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleExpandState(2);
            }
        });

        // 点击事件，展开或折叠内容：写作手法
        LinearLayout approach_linearlayout = rootView.findViewById(R.id.approach_linearlayout);
        approach_linearlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleExpandState(3);
            }
        });

        // 点击事件，展开或折叠内容：作者简介
        LinearLayout author_linearlayout = rootView.findViewById(R.id.author_linearlayout);
        author_linearlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleExpandState(4);
            }
        });

        // 点击事件，展开或折叠内容：作品简介
        LinearLayout work_intro_linearlayout = rootView.findViewById(R.id.work_intro_linearlayout);
        work_intro_linearlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleExpandState(5);
            }
        });

        // 点击事件，展开或折叠内容：创作背景
        LinearLayout creative_bg_linearlayout = rootView.findViewById(R.id.creative_bg_linearlayout);
        creative_bg_linearlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleExpandState(6);
            }
        });

        // 点击事件，展开或折叠内容：作品赏析
        LinearLayout appreciation_linearlayout = rootView.findViewById(R.id.appreciation_linearlayout);
        appreciation_linearlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleExpandState(7);
            }
        });

        return rootView;
    }

    private void toggleExpandState(int position) {
        // 切换展开状态
        boolean isExpanded = expandState.get(position);
        expandState.put(position, !isExpanded);
        updateExpandState(position);
    }

    private void updateExpandState(int position) {
        // 根据展开状态显示或隐藏内容
        final boolean isExpanded = expandState.get(position);
        contentTextViews[position].setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        // 更新图标状态
        expandIcons[position].setImageResource(isExpanded ? R.drawable.baseline_keyboard_arrow_down_24 : R.drawable.baseline_keyboard_arrow_right_24);
    }
}