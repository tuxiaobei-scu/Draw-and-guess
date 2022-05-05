package com.tuxiaobei.drawandguess.game;
import com.tuxiaobei.drawandguess.bean.AnswerItem;
import com.tuxiaobei.drawandguess.slice.MainAbilitySlice;
import com.tuxiaobei.drawandguess.util.Tools;
import ohos.aafwk.ability.Ability;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.Text;
import ohos.agp.components.TextField;
import ohos.agp.utils.Color;
import ohos.data.distributed.user.SingleKvStore;
import ohos.utils.zson.ZSONArray;

import java.util.List;

public class Guesser extends Ability{
    private Button submit;
    private TextField ans;
    private Text tip;
    private MainAbilitySlice main;
    private String nowroundid = null;
    private Boolean enable;
    private String word;
    private Text show_score;
    private int score;
    public Guesser(Button submit, TextField ans, Text tip, Text show_score, MainAbilitySlice main) {
        this.submit = submit;
        this.ans = ans;
        this.main = main;
        this.tip = tip;
        this.score = 0;
        this.show_score = show_score;
        submit.setClickedListener(component -> {
            String answer = ans.getText();
            ans.setText("");
            if (!answer.isEmpty() && enable) {
                AnswerItem a = new AnswerItem(answer, 0);
                checkAnswer(a);
                if (a.getStatus() == 3) {
                    ans.setVisibility(Component.HIDE);
                    submit.setVisibility(Component.HIDE);
                    ans.clearFocus();
                    ans.setTextCursorVisible(false);
                    ans.setEnabled(false);
                    tip.setText("恭喜你答对了！");
                    tip.setTextColor(new Color(Color.getIntColor("#2ecc71")));
                    tip.setVisibility(Component.VISIBLE);
                    score += 1;
                    show_score.setText("得分:"+score);
                    enable = false;
                }
                main.addAnswer(a);
            }

        });
        tip.setVisibility(Component.VISIBLE);

        enable = false;
    }

    public void checkEnable(List<AnswerItem> answers) {
        for (AnswerItem answer : answers) {
            if (answer.getStatus() == 4 && !answer.getRid().equals(nowroundid)) {
                ans.setVisibility(Component.VISIBLE);
                submit.setVisibility(Component.VISIBLE);
                tip.setVisibility(Component.HIDE);
                //submit.setTextCursorVisible(true);
                ans.setEnabled(true);
                ans.setTextCursorVisible(true);
                enable = true;
                nowroundid = answer.getRid();
                word = answer.getWord();
            }
        }
    }

    private int getLevenshteinDistance(String a, String b) {
        int la = a.length(), lb = b.length();
        int[][] f = new int[la + 1][lb + 1];
        for (int i = 1; i <= la; i++)
            f[i][0] = i;
        for (int i = 1; i <= lb; i++)
            f[0][i] = i;
        f[0][0] = 0;
        for (int i = 1; i <= la; i++) {
            for (int j = 1; j <= lb; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1))
                    f[i][j] = f[i - 1][j - 1];
                else
                    f[i][j] = Math.min(f[i - 1][j], Math.min(f[i][j - 1], f[i - 1][j - 1])) + 1;
            }
        }
        return f[la][lb];

    }

    public void checkAnswer(AnswerItem answer) {
        if (answer.getStatus() > 0) {
            return;
        }
        if (answer.getAns().equals(word)) {
            answer.setStatus(3);
            return;
        }
        int k = getLevenshteinDistance(word, answer.getAns());
        if (k == 1 || (k / word.length() < 0.25)) {
            answer.setStatus(2);
        } else {
            answer.setStatus(1);
        }
    }
}
