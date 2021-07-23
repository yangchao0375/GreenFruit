/*Author：杨超
Update:2021-07-10
Date：2021-07-09
Project：绿小果集群小车
Version：1.1.1
 */

package com.example.greenfruit;

import com.example.greenfruit.slice.MainAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.bundle.IBundleManager;
import ohos.security.SystemPermission;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MainAbility extends Ability {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(MainAbilitySlice.class.getName());
        requestPermissions();
        //Author：杨超
        //Update:2021-07-19
        // 权限申请函数

    }
    private void requestPermissions() {
        String[] permissions = {
                SystemPermission.WRITE_USER_STORAGE, SystemPermission.READ_USER_STORAGE, SystemPermission.CAMERA,
                SystemPermission.MICROPHONE, SystemPermission.LOCATION,//主要申请相机权限，还有其他的位置，录音，存储等权限
                SystemPermission.DISTRIBUTED_DATASYNC//分布式数据库
        };
        List<String> permissionFiltered = Arrays.stream(permissions)
                .filter(permission -> verifySelfPermission(permission) != IBundleManager.PERMISSION_GRANTED)
                .collect(Collectors.toList());
        requestPermissionsFromUser(permissionFiltered.toArray(new String[permissionFiltered.size()]), 0);
    }

    @Override
    public void onRequestPermissionsFromUserResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissions == null || permissions.length == 0 || grantResults == null || grantResults.length == 0) {
            return;
        }
        for (int grantResult : grantResults) {
            if (grantResult != IBundleManager.PERMISSION_GRANTED) {
                terminateAbility();
                break;
            }
        }
    }
}
