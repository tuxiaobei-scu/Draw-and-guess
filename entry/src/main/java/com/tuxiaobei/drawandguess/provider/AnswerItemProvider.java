package com.tuxiaobei.drawandguess.provider;
import com.tuxiaobei.drawandguess.ResourceTable;
import com.tuxiaobei.drawandguess.slice.MainAbilitySlice;
import com.tuxiaobei.drawandguess.util.Tools;
import ohos.aafwk.ability.AbilitySlice;
import ohos.agp.components.*;
import com.tuxiaobei.drawandguess.bean.AnswerItem;
import ohos.agp.components.element.Element;

import java.util.List;
public class AnswerItemProvider extends BaseItemProvider{
    private List<AnswerItem> list;
    private MainAbilitySlice slice;
    private Boolean isLocal;
    public AnswerItemProvider(List<AnswerItem> list, MainAbilitySlice slice, Boolean isLocal) {
        this.list = list;
        this.slice = slice;
        this.isLocal = isLocal;
    }

    public void addData(AnswerItem ans) {
        list.add(ans);
    }
    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }
    @Override
    public Object getItem(int position) {
        if (list != null && position >= 0 && position < list.size()){
            return list.get(position);
        }
        return null;
    }
    @Override
    public long getItemId(int position) {
        //可添加具体处理逻辑
        //...
        return position;
    }
    @Override
    public Component getComponent(int position, Component convertComponent, ComponentContainer componentContainer) {
        final Component cpt;
        cpt = LayoutScatter.getInstance(slice).parse(ResourceTable.Layout_item_answer, null, false);
        AnswerItem sampleItem = list.get(position);
        Text text = (Text) cpt.findComponentById(ResourceTable.Id_item_info);
        String ret = null;
        boolean myans = sampleItem.getDeviceId().equals(Tools.getDeviceId(slice));
        switch (sampleItem.getStatus()) {
            case 1: //错误答案
                ret = sampleItem.getAns();
                text = (Text) cpt.findComponentById(ResourceTable.Id_item_wa);
                break;
            case 4: //系统消息
                ret = sampleItem.getAns();
                break;
            case 2: //相似答案
                if (myans) {
                    ret = "你的答案很接近了：" + sampleItem.getAns();
                } else if (isLocal){
                    ret = "答案很接近了：" + sampleItem.getAns();
                } else {
                    ret = "答案很接近了";
                }
                text = (Text) cpt.findComponentById(ResourceTable.Id_item_sim);
                break;
            case 3: //正确答案
                if (myans) {
                    ret = "恭喜你回答正确！";
                } else {
                    ret = "回答正确！";
                }
                text = (Text) cpt.findComponentById(ResourceTable.Id_item_ok);
                break;
        }
        if (ret != null) {
            text.setText(slice.getName(sampleItem.getDeviceId()) + ":" + ret);
            text.setVisibility(Component.VISIBLE);
            return cpt;
        } else {
            return null;
        }

    }
}
