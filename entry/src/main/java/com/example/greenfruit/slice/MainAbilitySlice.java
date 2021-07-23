/*Author：杨超
Date：2021-07-09
Update:2021-07-21
Project：绿小果集群小车
Version：1.1.1
 */


package com.example.greenfruit.slice;

import com.example.greenfruit.uitls.DBUtils;
import com.example.greenfruit.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.window.service.WindowManager;
import ohos.bundle.ElementName;
import ohos.data.distributed.user.SingleKvStore;
import ohos.hiviewdfx.HiLogLabel;
import ohos.multimodalinput.event.TouchEvent;

import static java.lang.Thread.sleep;


public class MainAbilitySlice extends AbilitySlice {
    private Component chooseManualModeBtn,chooseAutomaticModeBtn;
    private Intent playerIntent = new Intent();
    private Intent startIntent=new Intent();

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        getWindow().addFlags(WindowManager.LayoutConfig.MARK_TRANSLUCENT_STATUS);
//        super.setUIContent(ResourceTable.Layout_ability_start);
//        startIntent.setElement(new ElementName(
//                "",
//                getBundleName(),
//                ".startAbility"
//        ));
//        startAbility(startIntent);

        super.setUIContent(ResourceTable.Layout_ability_main);
        playerIntent.setElement(new ElementName(
                "",
                getBundleName(),
                ".BJmusicServiceAbility"
        ));
        startAbility(playerIntent);

//Author：杨超
//Update:2021-07-21
//按钮修改
        chooseManualModeBtn = findComponentById(ResourceTable.Id_manual_mode_btn);
        chooseAutomaticModeBtn = findComponentById(ResourceTable.Id_automatic_mode_btn);
        chooseManualModeBtn.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                Intent MIntent = new Intent();
                MIntent.setElement(new ElementName("",
                        getBundleName(),
                        ".ChooseCarAbility"));
                MIntent.setParam("playModule", false);
                startAbility(MIntent);
                //打开分布式数据库
                SingleKvStore singleKvStore1 = DBUtils.initKvStore(MainAbilitySlice.this, "raceDB");
                DBUtils.deleteKvStore("raceDB");
                SingleKvStore singleKvStore2 = DBUtils.initKvStore(MainAbilitySlice.this, "raceGameOverDB");
                DBUtils.deleteKvStore("raceGameOverDB");
            }

        });
        chooseAutomaticModeBtn.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                Intent MIntent = new Intent();
                MIntent.setElement(new ElementName("",
                        getBundleName(),
                        ".ChooseCarAbility"));
                MIntent.setParam("playModule", true);
                startAbility(MIntent);

            }

        });
    }




    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    protected void onBackground() {
        stopAbility(playerIntent);
        super.onBackground();
    }


    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }


}

