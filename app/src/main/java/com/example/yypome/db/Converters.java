package com.example.yypome.db;

import androidx.room.TypeConverter;

import com.example.yypome.data.Question;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Converters {

    @TypeConverter
    public static String fromQuestionsList(List<Question> questions) {
        if (questions == null || questions.isEmpty()) {  // 确保空列表或 null 都处理为 null
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<Question>>() {}.getType();
        return gson.toJson(questions, type);
    }

    @TypeConverter
    public static List<Question> toQuestionsList(String questionsString) {
        if (questionsString == null || questionsString.isEmpty()) {  // 确保 null 或空字符串转换为 null 或空列表
            return new ArrayList<>();  // 返回空列表
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<Question>>() {}.getType();
        return gson.fromJson(questionsString, type);
    }

    // 将 List<String> 转换为 JSON 字符串存储到数据库
    @TypeConverter
    public static String fromStringList(List<String> list) {
        if (list == null) {
            return null;
        }
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    // 将数据库中的 JSON 字符串转换为 List<String>
    @TypeConverter
    public static List<String> toStringList(String listString) {
        if (listString == null) {
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(listString, type);
    }
}
