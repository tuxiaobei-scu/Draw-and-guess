package com.tuxiaobei.drawandguess.game;

import com.tuxiaobei.drawandguess.bean.AnswerItem;
import com.tuxiaobei.drawandguess.slice.MainAbilitySlice;
import com.tuxiaobei.drawandguess.ResourceTable;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.DataAbilityHelper;
import ohos.aafwk.ability.DataAbilityRemoteException;
import ohos.global.resource.NotExistException;
import ohos.global.resource.WrongTypeException;
import ohos.utils.net.Uri;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainGame extends Ability {
    private String[] words;
    private MainAbilitySlice main_sline;
    private final int words_num = 3;
    private String[] candidate_words = new String[words_num];
    private String word;
    private int round_num;
    public MainGame(MainAbilitySlice main_sline, String[] words) {
        this.main_sline = main_sline;
        this.words = words;
        round_num = 0;
        generateWords();
    }

    /**
     * 生成候选词语
     */
    public void generateWords() {
        for (int i = 0; i < words_num; i++) {
            String s = words[(int)(Math.random() * words.length)];
            for (int j = 0; j < i ; j++) {
                if (candidate_words[j].equals(s)) {
                    s = null;
                    break;
                }
            }
            if (word != null && word.equals(s)) {
                s = null;
            }
            if (s != null) {
                candidate_words[i] = s;
            } else {
                i--;
            }
        }
    }

    public String[] getWords() {
        return candidate_words;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getRoundnum() {
        round_num += 1;
        return round_num;
    }
}
