package com.example.yypome.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.yypome.R;
import com.example.yypome.data.CardData;
import com.example.yypome.db.StatisticsRepository;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_CARD = 0;  // 普通卡片类型
    private static final int VIEW_TYPE_ADD_CARD = 1;  // 添加卡片类型

    private List<CardData> cardDataList;
    private Context context;
    private OnItemClickListener onItemClickListener;
    private AddCardOnItemClickListener addCardOnItemClickListener;
    private OnItemClickListener speechOnItemClickListener;
    private OnItemClickListener imageCheckOnItemClickListener;
    private OnItemClickListener qaOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(CardData data);
    }

    public interface AddCardOnItemClickListener {
        void onItemClick();
    }

    public CardAdapter(List<CardData> cardDataList, Context context, OnItemClickListener onItemClickListener, OnItemClickListener speechOnItemClickListener, OnItemClickListener imageCheckOnItemClickListener, OnItemClickListener qaOnItemClickListener, AddCardOnItemClickListener addCardOnItemClickListener) {
        this.cardDataList = cardDataList;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
        this.speechOnItemClickListener = speechOnItemClickListener;
        this.imageCheckOnItemClickListener = imageCheckOnItemClickListener;
        this.qaOnItemClickListener = qaOnItemClickListener;
        this.addCardOnItemClickListener = addCardOnItemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        // 如果是最后一项，则显示“添加习题”按钮卡片
        if (position == cardDataList.size()) {
            return VIEW_TYPE_ADD_CARD;
        } else {
            return VIEW_TYPE_CARD;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_CARD) {
            View view = LayoutInflater.from(context).inflate(R.layout.card_layout, parent, false);
            return new CardViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.card_add_layout, parent, false);
            return new AddCardViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_CARD) {
            CardViewHolder cardHolder = (CardViewHolder) holder;
            CardData data = cardDataList.get(position);
            cardHolder.titleTextView.setText(data.getTitle());

            // 设置卡片点击事件
            cardHolder.button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(data);
                }
            });

            cardHolder.button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    speechOnItemClickListener.onItemClick(data);
                }
            });

            cardHolder.button3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageCheckOnItemClickListener.onItemClick(data);
                }
            });

            cardHolder.button4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    qaOnItemClickListener.onItemClick(data);
                }
            });


        } else {
            // “添加习题”按钮不需要特殊绑定数据
            AddCardViewHolder addCardHolder = (AddCardViewHolder) holder;

            // 设置卡片点击事件
            addCardHolder.addCardLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addCardOnItemClickListener.onItemClick();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        // cardDataList 的数量加上一个“添加习题”卡片
        return cardDataList.size() + 1;
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        MaterialButton button1;
        MaterialButton button2;
        MaterialButton button3;
        MaterialButton button4;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.card_title);
            button1 = itemView.findViewById(R.id.btn_dictate);
            button2 = itemView.findViewById(R.id.btn_recite);
            button3 = itemView.findViewById(R.id.btn_identify);
            button4 = itemView.findViewById(R.id.btn_qa);
        }
    }

    public static class AddCardViewHolder extends RecyclerView.ViewHolder {

        View addCardLayout;

        public AddCardViewHolder(@NonNull View itemView) {
            super(itemView);
            // 设置“添加习题”按钮的点击事件等

            addCardLayout = itemView;
        }
    }

    // 删除卡片数据
    public void removeItem(int position) {
        if (position == cardDataList.size()) {

        } else {
            StatisticsRepository statisticsRepository = new StatisticsRepository(context);
            statisticsRepository.clearCheckCardFlag(cardDataList.get(position).getTitle());
            cardDataList.remove(position);
            notifyDataSetChanged();
        }
    }

    // 动态添加卡片
    public void addCard(CardData data) {
        if (containsTitle(cardDataList, data.getTitle()) != null) {
            StatisticsRepository statisticsRepository = new StatisticsRepository(context);
            statisticsRepository.updateCheckCardFlag(data.getTitle());
            cardDataList.add(data);
            notifyDataSetChanged();
        }
    }

    public CardData containsTitle(List<CardData> visibleCardList, String targetTitle) {
        for (CardData card : visibleCardList) {
            if (card.getTitle().equals(targetTitle)) {
                return card; // 找到匹配的 title
            }
        }
        return null; // 没有找到匹配的 title
    }
}
