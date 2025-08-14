package com.example.yypome;


import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.yypome.fragment.CheckFragment;
import com.example.yypome.fragment.MeFragment;
import com.example.yypome.fragment.ReviewFragment;
import com.example.yypome.fragment.StudyFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    // 定义 ImageView 引用
    private ImageView iconStudy, iconCheck, iconReview, iconMe;

    private StudyFragment studyFragment;
    private CheckFragment checkFragment;
    private ReviewFragment reviewFragment;
    private MeFragment meFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        // 获取导航项的 LinearLayout
        iconStudy = findViewById(R.id.icon_study);
        iconCheck = findViewById(R.id.icon_check);
        iconReview = findViewById(R.id.icon_review);
        iconMe = findViewById(R.id.icon_me);

        // 设置点击监听器
        iconStudy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSelection(iconStudy);
                selectedFragment(0);
            }
        });
        iconCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSelection(iconCheck);
                selectedFragment(1);
            }
        });
        iconReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSelection(iconReview);
                selectedFragment(2);
            }
        });
        iconMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSelection(iconMe);
                selectedFragment(3);
            }
        });


        // 默认选中“学习”选项
        toggleSelection(iconStudy);
        // 默认首页选中
        selectedFragment(0);

    }

    private void toggleSelection(ImageView selectedIcon) {
        // 重置所有图标的选中状态
        iconStudy.setSelected(false);
        iconCheck.setSelected(false);
        iconReview.setSelected(false);
        iconMe.setSelected(false);

        // 设置当前点击的图标为选中状态
        selectedIcon.setSelected(true);
    }

    private void selectedFragment(int position) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        hideFragment(fragmentTransaction);

        if (position == 0) {
            if (studyFragment == null) {
                studyFragment = new StudyFragment();
                fragmentTransaction.add(R.id.content, studyFragment);
            } else {
                fragmentTransaction.show(studyFragment);
            }
        } else if (position == 1) {
            if (checkFragment == null) {
                checkFragment = new CheckFragment();
                fragmentTransaction.add(R.id.content, checkFragment);
            } else {
                fragmentTransaction.show(checkFragment);
            }
        } else if(position == 2) {
            if (reviewFragment == null) {
                reviewFragment = new ReviewFragment();
                fragmentTransaction.add(R.id.content, reviewFragment);
            } else {
                fragmentTransaction.show(reviewFragment);
            }
        } else {
            if (meFragment == null) {
                meFragment = new MeFragment();
                fragmentTransaction.add(R.id.content, meFragment);
            } else {
                fragmentTransaction.show(meFragment);
            }
        }

        // 一定要提交
        fragmentTransaction.commit();

    }

    private void hideFragment(FragmentTransaction fragmentTransaction) {
        if (studyFragment != null) {
            fragmentTransaction.hide(studyFragment);
        }

        if (checkFragment != null) {
            fragmentTransaction.hide(checkFragment);
        }

        if (reviewFragment != null) {
            fragmentTransaction.hide(reviewFragment);
        }

        if (meFragment != null) {
            fragmentTransaction.hide(meFragment);
        }
    }

}