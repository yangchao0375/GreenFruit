/*Author：杨超
Date：2021-07-09
Update:2021-07-10
Project：绿小果集群小车
Version：1.1.1
 */
package com.example.greenfruit.uitls;

import com.example.greenfruit.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.AbilitySliceLifecycleExecutor;
import ohos.agp.colors.RgbColor;
import ohos.agp.components.Component;
import ohos.agp.components.DirectionalLayout;
import ohos.agp.components.LayoutScatter;
import ohos.agp.components.Text;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.utils.Color;
import ohos.agp.utils.LayoutAlignment;
import ohos.agp.utils.TextAlignment;
import ohos.agp.window.dialog.CommonDialog;
import ohos.agp.window.dialog.ToastDialog;
import ohos.app.AbilityContext;
import ohos.app.Context;
import ohos.data.distributed.common.KvManagerConfig;
import ohos.data.distributed.common.KvManagerFactory;
import ohos.distributedschedule.interwork.DeviceInfo;
import ohos.distributedschedule.interwork.DeviceManager;
import ohos.utils.zson.ZSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ohos.agp.components.ComponentContainer.LayoutConfig.MATCH_CONTENT;
import static ohos.agp.components.ComponentContainer.LayoutConfig.MATCH_PARENT;

public class CommonTools {

    public static void showConfirmTips(AbilityContext context, String msg) {
        CommonDialog toastDialog = new CommonDialog(context);
        Component rootView = LayoutScatter.getInstance(context)
                .parse(ResourceTable.Layout_dialog, null, false);
        Text text = (Text) rootView.findComponentById(ResourceTable.Id_toast_content);
        Text confirm = (Text) rootView.findComponentById(ResourceTable.Id_toast_confirm);
        text.setText(msg);
        confirm.setClickedListener(c -> toastDialog.remove());
        toastDialog.setSize(MATCH_PARENT, MATCH_CONTENT);
        toastDialog.setAlignment(LayoutAlignment.CENTER);
        toastDialog.setTransparent(true);
        toastDialog.setCornerRadius(15);
        toastDialog.setContentCustomComponent(rootView);
        toastDialog.show();
    }

    public static void sendMsg(final String ipAddress,final int linkPort, final Map<String,String> msgMap) {
        new Thread(() -> {
            ZSONObject address = new ZSONObject();
            try {
                for(Map.Entry<String,String> entry:msgMap.entrySet()){
                    address.put(entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                InetAddress targetAddress = InetAddress.getByName(ipAddress);
                DatagramSocket socket = new DatagramSocket();
                DatagramPacket packet = new DatagramPacket(address.toString().getBytes(), address.toString().length(), targetAddress, linkPort);
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


    public static String getLocalDeviceId(AbilityContext context){
        String localDeviceId  = KvManagerFactory.getInstance().createKvManager(new KvManagerConfig(context))
                .getLocalDeviceInfo().getId();
        return localDeviceId;
    }

    //获取总线上的设备的id
    public static String getOnLineOtherDeviceId(){
        List<DeviceInfo> deviceList = DeviceManager.getDeviceList(DeviceInfo.FLAG_GET_ONLINE_DEVICE);
        if(deviceList.isEmpty()){
            return null;
        }
        int deviceNum = deviceList.size();

        List<String> deviceIds = new ArrayList<>(deviceNum);
        List<String> deviceNames = new ArrayList<>(deviceNum);

        deviceList.forEach((device)->{
            deviceIds.add(device.getDeviceId());
            deviceNames.add(device.getDeviceName());
        });

        //我们这里的实验环境，就两部手机，组件还没讲
        //我就直接使用deviceIds的第一个元素，做为启动远程设备的目标id
        String devcieIdStr = deviceIds.get(0);
        System.out.println("在线的另一台设备的ID：" + devcieIdStr);
        return devcieIdStr;
    }


    public static void showTip(Context context, String msg, int durationTime){
        //提示框的核心组件文本
        Text text = new Text(context);
        text.setWidth(MATCH_CONTENT);
        text.setHeight(MATCH_CONTENT);
        text.setTextSize(16, Text.TextSizeType.FP);
        text.setText(msg);
        text.setPadding(30,20,30,20);
        text.setMultipleLine(true);
        text.setMarginLeft(30);
        text.setMarginRight(30);
        text.setTextColor(Color.WHITE);
        text.setTextAlignment(TextAlignment.CENTER);

        //给上面的文本设置一个背景样式
        ShapeElement style = new ShapeElement();
        style.setShape(ShapeElement.RECTANGLE);
        style.setRgbColor(new RgbColor(77,77,77));
        style.setCornerRadius(15);
        text.setBackground(style);

        //构建存放上面的text的布局
        DirectionalLayout mainLayout = new DirectionalLayout(context);
        mainLayout.setWidth(MATCH_PARENT);
        mainLayout.setHeight(MATCH_CONTENT);
        mainLayout.setAlignment(LayoutAlignment.CENTER);
        mainLayout.addComponent(text);

        //最后要让上面的组件绑定dialog
        ToastDialog toastDialog = new ToastDialog(context);
        toastDialog.setSize(MATCH_PARENT,MATCH_CONTENT);
        toastDialog.setDuration(durationTime);
        toastDialog.setAutoClosable(true);
        toastDialog.setTransparent(true);
        toastDialog.setAlignment(LayoutAlignment.CENTER);
        toastDialog.setComponent(mainLayout);
        toastDialog.show();
    }

    public static String numToIP(int ip) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i<=3; i++) {
            sb.append((ip >>> (i * 8)) & 0x000000ff);
            if (i != 3) {
                sb.append('.');
            }
        }
        //System.out.println(sb);
        return sb.toString();

    }

    public static boolean isForeground(AbilitySlice context) {
        if(context.getState() == AbilitySliceLifecycleExecutor.LifecycleState.ACTIVE){
            return true;
        }else {
            return false;
        }
    }

}
