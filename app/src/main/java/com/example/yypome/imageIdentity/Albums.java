package com.example.yypome.imageIdentity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.yypome.ImageIdentifyActivity;
import com.example.yypome.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Albums extends Activity {
    private static final String TAG = "Albums";
    private  ImageView albumsPicture;
    public static final int CHOOSE_PHOTO = 2;
    private Button button1,button2;
    private String token;
    private String pathiden;
    private String resultden;

    private String poemOriginText;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
//        resultden =
        }
    };
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.albums);

        // 让状态栏透明
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);  // 设置透明状态栏

        // 获取从ImageIdentifyActivity传递的数据
        poemOriginText = getIntent().getStringExtra("poem_text");

        albumsPicture = super.findViewById(R.id.picture);

        String permissions;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13
            permissions = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permissions = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        if (ContextCompat.checkSelfPermission(this, permissions) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permissions}, CHOOSE_PHOTO);
        } else {
            openAlbum();
        }

        /*文字识别部分*/
        button1 = findViewById(R.id.pictureIdentity2);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    @Override
                    public void run() {
//                        resultden = accurateBasic(Uri.parse(pathiden));
                        resultden = accurateBasic(Uri.fromFile(new File(pathiden)));
                        Bitmap bitmap=BitmapFactory.decodeFile(pathiden);
                        try {
                            annotateImageWithText(bitmap, resultden);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }.start();

            }
        });

        button2=findViewById(R.id.pictureback2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(v.getContext(), ImageIdentifyActivity.class);
                intent.putExtra("poem_text", poemOriginText);
//                startActivity(intent);
                finish();
            }
        });

    }

    public void annotateImageWithText(Bitmap originalImage, String ocrResponseJson) throws JSONException {
        // 解析OCR结果
        JSONObject jsonResponse = new JSONObject(ocrResponseJson);
        JSONArray wordsArray = jsonResponse.getJSONArray("words_result");

        List<OcrResult> ocrResults = new ArrayList<>();

        for (int i = 0; i < wordsArray.length(); i++) {
            JSONObject wordObj = wordsArray.getJSONObject(i);
            String words = wordObj.getString("words");

            JSONObject locationObj = wordObj.getJSONObject("location");
            int left = locationObj.getInt("left");
            int top = locationObj.getInt("top");
            int width = locationObj.getInt("width");
            int height = locationObj.getInt("height");

            Rect location = new Rect(left, top, left + width, top + height);
            OcrResult result = new OcrResult(words, location);
            ocrResults.add(result);
        }

        // 在图片上绘制文字
        Bitmap annotatedImage = drawTextOnImage(originalImage, ocrResults, poemOriginText);

        // 将带有标注的图片显示在ImageView中
        albumsPicture.setImageBitmap(annotatedImage);
    }

//    public Bitmap drawTextOnImage(Bitmap originalImage, List<OcrResult> ocrResults) {
//        // 创建一个可编辑的 Bitmap，原始图像的副本
//        Bitmap mutableBitmap = originalImage.copy(Bitmap.Config.ARGB_8888, true);
//        Canvas canvas = new Canvas(mutableBitmap);
//
//        // 设置绘制文本的 Paintresult
//        Paint paint = new Paint();
//        paint.setColor(Color.RED);  // 文字颜色
//        paint.setTextSize(150);      // 文字大小
//        paint.setStyle(Paint.Style.FILL);
//
//        for (OcrResult result : ocrResults) {
//            // 取得OCR识别结果中的位置和文字
//            String text = result.getWords();
//            Rect location = result.getLocation();
//
//            // 在相应的位置绘制文本
//            canvas.drawText(text, location.left, location.top + location.height(), paint);
//        }
//
//        return mutableBitmap;  // 返回绘制好文字的Bitmap
//    }

    // 判断是否是标点符号
    private boolean isPunctuation(char c) {
        // 使用正则表达式判断是否为标点符号、空格、或换行符
        return Pattern.compile("[\\p{Punct}\\s]").matcher(String.valueOf(c)).find();
    }

    public Bitmap drawTextOnImage(Bitmap originalImage, List<OcrResult> ocrResults, String poemOriginText) {
        // 创建一个可编辑的 Bitmap，原始图像的副本
        Bitmap mutableBitmap = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);

        // 设置绘制文本的 Paint
        Paint paint = new Paint();
        paint.setTextSize(150);      // 文字大小
        paint.setStyle(Paint.Style.FILL);

        // 去除标点符号、空格和回车符的 poemOriginText 用于比较
        String textForComparison = poemOriginText.replaceAll("[\\p{Punct}\\s]", "");

        int comparisonIndex = 0;  // 用于追踪对比的位置

