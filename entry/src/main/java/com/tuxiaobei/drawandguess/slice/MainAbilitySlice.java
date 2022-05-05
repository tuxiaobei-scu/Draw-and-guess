/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tuxiaobei.drawandguess.slice;

import static ohos.agp.components.ComponentContainer.LayoutConfig.MATCH_PARENT;
import static ohos.security.SystemPermission.DISTRIBUTED_DATASYNC;

import com.tuxiaobei.drawandguess.MainAbility;
import com.tuxiaobei.drawandguess.ResourceTable;
import com.tuxiaobei.drawandguess.bean.AnswerItem;
import com.tuxiaobei.drawandguess.bean.MyPoint;
import com.tuxiaobei.drawandguess.component.ChangeName;
import com.tuxiaobei.drawandguess.component.DeviceSelectDialog;
import com.tuxiaobei.drawandguess.component.DrawPoint;
import com.tuxiaobei.drawandguess.component.WordSelectDialog;
import com.tuxiaobei.drawandguess.game.Guesser;
import com.tuxiaobei.drawandguess.game.MainGame;
import com.tuxiaobei.drawandguess.provider.AnswerItemProvider;
import com.tuxiaobei.drawandguess.util.GsonUtil;
import com.tuxiaobei.drawandguess.util.LogUtils;

import com.tuxiaobei.drawandguess.util.Tools;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.*;
import ohos.bundle.IBundleManager;
import ohos.data.distributed.common.*;
import ohos.data.distributed.user.SingleKvStore;
import ohos.global.resource.NotExistException;
import ohos.global.resource.WrongTypeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * MainAbilitySlice
 *
 * @since 2021-04-06
 */
