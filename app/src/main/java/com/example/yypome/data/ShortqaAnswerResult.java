package com.example.yypome.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "shortqa_answer_result_table")
public class ShortqaAnswerResult {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;

    @ColumnInfo(name = "question_text")
    private String questionText;

    @ColumnInfo(name = "user_answer")
    private String userAnswer;

    @ColumnInfo(name = "correct_answer")
    private String correctAnswer;

    @ColumnInfo(name = "ast_answer")
    private String astAnswer;

    @ColumnInfo(name = "answer_date")
    private String answerDate;

    public ShortqaAnswerResult(String title, String questionText, String userAnswer, String correctAnswer, String astAnswer, String answerDate) {
        this.title = title;
        this.questionText = questionText;
        this.userAnswer = userAnswer;
        this.correctAnswer = correctAnswer;
        this.astAnswer = astAnswer;
        this.answerDate = answerDate;
    }

    // Getters and Setters
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

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getAstAnswer() {
        return astAnswer;
    }

    public void setAstAnswer(String astAnswer) {
        this.astAnswer = astAnswer;
    }

    public String getAnswerDate() {
        return answerDate;
    }

    public void setAnswerDate(String answerDate) {
        this.answerDate = answerDate;
    }
}