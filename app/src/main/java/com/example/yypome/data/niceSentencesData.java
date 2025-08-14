package com.example.yypome.data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class niceSentencesData  implements Serializable {
    private String title;
    private List<Map<String, String>> nice_sentences;

    // Getter 和 Setter 方法
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Map<String, String>> getNice_sentences() {
        return nice_sentences;
    }

    public void setNice_sentences(List<Map<String, String>> nice_sentences) {
        this.nice_sentences = nice_sentences;
    }
}
