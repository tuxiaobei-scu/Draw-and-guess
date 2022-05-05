package com.tuxiaobei.drawandguess.bean;
import com.tuxiaobei.drawandguess.util.Tools;
public class AnswerItem {
    private String device_id;
    private String ans; //用户的答案
    private int status; //0 未检查, 1 错误, 2 接近, 3 正确, 4 系统消息
    private String rid;
    private String word; //实际答案
    public AnswerItem(String ans, int status) {
        this.ans = ans;
        this.status = status;
        rid = Tools.getRandom();
    }
    public String getDeviceId() {
        return device_id;
    }
    public String getAns() {
        return ans;
    }
    public String getRid() {
        return rid;
    }

    public String getWord() {
        return word;
    }
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    public void setWord(String word) {
        this.word = word;
    }
    public void setDeviceId(String device_id) {
        this.device_id = device_id;
    }
}
