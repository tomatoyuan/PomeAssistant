package com.example.yypome.data;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JSONReader {

    private static final String TAG = "JSONReader";

    public List<niceSentencesData> loadPoemData(Context context) {
        List<niceSentencesData> poemDataList = new ArrayList<>();
        Gson gson = new Gson();

        try {
            // 打开 assets 文件夹中的 JSON 文件
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("niceSentence.json");

            // 将文件内容读取为字符串
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            // 定义解析类型
            Type listType = new TypeToken<List<niceSentencesData>>() {}.getType();

            // 使用 Gson 解析 JSON 数据
            poemDataList = gson.fromJson(jsonBuilder.toString(), listType);

        } catch (IOException e) {
            Log.e(TAG, "Error reading JSON file", e);
        }

        return poemDataList;
    }
}
