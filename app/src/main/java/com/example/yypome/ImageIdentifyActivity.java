package com.example.yypome;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yypome.imageIdentity.Albums;
import com.example.yypome.imageIdentity.Camera;

import java.util.ArrayList;
import java.util.List;

public class ImageIdentifyActivity extends AppCompatActivity {

    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;
    private Intent intent1,intent2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_identify);

        // 获取从Fragment传递的数据
        String extraData = getIntent().getStringExtra("poem_text");

        // 显示传递的数据
//        TextView textView = findViewById(R.id.data_text_view);
//        textView.setText(extraData);

        // 设置返回按钮
        Button returnButton = findViewById(R.id.return_button);
        returnButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
//            resultIntent.putExtra("result_key", "Data returned from Activity");
            setResult(RESULT_OK, resultIntent);
            finish();  // 结束Activity并返回结果
        });

        Button chooseFromAlbum = findViewById(R.id.choose_from_album);
        chooseFromAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestPermissions();
            }
        });
        Button takePhoto = findViewById(R.id.rtake_photo);
//        picture = findViewById(R.id.picture);

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 动态申请权限
                if (ContextCompat.checkSelfPermission(ImageIdentifyActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ImageIdentifyActivity.this, new String[]{Manifest.permission.CAMERA}, TAKE_PHOTO);
                } else {
                    // 启动相机程序
                    startCamera();
                }
            }
        });


        intent1=new Intent(this, Albums.class);//创建跳转到Albums显示的窗口的Intent
        intent1.putExtra("poem_text", extraData);  // 将 extra_data 传递给 Albums
        intent2=new Intent(this, Camera.class);//创建跳转到Camera显示的窗口的Intent
        intent2.putExtra("poem_text", extraData);  // 将 extra_data 传递给 Albums
    }

    private void checkAndRequestPermissions() {
        String[] permissions;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13
            permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }

        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(permission);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[0]), CHOOSE_PHOTO);
        } else {
            openAlbum();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CHOOSE_PHOTO) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                // 图像来源：直接拍摄或从相册选择
                // openGallery();
                openAlbum();
            } else {
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == TAKE_PHOTO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera(); // 启动相机
            } else {
                Toast.makeText(this, "Permission denied to use the camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openAlbum() {
        startActivity(intent1);//进入album的窗口界面
    }
    private void startCamera() {
        startActivity(intent2);//进入camera的窗口界面
    }
}
