package com.example.yypome.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "card_statistics_table")
public class CardStatistics {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String cardTitle;
    private int visitCount;         // 复习次数
    private int incorrectCount;     // 错题数
    private int exerciseCount;      // 做题数
    private int checkCardFlag;
    private int reviewCardFlag;

    public CardStatistics(String cardTitle, int visitCount, int incorrectCount, int exerciseCount, int checkCardFlag, int reviewCardFlag) {
        this.cardTitle = cardTitle;
        this.visitCount = visitCount;
        this.incorrectCount = incorrectCount;
        this.exerciseCount = exerciseCount;
        this.checkCardFlag = checkCardFlag;
        this.reviewCardFlag = reviewCardFlag;
    }

    // Getter 和 Setter 方法
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCardTitle() { return cardTitle; }
    public void setCardTitle(String cardTitle) { this.cardTitle = cardTitle; }
    public int getVisitCount() { return visitCount; }
    public void setVisitCount(int visitCount) { this.visitCount = visitCount; }
    public int getIncorrectCount() { return incorrectCount; }
    public void setIncorrectCount(int incorrectCount) { this.incorrectCount = incorrectCount; }

    public int getCheckCardFlag() {
        return checkCardFlag;
    }

    public void setCheckCardFlag(int checkCardFlag) {
        this.checkCardFlag = checkCardFlag;
    }

    public int getReviewCardFlag() {
        return reviewCardFlag;
    }

    public void setReviewCardFlag(int reviewCardFlag) {
        this.reviewCardFlag = reviewCardFlag;
    }

    public int getExerciseCount() {
        return exerciseCount;
    }

    public void setExerciseCount(int exerciseCount) {
        this.exerciseCount = exerciseCount;
    }
}
