package com.example.yypome.db;

import android.content.Context;
import android.util.Log;

import com.example.yypome.data.FileNameWithImageUrl;
import com.example.yypome.data.Title;
import com.example.yypome.data.TitleWithFileNames;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FileRepository {

    private final FileDao fileDao;
    private final TitleDao titleDao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public FileRepository(Context context) {
        FileDatabase db = FileDatabase.getDatabase(context);
        fileDao = db.fileDao();
        titleDao = db.titleDao();
    }

    // 插入 title 和文件信息
    public void insertTitleAndFiles(String title, List<FileNameWithImageUrl> files) {
        executor.execute(() -> {
            Title titleEntity = new Title(title);
            long titleId = titleDao.insertTitle(titleEntity);
            for (FileNameWithImageUrl file : files) {
                file.titleId = (int) titleId;
                fileDao.insertFile(file);
            }
        });
    }

    // 通过 title 查询文件信息
    public List<TitleWithFileNames> getFilesByTitle(String title) {
        return titleDao.getTitleWithFiles(title);
    }

    // 通过 fileName 查询 imageUrl
    public String getImageUrlByFileName(String fileName) {
        return fileDao.getImageUrlByFileName(fileName);
    }

    // 读取 JSON 文件并将内容存储到数据库
    public void loadJsonToDatabase(Context context) {
        executor.execute(() -> {
            try (InputStreamReader reader = new InputStreamReader(context.getAssets().open("hash2url.json"))) {
                Type type = new TypeToken<Map<String, Map<String, String>>>() {}.getType();
                Map<String, Map<String, String>> jsonData = new Gson().fromJson(reader, type);

                for (Map.Entry<String, Map<String, String>> titleEntry : jsonData.entrySet()) {
                    String title = titleEntry.getKey();
                    Map<String, String> files = titleEntry.getValue();

                    Title titleEntity = new Title(title);
                    long titleId = titleDao.insertTitle(titleEntity);

                    List<FileNameWithImageUrl> fileList = new ArrayList<>();
                    for (Map.Entry<String, String> fileEntry : files.entrySet()) {
                        FileNameWithImageUrl fileRecord = new FileNameWithImageUrl();
                        fileRecord.titleId = (int) titleId;
                        fileRecord.fileName = fileEntry.getKey();
                        fileRecord.imageUrl = fileEntry.getValue();
                        fileList.add(fileRecord);
                    }
                    insertTitleAndFiles(title, fileList);
                }
                Log.d("FileRepository", "Data loaded into database successfully.");
            } catch (Exception e) {
                Log.e("FileRepository", "Error loading JSON data", e);
            }
        });
    }
}
