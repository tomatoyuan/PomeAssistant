package com.example.yypome.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yypome.R;

import java.util.List;

public class PoemSentenceAdapter extends RecyclerView.Adapter<PoemSentenceAdapter.PoemViewHolder> {
    private List<String> poemList;
    private Context context;
    private OnItemClickListener listener;
    private String TAG = "PoemSentenceAdapter";

    // 定义点击事件接口
    public interface OnItemClickListener {
        void onItemClick(String sentence, String poemSentence);
    }

    public PoemSentenceAdapter(Context context, List<String> poemList, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.poemList = poemList;
        this.listener = onItemClickListener;
    }

    @NonNull
    @Override
    public PoemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_item_image3, parent, false);
        return new PoemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PoemViewHolder holder, int position) {
        String poemSentence = poemList.get(position);
        holder.poemSentence.setText(poemSentence);
        holder.poemSentence.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(listener != null){
//                    Toast.makeText(context, "点击了: " + poemSentence, Toast.LENGTH_SHORT).show(); // 添加 Toast
                    MyToastShow(R.drawable.toast_pic3, "点击了: " + poemSentence);
                    listener.onItemClick(poemSentence, "正在赏析\"" + poemSentence + "\"");
                }
            }

            private void MyToastShow(int iconResId, String message) {
                // 获取布局服务
                LayoutInflater inflater = LayoutInflater.from(context);
                // 使用自定义布局
                View layout = inflater.inflate(R.layout.custom_toast, null);

                // 设置图标和文本
                ImageView toastIcon = layout.findViewById(R.id.toast_icon);
                toastIcon.setImageResource(iconResId); // 设置你想要的图标

                TextView toastText = layout.findViewById(R.id.toast_message);
                toastText.setText(message);

                // 创建并显示自定义的Toast
                Toast toast = new Toast(context);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(layout);
                toast.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return poemList.size();
    }

    public static class PoemViewHolder extends RecyclerView.ViewHolder {
        TextView poemSentence;

        public PoemViewHolder(@NonNull View itemView) {
            super(itemView);
            poemSentence = itemView.findViewById(R.id.poem_sentence);
        }
    }
    // 可选：更新数据的方法
    public void updatePoems(List<String> newPoems){
        poemList.clear();
        poemList.addAll(newPoems);
        notifyDataSetChanged();
    }
}
