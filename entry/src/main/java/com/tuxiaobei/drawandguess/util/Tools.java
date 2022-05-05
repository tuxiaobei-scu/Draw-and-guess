package com.tuxiaobei.drawandguess.util;

import ohos.app.Context;
import ohos.data.distributed.common.KvManagerConfig;
import ohos.data.distributed.common.KvManagerFactory;
import java.util.Random;
public class Tools {
    /**
     * 获取设备Id
     */
    public static String getDeviceId(Context mContext){
        return KvManagerFactory.getInstance().createKvManager(new KvManagerConfig(mContext)).getLocalDeviceInfo().getId();
    }

    public static String getRandom() {
        return String.valueOf(System.currentTimeMillis()) + String.valueOf(new Random().nextInt(1000));
    }
}
