/*
 * Author:曹涛
 * Date:2021/7/5
 * Update:2021/7/6
 */

package com.example.imagetest;

import com.example.imagetest.slice.MainAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;

public class MainAbility extends Ability {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(MainAbilitySlice.class.getName());
    }
}
