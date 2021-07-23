/*Author：杨超
Date：2021-07-09
Update:2021-07-19
Project：绿小果集群小车
Version：1.1.1
 */

package com.example.greenfruit.slice;

import com.example.greenfruit.ResourceTable;


import com.example.greenfruit.TakePhotoAbility;
import com.example.greenfruit.uitls.CommonTools;
import com.example.greenfruit.uitls.DBUtils;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.LayoutScatter;
import ohos.agp.components.Text;
import ohos.agp.utils.LayoutAlignment;
import ohos.agp.window.dialog.CommonDialog;
import ohos.agp.window.service.WindowManager;
import ohos.app.AbilityContext;
import ohos.bundle.ElementName;
import ohos.data.distributed.common.ChangeNotification;
import ohos.data.distributed.common.Entry;
import ohos.data.distributed.common.KvStoreObserver;
import ohos.data.distributed.common.SubscribeType;
import ohos.data.distributed.user.SingleKvStore;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.multimodalinput.event.KeyEvent;
import ohos.multimodalinput.event.MmiPoint;
import ohos.multimodalinput.event.TouchEvent;
import ohos.wifi.IpInfo;
import ohos.wifi.WifiDevice;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ohos.agp.components.ComponentContainer.LayoutConfig.MATCH_CONTENT;
import static ohos.agp.components.ComponentContainer.LayoutConfig.MATCH_PARENT;


public class CarHandleAbilitySlice extends AbilitySlice {
    private Button mBtnStop, mBtnStart,mBtnCamera,mBtnCameraOpen;
    private String carIp = "";
    private int chooseCarNo = 0;
    private String phoneIP = "";
    private Map<String, String> cmdMsg;
    static final HiLogLabel label = new HiLogLabel(HiLog.LOG_APP, 0x00201, "MY_TAG");
    private DatagramSocket server_sock;
    private DatagramPacket pac;
    private byte recv_buffer[];
    private String recv_string;
    private SingleKvStore singleKvStore = null;
    private int count = 0;

