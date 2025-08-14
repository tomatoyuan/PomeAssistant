package com.example.yypome.data;

import java.util.List;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.yypome.db.Converters;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "card_data_table")
@TypeConverters({Converters.class})
public class CardData {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String grade;
    private String author;
    private String image;

//    @ColumnInfo(name = "completion_date")
    private String completionDate;

    @ColumnInfo(name = "recitation_count")
    private int recitationCount;
    private String original_text;
    private String translation_text;
    private String comment_text;
    private String author_text;
    private String work_introduction;
    private String creative_background;
    private String appreciation;

    private String approach;
    private String style;

    @SerializedName("exercise")  // 将 JSON 中的 "exercise" 字段映射到这个字段
    List<Question> questions;
    @SerializedName("short_answer_question")
    List<Question> short_answer_question;

    private List<String> nice_sentence;
    private List<String> tasks;
    private String suggestMethod; // 新增字段

    public CardData() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(String completionDate) {
        this.completionDate = completionDate;
    }

    public int getRecitationCount() {
        return recitationCount;
    }

    public void setRecitationCount(int recitationCount) {
        this.recitationCount = recitationCount;
    }

    public List<String> getTasks() {
        return tasks;
    }

    public void setTasks(List<String> tasks) {
        this.tasks = tasks;
    }

    public String getOriginal_text() {
        return original_text;
    }

    public void setOriginal_text(String original_text) {
        this.original_text = original_text;
    }

    public String getTranslation_text() {
        return translation_text;
    }

    public void setTranslation_text(String translation_text) {
        this.translation_text = translation_text;
    }

    public String getComment_text() {
        return comment_text;
    }

    public void setComment_text(String comment_text) {
        this.comment_text = comment_text;
    }

    public String getAuthor_text() {
        return author_text;
    }

    public void setAuthor_text(String author_text) {
        this.author_text = author_text;
    }

    public List<String> getNice_sentence() {
        return nice_sentence;
    }

    public void setNice_sentence(List<String> nice_sentence) {
        this.nice_sentence = nice_sentence;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }


    public String getCreative_background() {
        return creative_background;
    }

    public void setCreative_background(String creative_background) {
        this.creative_background = creative_background;
    }

    public String getWork_introduction() {
        return work_introduction;
    }

    public void setWork_introduction(String work_introduction) {
        this.work_introduction = work_introduction;
    }

    public String getAppreciation() {
        return appreciation;
    }

    public void setAppreciation(String appreciation) {
        this.appreciation = appreciation;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public List<Question> getShort_answer_question() {
        return short_answer_question;
    }

    public void setShort_answer_question(List<Question> short_answer_question) {
        this.short_answer_question = short_answer_question;
    }

    public String getApproach() {
        return approach;
    }

    public void setApproach(String approach) {
        this.approach = approach;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getSuggestMethod() {
        return suggestMethod;
    }

    public void setSuggestMethod(String suggestMethod) {
        this.suggestMethod = suggestMethod;
    }
}
