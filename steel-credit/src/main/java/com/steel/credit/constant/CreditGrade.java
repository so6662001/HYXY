package com.steel.credit.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 信用等级枚举
 */
@Getter
@AllArgsConstructor
public enum CreditGrade {

    AAA("AAA", 900, 1000, "极优"),
    AA("AA", 800, 899, "优秀"),
    A("A", 700, 799, "良好"),
    BBB("BBB", 600, 699, "中等偏上"),
    BB("BB", 500, 599, "中等"),
    B("B", 400, 499, "一般"),
    C("C", 200, 399, "较差"),
    D("D", 0, 199, "差"),
    NONE("暂无", -1, -1, "数据不足");

    private final String code;
    private final int minScore;
    private final int maxScore;
    private final String description;

    public static CreditGrade fromScore(int score) {
        for (CreditGrade grade : values()) {
            if (grade == NONE) continue;
            if (score >= grade.minScore && score <= grade.maxScore) {
                return grade;
            }
        }
        return NONE;
    }

    /**
     * 判断当前等级是否超过天花板等级
     */
    public boolean exceeds(CreditGrade ceiling) {
        if (ceiling == null || ceiling == NONE) return false;
        return this.minScore > ceiling.minScore;
    }
}
