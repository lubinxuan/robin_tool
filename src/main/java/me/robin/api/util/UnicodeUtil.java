package me.robin.api.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnicodeUtil {
    private static final Logger logger = LoggerFactory.getLogger(UnicodeUtil.class);

    public static void main(String[] args) {
        String str = "   安徽省2009年普通高等学校专升本招生考试高等数学真题试题 注意事项： \u00A0\u00A0\u00A01．\u00A0试卷共8页，用钢笔或圆珠笔直接答在试题卷上。 \\u00A0\\u00A0\\u00A02．\\u00A0答卷前将密封线内的项目填写清楚。 \\u00A0\\u00A0\\u00A0 一、选择题：本大题共10小题，每小题3分，共30分。在每小题给出的四个选项中，只有一项是符合题目要求的，把所选项的字母填在题后的括号内。 1．是时，函数为无穷小量的是（\\u00A0\\u00A0） A．充分条件\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0B．必要条件 C．充分必要条件\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0D．既非充分又非必要条件 2．设函数在处连续，则（\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0） A．1\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0B． C．\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0D． 3．函数在区间（3，5）内是（\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0） A．单调递增且凸\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0B．单调递增且凹 C．单调递减且凸\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0D．单调递减且凹 4．已知则＝（\\u00A0\\u00A0） A．\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0B． C．\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0D． 5．设，交换积分次序得（\\u00A0） A．\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0B． C．\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0D． 6．下列级数中发散的是（\\u00A0\\u00A0\\u00A0） A．\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0B．\\u00A0 C．\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0D． 7．已知的伴随矩阵），则（\\u00A0\\u00A0\\u00A0\\u00A0） A．2\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0B．3\\u00A0\\u00A0\\u00A0C．4\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0D．5 8．已知向量，则（\\u00A0\\u00A0\\u00A0\\u00A0） A．线性相关\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0B．\\u00A0线性相关 C．\\u00A0线性无关\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0D．线性相关 9．学习小组有10名同学，其中6名男生，4名女生，从中随机选取4人参加社会实践活动，则这4人全为男生的概率是（\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0） A．\\u00A0\\u00A0\\u00A0\\u00A0B．\\u00A0\\u00A0\\u00A0C．\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0D． 10．已知（\\u00A0） A．0.7\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0B．0.46 C．0.38\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0D．0.24 责任编辑：小草 上一篇文章： 安徽省2009年专升本计算机真题试题(单选部分) 下一篇文章： 安徽省2009年专升本英语真题试题 文章搜索: \\u00A0相关文章";
        System.out.println(str.trim());
        System.out.println(removeInvisible(str).trim());
    }

    public static String removeInvisible(String str) {
        if (null != str) {
            str = str.trim();
            StringBuilder newString = new StringBuilder(str.length());
            for (int offset = 0; offset < str.length(); ) {
                int codePoint = str.codePointAt(offset);
                offset += Character.charCount(codePoint);
                switch (Character.getType(codePoint)) {
                    case Character.CONTROL:     // \p{Cc}
                    case Character.FORMAT:      // \p{Cf}
                    case Character.PRIVATE_USE: // \p{Co}
                    case Character.SURROGATE:   // \p{Cs}
                    case Character.UNASSIGNED:  // \p{Cn}
                    case Character.OTHER_SYMBOL:  // \OTHER_SYMBOL
                    case Character.SPACE_SEPARATOR:  // \SPACE_SEPARATOR
                        newString.append(' ');
                        break;
                    default:
                        newString.append(Character.toChars(codePoint));
                        break;
                }
            }
            return newString.toString();
        } else {
            return "";
        }
    }
}