    @Override
    public void onStart(Intent intent) {
        System.out.println("CarHandleAbilitySlice on start.....");
        super.onStart(intent);
        getWindow().addFlags(WindowManager.LayoutConfig.MARK_TRANSLUCENT_STATUS);
        super.setUIContent(ResourceTable.Layout_ability_car_handle);


        //获取数据库
        singleKvStore = DBUtils.initKvStore(CarHandleAbilitySlice.this, "raceGameOverDB");
        //数据库里的数据变化监听，订阅所有数据改变
        singleKvStore.subscribe(SubscribeType.SUBSCRIBE_TYPE_ALL, new KvStoreObserver() {
            @Override
            public void onChange(ChangeNotification changeNotification) {
                //刷新页面上的数据，onChange方法实质上在一个子线程里执行
//                getUITaskDispatcher()，获取绑定到 UI 线程的任务调度程序在这个调度器上调度的任务按顺序执行
//                asyncDispatch，异步调度任务。该方法分派任务并立即返回值，无需等待任务执行。
                getUITaskDispatcher().asyncDispatch(new Runnable() {
                    @Override
                    public void run() {
                        //在这里执行页面ui组件的显示刷新
                        String msg = "---";
                        List<Entry> insertEntries = changeNotification.getInsertEntries();
                        for (Entry insertEntry : insertEntries) {
                            msg = insertEntry.getValue().getString();
                        }
                        if(!"---".equals(msg) && count < 1){
//                            showGameOverConfirmTips(CarHandleAbilitySlice.this,"获胜选手"+msg+"号车！");
                            count++;
                        }
                        if (!"".equals(carIp)) {
                            cmdMsg = new HashMap<>();
                            cmdMsg.put("cmd", "stop");
                            mBtnCamera.setText("stop");
                            CommonTools.sendMsg(carIp, chooseCarNo, cmdMsg);
                        } else {
                            CommonTools.showConfirmTips(CarHandleAbilitySlice.this, "car's ip error!");
                        }
                    }
                });
            }
        });



        GetCarGameOverInfo gcinfo = new GetCarGameOverInfo();
        Thread t = new Thread(gcinfo, "gcinfoThread");
        t.start();
        System.out.println("线程开始执行--> " + t.isAlive());//判断是否启动

        if (intent != null) {
            carIp = intent.getStringParam("carip");
            chooseCarNo = intent.getIntParam("chooseCarNo", 0);
            System.out.println("ip:" + carIp + ",port:" + chooseCarNo);
        }
        // 获取WLAN管理对象
        WifiDevice wifiDevice = WifiDevice.getInstance(this);
        // 调用WLAN连接状态接口,确定当前设备是否连接WLAN
        boolean isConnected = wifiDevice.isConnected();
        while (!isConnected) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Optional<IpInfo> ipInfo = wifiDevice.getIpInfo();
        // 获取IP信息中的IP地址与网关
        int ipAddress = ipInfo.get().getIpAddress();
        phoneIP = CommonTools.numToIP(ipAddress);

        System.out.println("phoneIP:" + phoneIP);
        if (!"".equals(carIp) && chooseCarNo != 0) {
            cmdMsg = new HashMap<>();
            cmdMsg.put("cmd", phoneIP);
            CommonTools.sendMsg(carIp, chooseCarNo, cmdMsg);
        } else {
            CommonTools.showConfirmTips(CarHandleAbilitySlice.this, "car's ip error!");
        }

        mBtnStop = (Button) findComponentById(ResourceTable.Id_btn_stop);
        mBtnStart = (Button) findComponentById(ResourceTable.Id_btn_start);
        mBtnCamera=(Button) findComponentById(ResourceTable.Id_btn_camera);
        mBtnCameraOpen= (Button) findComponentById(ResourceTable.Id_btn_camera_open);
        mBtnStart.setTouchEventListener(new Component.TouchEventListener() {

            @Override
            public boolean onTouchEvent(Component component, TouchEvent touchEvent) {
                if (touchEvent.getAction() == TouchEvent.PRIMARY_POINT_DOWN) {

                    if (!"".equals(carIp) && chooseCarNo != 0) {

                        cmdMsg = new HashMap<>();
                        cmdMsg.put("cmd", "forward");
                        CommonTools.sendMsg(carIp, chooseCarNo, cmdMsg);

                    } else {

                        CommonTools.showConfirmTips(CarHandleAbilitySlice.this, "car's ip error!");
                    }
                } else if (touchEvent.getAction() == TouchEvent.OTHER_POINT_DOWN) {
                    float x = touchEvent.getPointerPosition(1).getX();
                    float y = touchEvent.getPointerPosition(1).getY();
                    System.out.println("==================================");
                    System.out.println(""+x);
                    //Author:杨超
                    //Update：2021/7/19
                    //上
                    if (x >= 1500 && x <= 1740 && y <= -178 && y >= -377) {
                        if (!"".equals(carIp) && chooseCarNo != 0) {
                            cmdMsg = new HashMap<>();
                            cmdMsg.put("cmd", "speedup");
                            CommonTools.sendMsg(carIp, chooseCarNo, cmdMsg);
                            mBtnCamera.setText("speedup");
                        } else {


                            CommonTools.showConfirmTips(CarHandleAbilitySlice.this, "car's ip error!");
                        }
                    }

                    //下
                    if (x >= 1500 && x <= 1740&& y >= -100 && y <= 175) {
                        if (!"".equals(carIp) && chooseCarNo != 0) {
                            cmdMsg = new HashMap<>();
                            cmdMsg.put("cmd", "backward");
                            CommonTools.sendMsg(carIp, chooseCarNo, cmdMsg);
                            mBtnCamera.setText("backward");
                        } else {
                            CommonTools.showConfirmTips(CarHandleAbilitySlice.this, "car's ip error!");
                        }
                    }

                    //left
                    if (x >= 1320 && x <=1540 && y >= -226 && y <= -2) {
                        if (!"".equals(carIp) && chooseCarNo != 0) {
                            mBtnCamera.setText(""+x+""+y);
                            cmdMsg = new HashMap<>();
                            cmdMsg.put("cmd", "left");
                            CommonTools.sendMsg(carIp, chooseCarNo, cmdMsg);
                            mBtnCamera.setText("left");
                        } else {
                            CommonTools.showConfirmTips(CarHandleAbilitySlice.this, "car's ip error!");
                        }
                    }

                    //right
                    if (x >= 1800&& x <= 2040 && y >= -226 && y <= -2) {
                        if (!"".equals(carIp) && chooseCarNo != 0) {
                            cmdMsg = new HashMap<>();
                            cmdMsg.put("cmd", "right");
                            CommonTools.sendMsg(carIp, chooseCarNo, cmdMsg);
                            mBtnCamera.setText("right");
                        } else {
                            CommonTools.showConfirmTips(CarHandleAbilitySlice.this, "car's ip error!");
                        }
                    }
                } else if (touchEvent.getAction() == TouchEvent.OTHER_POINT_UP) {
                    float x = touchEvent.getPointerPosition(1).getX();
                    float y = touchEvent.getPointerPosition(1).getY();
//                    System.out.println(""+x+y);
                    if ((x >= 1500 && x <= 1740 && y <= -178 && y >= -377)) {
                        if (!"".equals(carIp) && chooseCarNo != 0) {
                            cmdMsg = new HashMap<>();
                            cmdMsg.put("cmd", "speeddown");
                            CommonTools.sendMsg(carIp, chooseCarNo, cmdMsg);
                        } else {
                            CommonTools.showConfirmTips(CarHandleAbilitySlice.this, "car's ip error!");
                        }
                    } else if ((x >= 1500 && x <= 1740 && y >= 36 && y <= 135) ||
                            (x >= 1320 && x <=1540  && y >= -226 && y <= -2) ||
                            (x >= 1800&& x <= 2040  && y >= -226 && y <= -2))
                        if (!"".equals(carIp) && chooseCarNo != 0) {
                            cmdMsg = new HashMap<>();
                            cmdMsg.put("cmd", "forward");
                            CommonTools.sendMsg(carIp, chooseCarNo, cmdMsg);
                        } else {
                            CommonTools.showConfirmTips(CarHandleAbilitySlice.this, "car's ip error!");
                        }
                }
                return true;
            }
        });

        mBtnStop.setTouchEventListener(new Component.TouchEventListener() {
            @Override
            public boolean onTouchEvent(Component component, TouchEvent touchEvent) {
                if (touchEvent.getAction() == TouchEvent.PRIMARY_POINT_DOWN ||
                        touchEvent.getAction() == TouchEvent.PRIMARY_POINT_UP) {
                    if (!"".equals(carIp)) {
                        //Author：杨超
                        //UpDate：2021-07-19
//                        float x = touchEvent.getPointerPosition(1).getX();
//                        float y = touchEvent.getPointerPosition(1).getY();
//                        mBtnCamera.setText(""+x+"     "+y);
                        cmdMsg = new HashMap<>();
                        cmdMsg.put("cmd", "stop");
                        CommonTools.sendMsg(carIp, chooseCarNo, cmdMsg);
                    } else {
                        //Author：杨超
                        //UpDate：2021-07-19
//                        float x = touchEvent.getPointerPosition(1).getX();
//                        float y = touchEvent.getPointerPosition(1).getY();
//                        mBtnCamera.setText(""+x+"     "+y);
                        CommonTools.showConfirmTips(CarHandleAbilitySlice.this, "car's ip error!");
                    }
                }
                return true;
            }

        });
        //相机打开,页面跳转
        //Author：杨超
        //UpDate：2021-07-19
        mBtnCameraOpen.setTouchEventListener(new Component.TouchEventListener() {
            @Override
            public boolean onTouchEvent(Component component, TouchEvent touchEvent) {

                Intent CameraIntent = new Intent();
                CameraIntent.setElement(new ElementName(
                        "",
                        getBundleName(),
                        "com.example.greenfruit.TakePhotoAbility"
                ));
                startAbility(CameraIntent);
//                mBtnCamera.setText("相机打开");
                return false;
            }
        });

    }





