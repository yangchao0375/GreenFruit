/*Author：杨超
Date：2021-07-09
Update:2021-07-10
Project：绿小果集群小车
Version：1.1.1
 */

package com.example.greenfruit.slice;

import com.example.greenfruit.ResourceTable;
import com.example.greenfruit.uitls.CommonTools;
import com.example.greenfruit.uitls.DBUtils;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.*;
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

import java.util.ArrayList;
import java.util.List;

import static ohos.agp.components.ComponentContainer.LayoutConfig.MATCH_CONTENT;
import static ohos.agp.components.ComponentContainer.LayoutConfig.MATCH_PARENT;


public class ChooseCarAbilitySlice extends AbilitySlice {
    private Button chooseCarBtn1, chooseCarBtn2;
    private PageSlider view_pager;
    private List<Component> pageviews;
    private SingleKvStore singleKvStore = null;
    String car001 = "";
    String car002 = "";
    String loaclDeviceId = "";
    boolean playModule = false;



    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        getWindow().addFlags(WindowManager.LayoutConfig.MARK_TRANSLUCENT_STATUS);
        super.setUIContent(ResourceTable.Layout_ability_choose_car);
        loaclDeviceId = CommonTools.getLocalDeviceId(this);
        System.out.println("loaclDeviceId:" + loaclDeviceId);
        if (intent != null) playModule = intent.getBooleanParam("playModule", false);


        view_pager = (PageSlider) findComponentById(ResourceTable.Id_pager_slider);
        LayoutScatter dc = LayoutScatter.getInstance(getContext());
        DependentLayout view0 = (DependentLayout) dc.parse(ResourceTable.Layout_layout_page_0, null, false);
        DependentLayout view1 = (DependentLayout) dc.parse(ResourceTable.Layout_layout_page_1, null, false);
        pageviews = new ArrayList<Component>();

        if (playModule) {
            //获取数据库
            singleKvStore = DBUtils.initKvStore(this, "raceDB");
            //数据库里的数据变化监听
            singleKvStore.subscribe(SubscribeType.SUBSCRIBE_TYPE_REMOTE, new KvStoreObserver() {
                @Override
                public void onChange(ChangeNotification changeNotification) {
                    //刷新页面上的数据，同样有一个坑，onChange方法实质上，在一个子线程里执行
                    getUITaskDispatcher().asyncDispatch(new Runnable() {
                        @Override
                        public void run() {
                            //在这里执行页面ui组件的显示刷新
                            String msg = "---";
                            List<Entry> insertEntries = changeNotification.getInsertEntries();
                            for (Entry insertEntrie : insertEntries) {
                                msg = insertEntrie.getKey();
                            }

                            if(!"---".equals(msg) && CommonTools.isForeground(ChooseCarAbilitySlice.this)){
                                //setWifishowConfirmTips(ChooseCarAbilitySlice.this,"对手已经选择"+msg+"号车！"+ view_pager.getName() + ", "+ view_pager.getCurrentPage());
                                CommonTools.showTip(ChooseCarAbilitySlice.this,"对手选择了"+msg+"号小车！",2000);
                                if(msg.equals("car001") && view_pager.getCurrentPage()==0) {
                                    view_pager.setCurrentPage(1);
                                }else if(msg.equals("car002") && view_pager.getCurrentPage()==1){
                                    view_pager.setCurrentPage(0);
                                }

                            }
                        }
                    });
                }
            });

            try {
                car001 = singleKvStore.getString("car001");
                System.out.println("读取到car001：" + car001);
            } catch (Exception e) {
                System.out.println("读取到car001error");
            }
            try {
                car002 = singleKvStore.getString("car002");
                System.out.println("读取到car002：" + car002);
            } catch (Exception e) {
                System.out.println("读取到car002error");
            }
            //读写数据库里的car001和car002
            if ("".equals(car001) && "".equals(car002)) {
                //将view装入数组
                pageviews.add(view0);
                pageviews.add(view1);
            } else if (car001.equals(loaclDeviceId)) {
                pageviews.clear();
                pageviews.add(view0);
            } else if (car002.equals(loaclDeviceId)) {
                pageviews.clear();
                pageviews.add(view1);
            } else if (!car001.equals(loaclDeviceId) && !"".equals(car001)) {
                pageviews.clear();
                pageviews.add(view1);
            } else if (!car002.equals(loaclDeviceId) && !"".equals(car002)) {
                pageviews.clear();
                pageviews.add(view0);
            }
        } else {
            //将view装入数组
            pageviews.add(view0);
            pageviews.add(view1);
        }

        chooseCarBtn1 = (Button) view0.findComponentById(ResourceTable.Id_choose_car_btn_1);
        chooseCarBtn1.setClickedListener(component -> {
            if (playModule) {
                singleKvStore.putString("car001", loaclDeviceId);
                System.out.println("写入car001：" + loaclDeviceId);
            }
            //跳转setwifi
            Intent setwifiIntent = new Intent();
            setwifiIntent.setElement(new ElementName(
                    "",
                    "com.example.greenfruit",
                    "com.example.greenfruit.SetCarWifiAbility"
            ));
            setwifiIntent.setParam("chooseCarNo", 001);
            startAbility(setwifiIntent);
        });
        chooseCarBtn2 = (Button) view1.findComponentById(ResourceTable.Id_choose_car_btn_2);
        chooseCarBtn2.setClickedListener(component -> {
            if (playModule) {
                singleKvStore.putString("car002", loaclDeviceId);
                System.out.println("写入car002：" + loaclDeviceId);
            }
            //跳转setwifi
            Intent setwifiIntent = new Intent();
            setwifiIntent.setElement(new ElementName(
                    "",
                    "com.example.greenfruit",
                    "com.example.greenfruit.SetCarWifiAbility"
            ));
            setwifiIntent.setParam("chooseCarNo", 002);
            startAbility(setwifiIntent);
        });
        flushPageSlider();
    }

    private void flushPageSlider() {
        //绑定适配器
        view_pager.setProvider(new PageSliderProvider() {
            @Override
            //获取当前窗体界面数
            public int getCount() {
                return pageviews.size();
            }

            //返回一个对象，这个对象表明了PagerAdapter适配器选择哪个对象放在当前的ViewPager中
            @Override
            public Object createPageInContainer(ComponentContainer componentContainer, int i) {
                componentContainer.addComponent(pageviews.get(i));
                return pageviews.get(i);
            }

            //是从ViewGroup中移出当前View
            @Override
            public void destroyPageFromContainer(ComponentContainer componentContainer, int i, Object o) {
                componentContainer.removeComponent(pageviews.get(i));
            }

            //断是否由对象生成界面
            @Override
            public boolean isPageMatchToObject(Component component, Object o) {
                return component == o;
            }
        });
    }

    public void setWifishowConfirmTips(AbilityContext context, String msg) {
        CommonDialog toastDialog = new CommonDialog(context);
        Component rootView = LayoutScatter.getInstance(context)
                .parse(ResourceTable.Layout_dialog, null, false);
        Text text = (Text) rootView.findComponentById(ResourceTable.Id_toast_content);
        Text confirm = (Text) rootView.findComponentById(ResourceTable.Id_toast_confirm);
        text.setText(msg);
        confirm.setClickedListener(c -> {
            toastDialog.hide();
            if (playModule) {
                singleKvStore.delete("car001");
                singleKvStore.delete("car002");
            }
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
