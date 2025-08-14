package com.example.yypome.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yypome.R;
import com.example.yypome.data.MeListItem;

import java.util.List;

public class MeListAdapter extends RecyclerView.Adapter<MeListAdapter.ListViewHolder> {
    private Context context;
    private List<MeListItem> items;

    public MeListAdapter(Context context, List<MeListItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.me_item_list, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        MeListItem item = items.get(position);
        holder.itemIcon.setImageResource(item.getIconResId());
        holder.itemTitle.setText(item.getTitle());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {
        ImageView itemIcon;
        TextView itemTitle;
//        ImageView itemArrow;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            itemIcon = itemView.findViewById(R.id.item_icon);
            itemTitle = itemView.findViewById(R.id.item_title);
//            itemArrow = itemView.findViewById(R.id.item_arrow);
        }
    }

}
