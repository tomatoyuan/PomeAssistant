package com.example.yypome.adapter;

import android.content.Context;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yypome.R;
import com.example.yypome.data.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.noties.markwon.Markwon;
import retrofit2.http.Tag;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<ChatMessage> chatMessages;
    private Context context;
    private static final String TAG = "ChatAdapter";

    public ChatAdapter(ArrayList<ChatMessage> chatMessages, Context context) {
        this.chatMessages = chatMessages;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage chatMessage = chatMessages.get(position);
        if (chatMessage.isSent()) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_RECEIVED) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        } else {
            throw new IllegalArgumentException("Unknown view type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessages.get(position);
        Log.d(TAG, "Binding message at position: " + position + " with text: " + chatMessage.getText());
        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).bind(chatMessage);
        } else if (holder.getItemViewType() == VIEW_TYPE_RECEIVED) {
            // 重置 TextView 和 ImageView 的状态
            if (holder instanceof ReceivedMessageViewHolder) {
                ((ReceivedMessageViewHolder) holder).messageText.setText("");  // 重置为默认值
                ((ReceivedMessageViewHolder) holder).recvImage.setVisibility(View.GONE);  // 隐藏图像
            }

            // 继续绑定数据
            ((ReceivedMessageViewHolder) holder).bind(chatMessage);

        } else {

        }

        // 强制刷新 ViewHolder 中的内容
        holder.itemView.requestLayout(); // 确保布局重新测量并渲染
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;
        ImageView avatarImage;

        SentMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_view_message);
            timeText = itemView.findViewById(R.id.text_view_time);
            avatarImage = itemView.findViewById(R.id.image_view_avatar);
        }

        void bind(ChatMessage message) {
            messageText.setText(message.getText());
            timeText.setText(message.getFormattedTime());
            // 设置头像等
        }
    }

    public static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        androidx.appcompat.widget.AppCompatTextView messageText;
        TextView timeText;
        ImageView avatarImage;
        ImageView recvImage;

        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.recv_text_view_message);
            timeText = itemView.findViewById(R.id.recv_text_view_time);
            avatarImage = itemView.findViewById(R.id.recv_image_view_avatar);
            recvImage = itemView.findViewById(R.id.received_image_view);
        }

        // 渲染 Markdown 文本
        public void renderMarkdown(String markdownText) {
            Markwon markwon = Markwon.create(itemView.getContext());
            markwon.setMarkdown(messageText, markdownText);
        }

        void bind(ChatMessage message) {
            // 更新文本消息
            if (message.getText() != null) {
                messageText.setText(message.getText());  // 确保正确设置文本内容
                Log.d(TAG, "Setting text: " + message.getText());

//                // markdown渲染
//                // 创建 Markwon 实例
//                Markwon markwon = Markwon.create(itemView.getContext());
//                // 使用 Markwon 渲染 Markdown 到 TextView
//                markwon.setMarkdown(messageText, message.getText());

            } else {
                messageText.setText("Received message hhh");  // 如果文本为空，防止显示默认值
            }
            timeText.setText(message.getFormattedTime());

            // 如果有图像URL，使用Glide加载图像
            if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
                Log.d(TAG, "Loading image: " + message.getImageUrl());

                recvImage.setVisibility(View.VISIBLE);  // 显示图像
                Glide.with(itemView.getContext())
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder_image)  // 加载中显示的占位图
                        .error(R.drawable.error_image)              // 加载失败时显示的图片
                        .into(recvImage);
            } else {
                recvImage.setVisibility(View.GONE);  // 隐藏图像
            }
        }
    }

}
