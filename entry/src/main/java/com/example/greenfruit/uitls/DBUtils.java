/*Author：杨超
Date：2021-07-09
Update:2021-07-10
Project：绿小果集群小车
Version：1.1.1
 */
package com.example.greenfruit.uitls;

import ohos.app.Context;
import ohos.data.distributed.common.*;
import ohos.data.distributed.user.SingleKvStore;

public class DBUtils {
    private static SingleKvStore singleKvStore = null;
    private static KvManager kvManager = null;


    //创建分布式数据库
    public static SingleKvStore initKvStore(Context context, String storeId) {
        KvManagerConfig kvManagerConfig = new KvManagerConfig(context);
        kvManager = KvManagerFactory.getInstance().createKvManager(kvManagerConfig);
        Options options = new Options();
        options.setCreateIfMissing(true).setEncrypt(false).setKvStoreType(KvStoreType.SINGLE_VERSION);
        singleKvStore = kvManager.getKvStore(options, storeId); //拿到数据库
        return singleKvStore;
    }

    //删除数据库
    public static void deleteKvStore(String storeId) {
        if(singleKvStore != null){
            kvManager.closeKvStore(singleKvStore);
            kvManager.deleteKvStore(storeId);
        }
    }
}
