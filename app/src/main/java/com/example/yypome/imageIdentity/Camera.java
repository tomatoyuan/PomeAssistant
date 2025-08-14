package com.example.yypome.imageIdentity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.yypome.ImageIdentifyActivity;
import com.example.yypome.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Camera extends Activity {
    private ImageView cameraPicture;
    public static final int TAKE_PHOTO = 1;
    private Button pictureSave=null;
    private Uri imageUri;
    private String uriden;
    private String token;
    private String pathiden;
    private String resultden;

    private String poemOriginText;

    private String TAG = "Camera";


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);

        // 让状态栏透明
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);  // 设置透明状态栏

        // 获取从ImageIdentifyActivity传递的数据
        poemOriginText = getIntent().getStringExtra("poem_text");

        pictureSave=findViewById(R.id.pictureSave);
        cameraPicture = findViewById(R.id.picture);

        // 创建一个File对象，用于保存摄像头拍下的图片，这里把图片命名为output_image.jpg
        // 并将它存放在手机SD卡的应用关联缓存目录下
        File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
        // 对照片的更换设置
        try {
            // 如果上一次的照片存在，就删除
            if (outputImage.exists()) {
                outputImage.delete();
            }
            // 创建一个新的文件
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 如果Android版本大于等于7.0
        if (Build.VERSION.SDK_INT >= 24) {
            // 将File对象转换成一个封装过的Uri对象
            imageUri = FileProvider.getUriForFile(this, "com.example.yypome.fileProvider", outputImage);
        } else {
            // 将File对象转换为Uri对象，这个Uri标识着output_image.jpg这张图片的本地真实路径
            imageUri = Uri.fromFile(outputImage);
        }
        uriden = imageUri.toString(); // 设置 uriden
        // 动态申请权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.CAMERA}, TAKE_PHOTO);
        } else {
            // 启动相机程序
            startCamera();
        }


        pictureSave.setOnClickListener(new pictureSaveFunction());

        Button button1=findViewById(R.id.pictureback);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ImageIdentifyActivity.class);
                intent.putExtra("poem_text", poemOriginText);
//                startActivity(intent);
                finish();
            }
        });

        /*文字识别*/
        Button button2=findViewById(R.id.pictureIdentity);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("uriden:" + uriden);
                new Thread(){
                    @Override
                    public void run() {
                        resultden = accurateBasic(uriden);
                        if (Build.VERSION.SDK_INT >= 23) {
                            int REQUEST_CODE_CONTACT = 101;
                            String[] permissions = {
                                    Manifest.permission.READ_MEDIA_IMAGES};
                            //验证是否许可权限
                            for (String str : permissions) {
                                if (Camera.this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                                    //申请权限
                                    Camera.this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                                    return;
                                } else {
                                    resultden = accurateBasic(uriden);
                                    BitmapDrawable bmpDrawable = (BitmapDrawable) cameraPicture.getDrawable();
                                    Bitmap bitmap = bmpDrawable.getBitmap();
                                    try {
                                        annotateImageWithText(bitmap, resultden);
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
//                                    Toast.makeText(Camera.this,"检查完成成功！",Toast.LENGTH_SHORT).show();
                                }
                            }

                        }
                    }
                }.start();
//                System.out.println("resultden:"+resultden);
            }
        });



    }

    private void startCamera() {
        Intent intent4 = new Intent("android.media.action.IMAGE_CAPTURE");
        // 指定图片的输出地址为imageUri
        intent4.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent4, TAKE_PHOTO);
    }


    private class pictureSaveFunction implements View.OnClickListener {
        public void onClick(View view){
            BitmapDrawable bmpDrawable = (BitmapDrawable) cameraPicture.getDrawable();
            Bitmap bitmap = bmpDrawable.getBitmap();
            saveToSystemGallery(bitmap);//将图片保存到本地
            Toast.makeText(getApplicationContext(),"图片保存成功！",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (requestCode == TAKE_PHOTO && resultCode == RESULT_OK ) {
                    try {
                        uriden = imageUri.toString(); // 更新 uriden
                        // 将图片解析成Bitmap对象
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        cameraPicture.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }

    public void saveToSystemGallery(Bitmap bmp) {
        ContentResolver contentResolver = getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis() + ".jpg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/cinema");

        Uri imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        if (imageUri != null) {
            try {
                OutputStream fos = contentResolver.openOutputStream(imageUri);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
                Toast.makeText(getApplicationContext(), "图片保存成功！", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(), "图片保存失败！", Toast.LENGTH_SHORT).show();
        }
    }


    /*识别方法*/
    public  String accurateBasic(String uripath) {
        // 请求url
        AccessToken accessToken1 = new AccessToken();
        token = accessToken1.getAuth();  // 获取AccessToken

        String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/accurate";
//        String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic?access_token=24.ffe186c85ecf67715a7a0755e63fa0d7.2592000.1730079033.282335-115724685"
        try {
            // 本地文件路径
            String filePath = uripath;
            byte[] imgData = FileUtil.readFileByBytes(this, filePath);
            String imgStr = Base64Util.encode(imgData);
            String imgParam = URLEncoder.encode(imgStr, "UTF-8");

            String param = "image=" + imgParam;
            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = token;
            String result = HttpUtil.post(url, accessToken, param);
            System.out.println(result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
        cameraPicture.setImageBitmap(annotatedImage);
    }

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

                // 绘制当前字符
                canvas.drawText(String.valueOf(currentChar), location.left + i * paint.getTextSize(),
                        location.top + location.height(), paint);
            }
        }

        return mutableBitmap;  // 返回绘制好文字的Bitmap
    }
}


