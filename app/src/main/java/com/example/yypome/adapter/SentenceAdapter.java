package com.example.yypome.adapter;

import static com.example.yypome.myapi.MyApi.BEARER_TOKEN;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.yypome.R;
import com.example.yypome.data.ChatMessage;
import com.example.yypome.data.Sentence;
import com.example.yypome.data.TitleWithFileNames;
import com.example.yypome.db.FileRepository;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;

public class  SentenceAdapter extends RecyclerView.Adapter<SentenceAdapter.SentenceViewHolder> {
    private View rootView;

    private List<Sentence> sentenceList;
    private String title;
    private Context context;
    private String TAG = "SentenceAdapter";
    private boolean[] requestInProgress; // 添加请求状态数组
    private boolean[] statusClick; // 添加点击状态数组

    public SentenceAdapter(Context context, List<Sentence> sentenceList, String title) {
        this.title = title;
        this.context = context;
        this.sentenceList = sentenceList;
        this.requestInProgress = new boolean[sentenceList.size()]; // 初始化请求状态数组
        this.statusClick = new boolean[sentenceList.size()];
    }

    @Override
    public SentenceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        rootView = LayoutInflater.from(context).inflate(R.layout.item_sentence, parent, false);
        return new SentenceViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(SentenceViewHolder holder, int position) {
        Sentence sentence = sentenceList.get(position);
        holder.textViewSentence.setText(sentence.getText());

        // 生成唯一的文件名
        String fileName = "sentence_image_" + sentence.getText().hashCode() + ".png";

        // 获取 FileRepository 实例
        FileRepository fileRepository = new FileRepository(context);

        // 优先加载本地缓存的图片
        Bitmap cachedImage = loadImageFromInternalStorage(context, fileName);
        if (cachedImage != null) {
            Log.d(TAG, "onBindViewHolder: cacheImage 不为空" + sentence);
            holder.imageViewDescription.setImageBitmap(cachedImage); // 显示缓存图片
        } else {
            // 如果没有缓存，异步加载数据库中的 URL
            loadFromDatabaseOrGenerate(fileRepository, fileName, holder, sentence, position);
        }

        // 设置显示状态
        if (statusClick[position]) {
            holder.imageViewDescription.setVisibility(View.VISIBLE);
        } else {
            holder.imageViewDescription.setVisibility(View.GONE);
        }

        // 句子文本点击事件
        holder.textViewSentence.setOnClickListener(v -> {
            if (holder.imageViewDescription.getVisibility() == View.GONE && !statusClick[position]) {
                holder.imageViewDescription.setVisibility(View.VISIBLE);
                statusClick[position] = true;

                Bitmap updatedCachedImage = loadImageFromInternalStorage(context, fileName);
                if (updatedCachedImage != null) {
                    holder.imageViewDescription.setImageBitmap(updatedCachedImage);
                } else {
                    loadFromDatabaseOrGenerate(fileRepository, fileName, holder, sentence, position);
                }
            } else {
                holder.imageViewDescription.setVisibility(View.GONE);
                statusClick[position] = false;
            }
        });

        // 图片点击事件，重新生成并缓存
        holder.imageViewDescription.setOnClickListener(v -> {
            holder.imageViewDescription.setImageResource(R.drawable.placeholder_image3); // 加载占位图
            if (!requestInProgress[position]) {
                requestInProgress[position] = true;
                // 获取新图片并缓存在本地
                sendMessage(sentence, position, holder);
                Log.d(TAG, "onBindViewHolder: 覆盖成功:" + sentence);
            }
        });
    }

    /**
     * 尝试从数据库加载图片 URL，或在 URL 不存在时生成新的图片。
     */
    private void loadFromDatabaseOrGenerate(FileRepository fileRepository, String fileName, SentenceViewHolder holder, Sentence sentence, int position) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String imageUrl = fileRepository.getImageUrlByFileName(fileName);

            // 切换到主线程更新 UI
            new Handler(Looper.getMainLooper()).post(() -> {
                if (imageUrl != null) {
                    // 如果数据库中有该图片 URL，加载图片并缓存
                    Glide.with(context)
                            .load(imageUrl)
                            .placeholder(R.drawable.placeholder_image3)
                            .error(R.drawable.error_image2)
                            .into(holder.imageViewDescription);
                    cacheImage(context, imageUrl, fileName); // 缓存新图片
                } else if (!requestInProgress[position]) {
                    // 如果数据库中没有记录，生成新图片
                    requestInProgress[position] = true;
                    sendMessage(sentence, position, holder);
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return sentenceList.size();
    }

    // ViewHolder 内部类
    public static class SentenceViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSentence;
        ImageView imageViewDescription;

        public SentenceViewHolder(View itemView) {
            super(itemView);
            textViewSentence = itemView.findViewById(R.id.textViewSentence);
            imageViewDescription = itemView.findViewById(R.id.imageViewDescription);
        }
    }

    private static final String URL = "https://open.oppomobile.com/agentplatform/app_api/chat";
    private String myconversation_id;


    private void sendMessage(Sentence sentence, int position, SentenceViewHolder holder) {

        String prompt = "请调用文生图工具帮我生成一张图片，要求如下。理解并赏析《" + title + "》中的句子：“" + sentence.getText() + "”，生成一张与其意境一致的图片，用来辅助记忆。";

        if (!sentence.getText().isEmpty() && BEARER_TOKEN.trim().length() > "Bearer".length()) {

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("query", prompt);
            jsonObject.addProperty("response_mode", "streaming");
            jsonObject.addProperty("conversation_id", myconversation_id);
            jsonObject.addProperty("user", "8f3d226a-34c7-486c-b760-c8e8ada22ba4");

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    jsonObject.toString()
            );

            Request request = new Request.Builder()
                    .url(URL)
                    .post(body)
                    .addHeader("Authorization", BEARER_TOKEN)
                    .addHeader("Content-Type", "application/json")
                    .build();

            receiveMessage(request, sentence, position, holder);
        }
    }

