/*Author：杨超
Date：2021-07-09
Update:2021-07-10
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



public class MainAbilitySlice extends AbilitySlice {
    private Component chooseModeBtn;
    private Intent playerIntent = new Intent();

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        getWindow().addFlags(WindowManager.LayoutConfig.MARK_TRANSLUCENT_STATUS);
        super.setUIContent(ResourceTable.Layout_ability_main);
        playerIntent.setElement(new ElementName(
                "",
                "com.example.greenfruit",
                "com.example.greenfruit.BJmusicServiceAbility"
        ));
        startAbility(playerIntent);


        chooseModeBtn = findComponentById(ResourceTable.Id_choose_mode_btn);
        chooseModeBtn.setTouchEventListener(new Component.TouchEventListener() {
            @Override
            public boolean onTouchEvent(Component component, TouchEvent touchEvent) {
                if(TouchEvent.PRIMARY_POINT_DOWN == touchEvent.getAction()){
                    //获取触屏点的位置
                    float x = touchEvent.getPointerPosition(0).getX();
                    float y = touchEvent.getPointerPosition(0).getY();
                    Intent ccIntent = new Intent();
                    ccIntent.setElement(new ElementName("",
                            "com.example.greenfruit",
                            ".ChooseCarAbility"));
                    //手动模式
                    if(x>=234 && x<=557 && y>=93 && y<=254 ){
                        ccIntent.setParam("playModule",false);
                        startAbility(ccIntent);
                        //打开分布式数据库
                       SingleKvStore singleKvStore1 = DBUtils.initKvStore(MainAbilitySlice.this, "raceDB");
                       DBUtils.deleteKvStore("raceDB");
                        SingleKvStore singleKvStore2 = DBUtils.initKvStore(MainAbilitySlice.this, "raceGameOverDB");
                        DBUtils.deleteKvStore("raceGameOverDB");
                    }

                    //自动模式
                    if(x>=272 && x<=607 && y>=396 && y<=539 ){
                        ccIntent.setParam("playModule",true);
                        startAbility(ccIntent);
                    }

                }

                return true;
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