    class GetCarGameOverInfo implements Runnable {
        @Override
        public void run() {
            try {
                //监听端口设为3000
                server_sock = new DatagramSocket(3000);
                recv_buffer = new byte[5];//接收缓冲区，byte型
                pac = new DatagramPacket(recv_buffer, recv_buffer.length);//构造一个packet
                recv_string = "";

                while (true)//循环接受数据
                {
                    server_sock.receive(pac);//阻塞式接收数据
                    //将byte[]转化成string
                    recv_string = new String(recv_buffer, 0, pac.getLength());
                    System.out.println("接受到upd数据：" +recv_string);
//                    System.out.println(Arrays.toString(recv_buffer));
                    singleKvStore.putString("gameover",recv_string);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        switch (keyCode) {
            case KeyEvent.KEY_HOME:
                return true;
            case KeyEvent.KEY_BACK:
                return true;
            case KeyEvent.KEY_CALL:
                return true;
            case KeyEvent.KEY_SYM:
                return true;
            case KeyEvent.KEY_VOLUME_DOWN:
                return true;
            case KeyEvent.KEY_VOLUME_UP:
                return true;
            case KeyEvent.KEY_STAR:
                return true;
        }
        return super.onKeyDown(keyCode, keyEvent);
    }

    public void showGameOverConfirmTips(AbilityContext context, String msg) {
        CommonDialog toastDialog = new CommonDialog(context);
        Component rootView = LayoutScatter.getInstance(context)
                .parse(ResourceTable.Layout_dialog, null, false);
        Text text = (Text) rootView.findComponentById(ResourceTable.Id_toast_content);
        Text confirm = (Text) rootView.findComponentById(ResourceTable.Id_toast_confirm);
        text.setText(msg);
        confirm.setClickedListener(c -> {
            singleKvStore.delete("gameover");
            count = 0;
            toastDialog.remove();
        });
        toastDialog.setSize(MATCH_PARENT, MATCH_CONTENT);
        toastDialog.setAlignment(LayoutAlignment.CENTER);
        toastDialog.setTransparent(true);
        toastDialog.setCornerRadius(15);
        toastDialog.setContentCustomComponent(rootView);
        toastDialog.show();
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