//        Log.d(TAG, "drawTextOnImage: poem:" + poemOriginText);

        for (OcrResult result : ocrResults) {
            // 取得OCR识别结果中的位置和文字
            String ocrText = result.getWords();
            Rect location = result.getLocation();
//            Log.d(TAG, "drawTextOnImage: ocrText:" + ocrText);
//            Log.d(TAG, "drawTextOnImage: textForComparison:" + textForComparison);
            // 遍历 OCR 识别到的文字
            for (int i = 0; i < ocrText.length(); i++) {
                char currentChar = ocrText.charAt(i);

                // 检查字符是否为标点符号
                if (isPunctuation(currentChar)) {
                    // 标点符号，绘制为蓝色
                    paint.setColor(Color.BLUE);
                } else {
                    // 检查字符是否为标点符号
                    if (isPunctuation(currentChar)) {
                        // 标点符号，绘制为蓝色
                        paint.setColor(Color.BLUE);
                    } else {
                        // 确保 comparisonIndex 不超出 textForComparison 的长度
                        if (comparisonIndex < textForComparison.length()) {
                            // 非标点符号，与原始文本进行比较
                            char comparisonChar = textForComparison.charAt(comparisonIndex);

                            if (currentChar == comparisonChar) {
                                // 匹配成功，绘制为蓝色
                                paint.setColor(Color.BLUE);
                            } else {
                                // 匹配失败，绘制为红色
                                paint.setColor(Color.RED);
                            }

                            comparisonIndex++;  // 继续比较下一个字符
                        } else {
                            // 如果比较字符串已结束，直接绘制红色
                            paint.setColor(Color.RED);
                        }
                    }
                }

                // 绘制当前字符
                canvas.drawText(String.valueOf(currentChar), location.left + i * paint.getTextSize(),
                        location.top + location.height(), paint);
            }
        }

        return mutableBitmap;  // 返回绘制好文字的Bitmap
    }

    private void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    // 使用startActivityForResult()方法开启Intent的回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_PHOTO && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
//            Log.d("Albums", "Selected image URI: " + uri);
            if (Build.VERSION.SDK_INT >= 19) {
//                Log.d(TAG, "onActivityResult: Step 5");
                handleImageOnKitkat(data);
            } else {
                handleImageBeforeKitKat(data);
            }
        }
    }

    @TargetApi(19)
    private void handleImageOnKitkat(Intent data) {
        Uri uri = data.getData();
        String imagePath = null;
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            imagePath = uri.getPath();
        }

        if (imagePath != null) {
            displayImage(imagePath);
        }
        pathiden = imagePath;
    }



    private void handleImageBeforeKitKat(Intent data){
        Uri uri=data.getData();
        String imagePath = getImagePath(uri,null);
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                path = cursor.getString(index);
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath){
        if(imagePath!=null){
            Bitmap bitmap=BitmapFactory.decodeFile(imagePath);
            albumsPicture.setImageBitmap(bitmap);//将图片放置在控件上
        }else {
            Toast.makeText(this,"得到图片失败",Toast.LENGTH_SHORT).show();
        }
    }


    /*识别方法*/
    public String accurateBasic(Uri uri) {
        AccessToken accessToken1 = new AccessToken();
        token = accessToken1.getAuth();

        String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/accurate";
        try {
            byte[] imgData = readImageFromUri(uri);
            String imgStr = Base64Util.encode(imgData);
            String imgParam = URLEncoder.encode(imgStr, "UTF-8");

            String param = "image=" + imgParam;

            String accessToken = token;
            String result = HttpUtil.post(url, accessToken, param);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] readImageFromUri(Uri uri) throws IOException {
        ContentResolver contentResolver = getContentResolver();
        InputStream inputStream = null;

        if ("content".equals(uri.getScheme())) {
            inputStream = contentResolver.openInputStream(uri);  // 处理 content:// 类型的Uri
        } else if ("file".equals(uri.getScheme())) {
            inputStream = new FileInputStream(new File(uri.getPath()));  // 处理 file:// 类型的Uri
        }

        if (inputStream == null) {
            return null;
        }

        return readBytesFromStream(inputStream);
    }

    private byte[] readBytesFromStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int read;
        byte[] buffer = new byte[1024];

        while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
            byteBuffer.write(buffer, 0, read);
        }
        return byteBuffer.toByteArray();
    }


}

