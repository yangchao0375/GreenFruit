package com.example.greenfruit;

import com.example.greenfruit.slice.startAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;

public class startAbility extends Ability {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(startAbilitySlice.class.getName());
    }
}
