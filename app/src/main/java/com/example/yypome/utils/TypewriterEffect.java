package com.example.yypome.utils;

import android.os.Handler;
import android.widget.TextView;

public class TypewriterEffect {
    private TextView textView;
    private String fullText;
    private int index = 0;
    private long delay = 10; // 每个字符出现的间隔（毫秒）
    private Handler handler = new Handler();

    public TypewriterEffect(TextView textView, String fullText) {
        this.textView = textView;
        this.fullText = fullText;
    }

    public void start() {
        index = 0; // 重置索引
        textView.setText(""); // 清空 TextView
        handler.removeCallbacks(characterAdder);
        handler.postDelayed(characterAdder, delay);
    }

    // 控制文字逐个显示的 Runnable
    private Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            if (index < fullText.length()) {
                // 每次添加一个字符
                textView.append(String.valueOf(fullText.charAt(index)));
                index++;
                // 延迟显示下一个字符
                handler.postDelayed(this, delay);
            }
        }
    };
}

