/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tuxiaobei.drawandguess.component;

import com.tuxiaobei.drawandguess.ResourceTable;
import com.tuxiaobei.drawandguess.component.listcomponent.CommentViewHolder;
import com.tuxiaobei.drawandguess.component.listcomponent.ListComponentAdapter;
import ohos.agp.components.Component;
import ohos.agp.components.LayoutScatter;
import ohos.agp.components.ListContainer;
import ohos.agp.components.Text;
import ohos.agp.utils.LayoutAlignment;
import ohos.agp.window.dialog.CommonDialog;
import ohos.app.Context;
import java.util.Arrays;


import java.util.List;

import static ohos.agp.components.ComponentContainer.LayoutConfig.MATCH_CONTENT;
import static ohos.agp.components.ComponentContainer.LayoutConfig.MATCH_PARENT;

/**
 * WordSelectDialog
 *
 * @since 2022-05-04
 */
public class WordSelectDialog extends CommonDialog {
    private static final int CORNER_RADIUS = 10;
    private ListContainer listContainer;
    private final Context context;
    private OnclickListener onclickListener;
    private List<String> wordsList = null;
    private String checkedword = new String();

    private ListComponentAdapter listComponentAdapter;
    private Text operateConfirm;
    private Text operateCancel;

    /**
     * Interfaces for setting the OK button and canceling clicks
     *
     * @since 2021-04-06
     */
    public interface OnclickListener {
        /**
         * Used for interface callback.
         *
         * @param word select word
         */
        void onConfirmClick(String word);
    }

    /**
     * setListener
     *
     * @param context context
     */
    public WordSelectDialog(Context context, String[] words) {
        super(context);
        this.context = context;
        wordsList = Arrays.asList(words);
    }

    /**
     * setListener
     *
     * @param listener listener
     */
    public void setListener(OnclickListener listener) {
        onclickListener = listener;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        initView();
        setAdapter();
    }

    private void initView() {
        Component rootView = LayoutScatter.getInstance(context)
                .parse(ResourceTable.Layout_dialog_layout_words, null, false);
        if (rootView.findComponentById(ResourceTable.Id_list_container_words) instanceof ListContainer) {
            listContainer = (ListContainer) rootView.findComponentById(ResourceTable.Id_list_container_words);
        }
        if (rootView.findComponentById(ResourceTable.Id_operate_yes) instanceof Text) {
            operateConfirm = (Text) rootView.findComponentById(ResourceTable.Id_operate_yes);
        }
        if (rootView.findComponentById(ResourceTable.Id_operate_no) instanceof Text) {
            operateCancel = (Text) rootView.findComponentById(ResourceTable.Id_operate_no);
        }
        setSize(MATCH_PARENT, MATCH_CONTENT);
        setAlignment(LayoutAlignment.BOTTOM);
        setCornerRadius(CORNER_RADIUS);
        setAutoClosable(true);
        setContentCustomComponent(rootView);
        setTransparent(true);

        componentBonding();
    }

    private void componentBonding() {
        operateConfirm.setClickedListener(component -> {
            if (onclickListener != null) {
                cirWord();
            }
        });
        operateCancel.setClickedListener(component -> hide());
    }

    private void cirWord() {
        onclickListener.onConfirmClick(checkedword);
    }


    private void setAdapter() {
        listComponentAdapter = new ListComponentAdapter<String>(context,
                wordsList, ResourceTable.Layout_dialog_word_item) {
            @Override
            public void onBindViewHolder(CommentViewHolder commonViewHolder, String item, int position) {
                commonViewHolder.getTextView(ResourceTable.Id_item_desc)
                        .setText(item);
                commonViewHolder.getImageView(ResourceTable.Id_item_check).setPixelMap(checkedword.equals(item)
                        ? ResourceTable.Media_checked_point : ResourceTable.Media_uncheck_point);
            }

            @Override
            public void onItemClick(Component component, String item, int position) {
                super.onItemClick(component, item, position);
                checkedword = item;
                listComponentAdapter.notifyDataChanged();
            }
        };
        listContainer.setItemProvider(listComponentAdapter);
    }
}
