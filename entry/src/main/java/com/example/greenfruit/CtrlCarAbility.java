/*Author：杨超
Date：2021-07-09
Update:2021-07-10
Project：绿小果集群小车
Version：1.1.1
 */

package com.example.greenfruit;

import com.example.greenfruit.slice.CtrlCarAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;

public class CtrlCarAbility extends Ability {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(CtrlCarAbilitySlice.class.getName());
    }

}