    private void receiveMessage(Request request, Sentence sentence, int position, SentenceViewHolder holder) {
        OkHttpClient client = new OkHttpClient();

        // 初始化 currentAnswer，用于累积回复内容
        final StringBuilder currentAnswer = new StringBuilder();

        // 创建 EventSource 工厂
        EventSource.Factory factory = EventSources.createFactory(client);
        EventSource eventSource = factory.newEventSource(request, new EventSourceListener() {
            @Override
            public void onOpen(EventSource eventSource, Response response) {
                // 连接已打开
//                Log.d(TAG, "EventSource opened");
            }

            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                Log.d(TAG, "onEvent received data: " + data);

                // 过滤掉心跳消息，例如"ping"
                if ("ping".equalsIgnoreCase(data.trim())) {
//                    Log.d(TAG, "Received ping, ignoring.");
                    return; // 不处理心跳消息
                }

                // 处理接收到的数据
                processResponseLine(data, sentence, position, holder, currentAnswer);

                // 当图片URL生成后，缓存图片
                if (sentence.getImageResUrl() != null) {
                    String fileName = "sentence_image_" + sentence.getText().hashCode() + ".png";
                    cacheImage(context, sentence.getImageResUrl(), fileName);
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                // 连接已关闭，意味着接收完毕
                Log.d(TAG, "EventSource closed");

                if (sentence.getImageResUrl() != null) {
                    // 更新 UI（ImageView）
                    updateImageViewOnMainThread(holder, position, holder.imageViewDescription, sentence.getImageResUrl());

                    // 缓存图片
                    String fileName = "sentence_image_" + sentence.getText().hashCode() + ".png";
                    cacheImage(context, sentence.getImageResUrl(), fileName);

                    Log.d(TAG, "Extracted Image URL: " + sentence.getImageResUrl());
                }

                requestInProgress[position] = false; // 标记请求结束
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                // 处理错误
                Log.e(TAG, "Error: " + t.getMessage());
                requestInProgress[position] = false; // 标记请求结束
                // 在主线程显示错误图片
                updateImageViewOnMainThread(holder.imageViewDescription, R.drawable.error_image);
            }
        });
    }

    private void processResponseLine(String jsonString, Sentence sentence, int position, SentenceViewHolder holder, StringBuilder currentAnswer) {
        try {
            JsonObject jsonObject = new Gson().fromJson(jsonString, JsonObject.class);

            // 检查响应中是否包含 answer 字段
            if (jsonObject.has("answer")) {
                String answer = jsonObject.get("answer").getAsString();
                currentAnswer.append(answer);

                // 尝试提取图像URL（假设返回的文本中有图片链接）
                String potentialImageUrl = extractImageUrl(currentAnswer.toString());

                if (potentialImageUrl != null) {
                    // 更新 Sentence 对象
                    sentence.setImageResUrl(potentialImageUrl);

                    // 更新 UI（ImageView）
                    updateImageViewOnMainThread(holder, position, holder.imageViewDescription, potentialImageUrl);

//                    Log.d(TAG, "Extracted Image URL: " + potentialImageUrl);
                }
            }

            // 获取 conversation_id 并更新
            if (myconversation_id == null && jsonObject.has("conversation_id")) {
                myconversation_id = jsonObject.get("conversation_id").getAsString();
            }

        } catch (Exception e) {
            // 如果JSON解析失败，将响应视为纯文本
            currentAnswer.append(jsonString);
        }
    }


    private String extractImageUrl(String text) {
        // 去除文本中的换行符，使URL连续
        String textWithoutLineBreaks = text.replace("\n", "");

        // 正则表达式匹配图片URL
        String urlPattern = "(https?://[\\w\\-\\.\\~:/%\\?#\\[\\]@!\\$&'\\(\\)\\*\\+,;=]+\\.(png|jpg|jpeg|gif))";

        Pattern pattern = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(textWithoutLineBreaks);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    private void updateImageViewOnMainThread(SentenceViewHolder holder, int position, ImageView imageView, String imageUrl) {
        // 在主线程更新 ImageView
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> {
                // 检查是否是当前的position，以避免混淆
                if (holder.getAdapterPosition() == position) {
                    Glide.with(context)
                            .load(imageUrl)
                            .placeholder(R.drawable.placeholder_image3)
                            .error(R.drawable.error_image2)
                            .into(imageView);
                }
            });
        } else {
            Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.post(() -> {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_image3)
                        .error(R.drawable.error_image2)
                        .into(imageView);
            });
        }
    }

    private void updateImageViewOnMainThread(ImageView imageView, int resourceId) {
        // 在主线程更新 ImageView，使用资源ID
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> {
                imageView.setImageResource(resourceId);
            });
        } else {
            Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.post(() -> {
                imageView.setImageResource(resourceId);
            });
        }
    }

    // 下载并缓存图片
    private void cacheImage(Context context, String imageUrl, String fileName) {
        Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        // 图片下载成功后保存到本地
                        saveBitmapToInternalStorage(context, resource, fileName);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // 清除资源时的处理
                    }
                });
    }

    // 保存图片到本地
    private void saveBitmapToInternalStorage(Context context, Bitmap bitmap, String fileName) {
        try {
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 从本地读取缓存图片
    private Bitmap loadImageFromInternalStorage(Context context, String fileName) {
        try {
            File file = new File(context.getFilesDir(), fileName);
            if (file.exists()) {
                return BitmapFactory.decodeFile(file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



}
