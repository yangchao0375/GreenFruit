/*Author：杨超
Date：2021-07-09
Update:2021-07-10
Project：绿小果集群小车
Version：1.1.1
 */

package com.example.greenfruit;

import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.app.Environment;
import ohos.global.resource.Resource;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.common.Source;
import ohos.media.player.Player;
import ohos.rpc.IRemoteObject;

import java.io.*;

public class BJmusicServiceAbility extends Ability {
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, 0xD001100, "Demo");
    private static Source sVideoSource;
    public Player sPlayer = null;
    public FileDescriptor fileDescriptor = null;
    @Override
    public void onStart(Intent intent) {
        HiLog.error(LABEL_LOG, "BJmusicServiceAbility::onStart");
        super.onStart(intent);

        sPlayer = new Player(BJmusicServiceAbility.this);
        new PlayerThread().start();
    }

    class PlayerThread extends Thread {
        @Override
        public void run() {
            try {
                File mp3FilePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
                if (!mp3FilePath.exists()) {
                    mp3FilePath.mkdirs();
                }
                File mp3File = new File(mp3FilePath.getAbsolutePath() + "/" + "bj.mp3");

                Resource res = getResourceManager()
                        .getRawFileEntry("resources/rawfile/bj.mp3").openRawFile();
                byte[] buf = new byte[4096];
                int count = 0;
                FileOutputStream fos = new FileOutputStream(mp3File);
                while ((count = res.read(buf)) != -1) {
                    fos.write(buf, 0, count);
                }
                fileDescriptor = new FileInputStream(mp3File).getFD();
                sVideoSource = new Source(fileDescriptor);
                sPlayer.setSource(sVideoSource);
                sPlayer.prepare();
                sPlayer.setVolume(0.3f);
                sPlayer.enableSingleLooping(true);
                sPlayer.play();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackground() {
        super.onBackground();
        HiLog.info(LABEL_LOG, "BJmusicServiceAbility::onBackground");
    }

    @Override
    public void onStop() {
        super.onStop();
        sPlayer.stop();
        HiLog.info(LABEL_LOG, "BJmusicServiceAbility::onStop");
    }

    @Override
    public void onCommand(Intent intent, boolean restart, int startId) {
    }

    @Override
    public IRemoteObject onConnect(Intent intent) {
        return null;
    }

    @Override
    public void onDisconnect(Intent intent) {
    }
}