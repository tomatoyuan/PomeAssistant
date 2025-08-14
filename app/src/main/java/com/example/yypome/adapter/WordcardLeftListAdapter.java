package com.example.yypome.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yypome.R;

import java.util.ArrayList;
import java.util.List;

public class WordcardLeftListAdapter extends RecyclerView.Adapter<WordcardLeftListAdapter.MyHolder> {
    private List<String> dataList = new ArrayList<>();

    private int currentIndex = 0;

    public WordcardLeftListAdapter(List<String> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.wordcard_left_list_item, null);
        return new MyHolder(inflate);
    }

    @Override
    @SuppressLint("RecyclerView")
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        // 绑定数据
        String name = dataList.get(position);
//        holder.tv_name.setText(name);

        // 分类的点击事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != leftListOnClickItemListener) {
                    leftListOnClickItemListener.onItemClick(position);
                }
            }
        });

        //
        // 根据位置设置不同的selector文件
        switch (position) {
            case 0:
                holder.imageView.setImageResource(R.drawable.wordcard_icon_selector0);
                break;
            case 1:
                holder.imageView.setImageResource(R.drawable.wordcard_icon_selector1);
                break;
            case 2:
                holder.imageView.setImageResource(R.drawable.wordcard_icon_selector2);
                break;
            case 3:
                holder.imageView.setImageResource(R.drawable.wordcard_icon_selector3);
                break;
        }

        // 设置选中状态
        holder.imageView.setSelected(position == currentIndex);

//        // 点击事件处理
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                currentIndex = position;  // 更新选中的位置
//                notifyDataSetChanged();  // 通知适配器更新UI
//            }
//        });

//        if (currentIndex == position) {
//            holder.itemView.setBackgroundResource(R.drawable.type_selector_bg);
//        } else {
//            holder.itemView.setBackgroundResource(R.drawable.type_selector_normal_bg);
//        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{
        TextView tv_name;
        ImageView imageView;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.itemIcon); // 图标
//            tv_name = itemView.findViewById(R.id.name);
        }
    }


    public void setLeftListOnClickItemListener(LeftListOnClickItemListener leftListOnClickItemListener) {
        this.leftListOnClickItemListener = leftListOnClickItemListener;
    }

    private LeftListOnClickItemListener leftListOnClickItemListener;

    public interface LeftListOnClickItemListener {
        void onItemClick(int position);
    }

    public void setCurrentIndex(int position) {
        this.currentIndex = position;
        // 这句话不能少
        notifyDataSetChanged();
    }


}
