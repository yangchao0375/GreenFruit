/*Author：杨超
Date：2021-07-09
Update:2021-07-10
Project：绿小果集群小车
Version：1.1.1
 */


package com.example.greenfruit.slice;

import com.example.greenfruit.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.TextField;
import ohos.agp.window.service.WindowManager;
import ohos.bundle.ElementName;

public class CtrlCarAbilitySlice extends AbilitySlice {
    private TextField carIpTextfield;
    private Button startCtrlCarBtn;
    private String carip;
    private int chooseCarNo;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        getWindow().addFlags(WindowManager.LayoutConfig.MARK_TRANSLUCENT_STATUS);
        super.setUIContent(ResourceTable.Layout_ability_ctrl_car);
        carIpTextfield = (TextField) findComponentById(ResourceTable.Id_car_ip_textfield);
        if (intent != null) {
            chooseCarNo = intent.getIntParam("chooseCarNo",0);
            System.out.println("port:" + intent.getIntParam("chooseCarNo",0));
        }

        startCtrlCarBtn = (Button) findComponentById(ResourceTable.Id_start_ctrl_car_btn);
        startCtrlCarBtn.setClickedListener(component -> {
            Intent carHandleIntent = new Intent();
            carHandleIntent.setElement(new ElementName("", "com.example.greenfruit", ".CarHandleAbility"));
            carip = carIpTextfield.getText();
            carHandleIntent.setParam("carip", carip);
            carHandleIntent.setParam("chooseCarNo", chooseCarNo);
            startAbility(carHandleIntent);
        });

    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }
}
