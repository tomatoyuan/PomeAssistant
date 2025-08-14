package com.example.yypome.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yypome.R;
import com.example.yypome.WordcardActivity;
import com.example.yypome.data.CardData;
import com.example.yypome.data.DailyErrorCount;
import com.example.yypome.db.PoemRepository;
import com.example.yypome.db.StatisticsRepository;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    Context context;
    List<CardData> cardDataList;
    private String TAG = "MyAdapter";

    public MyAdapter(Context context, List<CardData> cardDataList) {
        this.context = context;
        this.cardDataList = cardDataList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 加载 item 布局
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false);
        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(MyAdapter.MyViewHolder holder, int position) {
        CardData cardData = cardDataList.get(position);
        holder.titleTextView.setText(cardData.getTitle());
        holder.authorTextView.setText(cardData.getAuthor());
        holder.gradeTextView.setText("来自" + "📖" +cardData.getGrade());

        // 加载图片资源
        if (cardData.getImage() != null && !cardData.getImage().isEmpty()) {
//            try {
//                // 获取 AssetManager
//                AssetManager assetManager = context.getAssets();
//                // 打开 assets 中的图片文件输入流
//                InputStream inputStream = assetManager.open("images/" + position + ".png");
//                // 将输入流转换为 Bitmap
//                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//                // 设置图片到 ImageView
//                holder.imageView.setImageBitmap(bitmap);
//                // 关闭输入流
//                inputStream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//                // 处理异常（如图片不存在）
//                holder.imageView.setImageResource(R.drawable.card_bg_blank); // 可设置一张默认图
//            }
//            int imageId = context.getResources().getIdentifier(cardData.getImage(), "assets", context.getPackageName());
//            holder.imageView.setImageResource(imageId);
            /* 如果在assets/hash2url.json中正确设置了图床，可以使用下面的url加载方案，更加轻便 */
            String imageUrl = cardData.getImage();
            Log.d(TAG, "onBindViewHolder: imageurl:" + cardData.getTitle() + imageUrl);
            Glide.with(context)
                    .load(imageUrl)                // 加载网络图片URL
                    .placeholder(R.drawable.card_bg_blank)  // 占位图
                    .error(R.drawable.card_bg_blank)       // 错误时显示的图片
                    .into(holder.imageView);       // 显示在目标 ImageView 中

            // 设置学习状态
            if (cardData.getCompletionDate() != null && !cardData.getCompletionDate().isEmpty()) {
                holder.imageLearnStatus.setImageResource(R.drawable.ic_learned);
                holder.textLearnStatus.setText("已学习");
            } else {
                holder.imageLearnStatus.setImageResource(R.drawable.ic_nolearn);
                holder.textLearnStatus.setText("未学习");
            }
        } else {
            Log.d(TAG, "onBindViewHolder: imageurl:" + cardData.getTitle() + "没有imageUrl");
            holder.imageView.setImageResource(R.drawable.card_bg_blank);
        }

        // 设置点击事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 更新学习日期
                PoemRepository poemRepository = new PoemRepository(context);
                poemRepository.updateCompletionDateByTitle(cardData.getTitle());

                // 获取当前日期
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
                String currentDate = sdf.format(new Date());

                // 更新学习次数
                poemRepository.incrementPoetryCount(currentDate, cardData.getTitle());

                // 增加 recitationCount
                poemRepository.incrementRecitationCount(cardData.getTitle());

                StatisticsRepository statisticsRepository = new StatisticsRepository(context);
                statisticsRepository.incrementVisitCount(cardData.getTitle());

                // 创建 Intent，跳转到 DetailActivity
                Intent intent = new Intent(context, WordcardActivity.class);

                // 传递必要的数据
                intent.putExtra("title", cardData.getTitle());
                intent.putExtra("author", cardData.getAuthor());
                intent.putExtra("image", cardData.getImage());

                // 如果在 Adapter 中使用 context，需要添加以下标志
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                Log.d(TAG, "onClick: Successful!");
                
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cardDataList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView, imageLearnStatus;
        TextView titleTextView, authorTextView, gradeTextView, textLearnStatus;


        public MyViewHolder(View itemView) {
            super(itemView);
            // 初始化视图
            imageView = itemView.findViewById(R.id.imageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            gradeTextView = itemView.findViewById(R.id.gradeTextView);
            imageLearnStatus = itemView.findViewById(R.id.img_learn_status);
            textLearnStatus = itemView.findViewById(R.id.text_learn_status);
        }
    }

}
