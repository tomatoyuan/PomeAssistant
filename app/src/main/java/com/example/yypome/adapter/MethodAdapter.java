package com.example.yypome.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yypome.R;
import com.example.yypome.db.PoemRepository;

import java.util.List;
import java.util.Map;

public class MethodAdapter extends RecyclerView.Adapter<MethodAdapter.MethodViewHolder> {

    private Context context;
    private List<String> methods;
    private String title;
    Map<String, String> suggestMethodMap;
    private String suggestionMethod = "联想记忆法";  // 初始默认值
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String methodName);
    }

    public MethodAdapter(Context context, List<String> methods, String title, Map<String, String> suggestMethodMap, OnItemClickListener listener) {
        this.context = context;
        this.methods = methods;
        this.title = title;
        this.suggestMethodMap = suggestMethodMap;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MethodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card4_method_list, parent, false);
        return new MethodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MethodViewHolder holder, int position) {
        String suggestionMethod = suggestMethodMap.get(title);

        if (suggestionMethod != null && methods.get(position).equals(suggestionMethod)) {
            holder.methodName.setText(suggestionMethod + "⭐(推荐)");
        } else {
            holder.methodName.setText(methods.get(position));
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(methods.get(position)));
    }

    @Override
    public int getItemCount() {
        return methods.size();
    }

    static class MethodViewHolder extends RecyclerView.ViewHolder {
        TextView methodName;

        MethodViewHolder(View itemView) {
            super(itemView);
            methodName = itemView.findViewById(R.id.method_name);
        }
    }

}
