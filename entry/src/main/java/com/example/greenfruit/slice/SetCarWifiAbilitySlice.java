/*Author：杨超
Date：2021-07-09
Update:2021-07-10
Project：绿小果集群小车
Version：1.1.1
 */

package com.example.greenfruit.slice;

import com.example.greenfruit.ResourceTable;
import com.example.greenfruit.uitls.CommonTools;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.TextField;
import ohos.agp.window.service.WindowManager;
import ohos.bundle.ElementName;

import java.util.HashMap;
import java.util.Map;

public class SetCarWifiAbilitySlice extends AbilitySlice {
    private TextField wifiNameTextfield, wifiPasswdTextfield;
    private Button sendWifiMsgBtn;
    private int chooseCarNo;

    @Override
    public void onStart(Intent intent) {
        System.out.println("SetCarWifiAbilitySlice....");
        super.onStart(intent);
        getWindow().addFlags(WindowManager.LayoutConfig.MARK_TRANSLUCENT_STATUS);
        super.setUIContent(ResourceTable.Layout_ability_set_car_wifi);
        wifiNameTextfield = (TextField) findComponentById(ResourceTable.Id_wf_name_textfield);
        wifiPasswdTextfield = (TextField) findComponentById(ResourceTable.Id_wf_passwd_textfield);
        sendWifiMsgBtn = (Button) findComponentById(ResourceTable.Id_send_wf_msg_btn);

        sendWifiMsgBtn.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                Intent ctrlCarIntent = new Intent();
                ctrlCarIntent.setElement(new ElementName("", "com.example.greenfruit", ".CtrlCarAbility"));
                if (intent != null) {
                    chooseCarNo = intent.getIntParam("chooseCarNo", 0);
                    ctrlCarIntent.setParam("chooseCarNo", chooseCarNo);

                }
                Map<String, String> msg = new HashMap<>();
                msg.put("ssid", wifiNameTextfield.getText().trim());
                msg.put("passwd", wifiPasswdTextfield.getText().trim());
                if(chooseCarNo == 001){
                    CommonTools.sendMsg("192.168.10.1", 28881, msg);
                }else{
                    CommonTools.sendMsg("192.168.11.1", 28881, msg);
                }
                startAbility(ctrlCarIntent);
            }
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