public class MainAbilitySlice extends AbilitySlice {
    private static final String TAG = MainAbilitySlice.class.getName();
    private static final int PERMISSION_CODE = 20201203;
    private static final int DELAY_TIME = 10;
    private static final String STORE_ID_KEY = "storeId";
    private static final String POINTS_KEY = "points";
    private static final String ANS_KEY = "ans";
    private static final String COLOR_INDEX_KEY = "colorIndex";
    private static final String IS_FORM_LOCAL_KEY = "isFormLocal";
    private static String storeId;
    private DependentLayout canvas;
    private Image transform;
    private Image change_name;
    private KvManager kvManager;
    private SingleKvStore pointsSingleKvStore;
    private SingleKvStore ansSingleKvStore;
    private SingleKvStore nameSingleKvStore;
    private Text title;
    private DrawPoint drawl;
    private Image play;
    private Button back;
    private Text show_score;
    private Guesser guesser;
    private Text tip;
    private Button submit;
    private TextField ans;
    private MainGame main_game;
    private ListContainer ans_list;
    private final List<AnswerItem> ansData = new ArrayList<>();
    private AnswerItemProvider answerItemProvider;
    private String deviceId;
    private String local_name;
    private boolean isLocal;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);

        super.setUIContent(ResourceTable.Layout_ability_main);
        storeId = STORE_ID_KEY + Tools.getRandom();
        isLocal = !intent.getBooleanParam(IS_FORM_LOCAL_KEY, false);
        drawl = new DrawPoint(this, isLocal);
        findComponentById(isLocal);
        requestPermission();
        initView(intent);
        initDatabase();
        if (isLocal) {
            nameSingleKvStore.putString(Tools.getDeviceId(this), "系统");
        }
        initDraw(intent);
        ohos.global.resource.ResourceManager resManager = getResourceManager();
        String[] words = null;
        try {
            words = resManager.getElement(ResourceTable.Strarray_words).getStringArray();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotExistException e) {
            e.printStackTrace();
        } catch (WrongTypeException e) {
            e.printStackTrace();
        }
        main_game = new MainGame(this, words);
        initListContainer();
    }


    private void initListContainer() {
        answerItemProvider = new AnswerItemProvider(ansData, this, isLocal);
        ans_list.setItemProvider(answerItemProvider);
    }


    private void initView(Intent intent) {

        if (!isLocal) {
            local_name = Tools.getDeviceId(this).substring(0, 6);
            storeId = intent.getStringParam(STORE_ID_KEY);
        }
        title.setText(isLocal ? "本地端" : local_name);
        transform.setVisibility(isLocal ? Component.VISIBLE : Component.INVISIBLE);
    }

    private void requestPermission() {
        if (verifySelfPermission(DISTRIBUTED_DATASYNC) != IBundleManager.PERMISSION_GRANTED) {
            if (canRequestPermission(DISTRIBUTED_DATASYNC)) {
                requestPermissionsFromUser(new String[]{DISTRIBUTED_DATASYNC}, PERMISSION_CODE);
            }
        }
    }

    private void setName(String name) {
        title.setText(name);
        local_name = name;
        nameSingleKvStore.putString(Tools.getDeviceId(this), name);
    }

    private void findComponentById(Boolean isLocal) {
        if (findComponentById(ResourceTable.Id_canvas) instanceof DependentLayout) {
            canvas = (DependentLayout) findComponentById(ResourceTable.Id_canvas);
        }
        if (findComponentById(ResourceTable.Id_transform) instanceof Image) {
            transform = (Image) findComponentById(ResourceTable.Id_transform);
        }
        if (findComponentById(ResourceTable.Id_change_name) instanceof Image) {
            change_name = (Image) findComponentById(ResourceTable.Id_change_name);
        }
        if (findComponentById(ResourceTable.Id_play) instanceof Image) {
            play = (Image) findComponentById(ResourceTable.Id_play);
        }
        if (findComponentById(ResourceTable.Id_title) instanceof Text) {
            title = (Text) findComponentById(ResourceTable.Id_title);
        }
        if (findComponentById(ResourceTable.Id_back) instanceof Button) {
            back = (Button) findComponentById(ResourceTable.Id_back);
        }
        if (findComponentById(ResourceTable.Id_ans) instanceof TextField) {
            ans = (TextField) findComponentById(ResourceTable.Id_ans);
        }
        if (findComponentById(ResourceTable.Id_submit) instanceof Button) {
            submit = (Button) findComponentById(ResourceTable.Id_submit);
        }
        if (findComponentById(ResourceTable.Id_tip) instanceof Text) {
            tip = (Text) findComponentById(ResourceTable.Id_tip);
        }
        if (findComponentById(ResourceTable.Id_list_answers) instanceof ListContainer) {
            ans_list = (ListContainer) findComponentById(ResourceTable.Id_list_answers);
        }
        if (findComponentById(ResourceTable.Id_show_score) instanceof Text) {
            show_score = (Text) findComponentById(ResourceTable.Id_show_score);
        }

        if (isLocal) {
            if (findComponentById(ResourceTable.Id_red) instanceof Button) {
                drawl.addButton((Button) findComponentById(ResourceTable.Id_red));
            }
            if (findComponentById(ResourceTable.Id_green) instanceof Button) {
                drawl.addButton((Button) findComponentById(ResourceTable.Id_green));
            }
            if (findComponentById(ResourceTable.Id_blue) instanceof Button) {
                drawl.addButton((Button) findComponentById(ResourceTable.Id_blue));
            }
            if (findComponentById(ResourceTable.Id_yellow) instanceof Button) {
                drawl.addButton((Button) findComponentById(ResourceTable.Id_yellow));
            }
            if (findComponentById(ResourceTable.Id_pink) instanceof Button) {
                drawl.addButton((Button) findComponentById(ResourceTable.Id_pink));
            }
            if (findComponentById(ResourceTable.Id_cyan) instanceof Button) {
                drawl.addButton((Button) findComponentById(ResourceTable.Id_cyan));
            }
            if (findComponentById(ResourceTable.Id_black) instanceof Button) {
                drawl.addButton((Button) findComponentById(ResourceTable.Id_black));
            }

            ans.setVisibility(Component.HIDE);
            submit.setVisibility(Component.HIDE);
        } else {
            back.setVisibility(Component.HIDE);
            play.setVisibility(Component.HIDE);
            show_score.setVisibility(Component.VISIBLE);
            change_name.setVisibility(Component.VISIBLE);
            guesser = new Guesser(submit, ans, tip, show_score, this);
        }
        transform.setClickedListener(component -> {
            DeviceSelectDialog dialog = new DeviceSelectDialog(MainAbilitySlice.this);
            dialog.setListener(deviceIds -> {
                if (deviceIds != null && !deviceIds.isEmpty()) {
                    // 启动远程页面
                    startRemoteFas(deviceIds);
                    // 同步远程数据库
                    pointsSingleKvStore.sync(deviceIds, SyncMode.PUSH_ONLY);
                    ansSingleKvStore.sync(deviceIds, SyncMode.PUSH_ONLY);
                    nameSingleKvStore.sync(deviceIds, SyncMode.PUSH_ONLY);
                }
                dialog.hide();
            });
            dialog.show();
        });
        play.setClickedListener(component -> {
            WordSelectDialog dialog = new WordSelectDialog(MainAbilitySlice.this, main_game.getWords());
            dialog.setListener(word -> {
                if (!word.isEmpty()) {
                    newGame(word);
                }
                dialog.hide();
            });
            dialog.show();
        });
        change_name.setClickedListener(component -> {
            ChangeName dialog = new ChangeName(MainAbilitySlice.this, local_name);
            dialog.setListener(name -> {
                if (!name.isEmpty()) {
                    setName(name);
                }
                dialog.hide();
            });
            dialog.show();
        });
    }



    /**
     * Initialize art boards
     *
     * @param intent Intent
     */
    private void initDraw(Intent intent) {
        boolean isLocal = !intent.getBooleanParam(IS_FORM_LOCAL_KEY, false);

        //int colorIndex = switchcolor.getColorIndex();

        drawl.setWidth(MATCH_PARENT);
        drawl.setWidth(MATCH_PARENT);
        canvas.addComponent(drawl);

        drawPoints();
        drawl.setOnDrawBack(points -> {
            if (points != null && points.size() > 1) {
                String pointsString = GsonUtil.objectToString(points);
                LogUtils.info(TAG, "pointsString::" + pointsString);
                if (pointsSingleKvStore != null) {
                    pointsSingleKvStore.putString(POINTS_KEY, pointsString);
                }
            }
        });

        back.setClickedListener(component -> {
            List<MyPoint> points = drawl.getPoints();
            if (points == null || points.size() <= 1) {
                return;
            }
            points.remove(points.size() - 1);
            for (int i = points.size() - 1; i >= 0; i--) {
                if (points.get(i).isLastPoint()) {
                    break;
                }
                points.remove(i);
            }
            drawl.setDrawParams(points);
            String pointsString = GsonUtil.objectToString(points);
            if (pointsSingleKvStore != null) {
                pointsSingleKvStore.putString(POINTS_KEY, pointsString);
            }
        });
    }

    public boolean getIslocal() {
        return isLocal;
    }


    public void addAnswer(AnswerItem ans) {
        ans.setDeviceId(Tools.getDeviceId(this));
        ansData.add(0, ans);
        String ansString = GsonUtil.objectToString(ansData);
        if (ansSingleKvStore != null) {
            ansSingleKvStore.putString(ANS_KEY, ansString);
        }
    }

    private void newGame(String word) {
        ansData.clear();
        AnswerItem a = new AnswerItem("开始第 " + main_game.getRoundnum() +" 轮游戏了！", 4);
        a.setWord(word);
        addAnswer(a);
        //answerItemProvider.notifyDataChanged();
        main_game.setWord(word);
        main_game.generateWords();
        title.setText(word);
        clearBoard();
    }

    private void showAns(List<AnswerItem> answers) {
        if (!isLocal) {
            guesser.checkEnable(answers);
        }
        ansData.clear();
        ansData.addAll(answers);
        answerItemProvider.notifyDataChanged();
    }

    private void clearBoard() {
        List<MyPoint> points = new ArrayList<>(0);
        drawl.setDrawParams(points);
        String pointsString = GsonUtil.objectToString(points);
        if (pointsSingleKvStore != null) {
            pointsSingleKvStore.putString(POINTS_KEY, pointsString);
        }
    }

    // 获取数据库中的点数据，并在画布上画出来
    private void drawPoints() {
        List<Entry> points = pointsSingleKvStore.getEntries(POINTS_KEY);
        for (Entry entry : points) {
            if (entry.getKey().equals(POINTS_KEY)) {
                List<MyPoint> remotePoints = GsonUtil.jsonToList(pointsSingleKvStore.getString(POINTS_KEY), MyPoint.class);
                getUITaskDispatcher().delayDispatch(() -> drawl.setDrawParams(remotePoints), DELAY_TIME);
            }
        }
    }

    private void updateAnsShow() {
        List<Entry> ans = ansSingleKvStore.getEntries(ANS_KEY);
        for (Entry entry : ans) {
            if (entry.getKey().equals(ANS_KEY)) {
                List<AnswerItem> remoteAns= GsonUtil.jsonToList(ansSingleKvStore.getString(ANS_KEY), AnswerItem.class);
                getUITaskDispatcher().delayDispatch(() -> {showAns(remoteAns);}, DELAY_TIME);
            }
        }
    }

    public String getName(String deviceId) {
        if (Tools.getDeviceId(this).equals(deviceId)) {
            if (isLocal) {
                return "系统";
            }
            return local_name;
        }
        String ret;
        try {
            ret = nameSingleKvStore.getString(deviceId);
        }catch (KvStoreException e) {
            ret = deviceId.substring(0, 6);
        }
        return ret;
    }
    /**
     * Receive database messages
     *
     * @since 2021-04-06
     */
    private class pointsKvStoreObserverClient implements KvStoreObserver {
        @Override
        public void onChange(ChangeNotification notification) {
            LogUtils.info(TAG, "data changed......");
            drawPoints();
        }
    }

    private class ansKvStoreObserverClient implements KvStoreObserver {
        @Override
        public void onChange(ChangeNotification notification) {
            LogUtils.info(TAG, "ans changed......");
            updateAnsShow();
        }
    }

    private class nameKvStoreObserverClient implements KvStoreObserver {
        @Override
        public void onChange(ChangeNotification notification) {
            LogUtils.info(TAG, "name changed......");
            getUITaskDispatcher().delayDispatch(() -> {answerItemProvider.notifyDataChanged();}, DELAY_TIME);
        }
    }

    private void initDatabase() {
        // 创建分布式数据库管理对象
        KvManagerConfig config = new KvManagerConfig(this);
        kvManager = KvManagerFactory.getInstance().createKvManager(config);
        // 创建分布式数据库
        Options options = new Options();
        options.setCreateIfMissing(true).setEncrypt(false).setKvStoreType(KvStoreType.SINGLE_VERSION);
        pointsSingleKvStore = kvManager.getKvStore(options, storeId);
        // 订阅分布式数据变化
        KvStoreObserver kvStoreObserverClient = new pointsKvStoreObserverClient();
        pointsSingleKvStore.subscribe(SubscribeType.SUBSCRIBE_TYPE_ALL, kvStoreObserverClient);

        // 创建分布式数据库
        ansSingleKvStore = kvManager.getKvStore(options, storeId);
        // 订阅分布式数据变化
        KvStoreObserver kvStoreObserverClient1 = new ansKvStoreObserverClient();
        ansSingleKvStore.subscribe(SubscribeType.SUBSCRIBE_TYPE_ALL, kvStoreObserverClient1);

        // 创建分布式数据库
        nameSingleKvStore = kvManager.getKvStore(options, storeId);
        // 订阅分布式数据变化
        KvStoreObserver kvStoreObserverClient2 = new nameKvStoreObserverClient();
        nameSingleKvStore.subscribe(SubscribeType.SUBSCRIBE_TYPE_ALL, kvStoreObserverClient2);

    }

    /**
     * Starting Multiple Remote Fas
     *
     * @param deviceIds deviceIds
     */
    private void startRemoteFas(List<String> deviceIds) {
        Intent[] intents = new Intent[deviceIds.size()];
        for (int i = 0; i < deviceIds.size(); i++) {
            Intent intent = new Intent();
            intent.setParam(IS_FORM_LOCAL_KEY, true);
            intent.setParam(COLOR_INDEX_KEY, i + 1);
            intent.setParam(STORE_ID_KEY, storeId);
            Operation operation = new Intent.OperationBuilder()
                    .withDeviceId(deviceIds.get(i))
                    .withBundleName(getBundleName())
                    .withAbilityName(MainAbility.class.getName())
                    .withFlags(Intent.FLAG_ABILITYSLICE_MULTI_DEVICE)
                    .build();
            intent.setOperation(operation);
            intents[i] = intent;
        }
        startAbilities(intents);
    }



    @Override
    protected void onStop() {
        super.onStop();
        kvManager.closeKvStore(pointsSingleKvStore);
    }

}