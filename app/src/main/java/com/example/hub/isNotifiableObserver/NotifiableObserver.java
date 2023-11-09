/**
 *
 * @ProjectName: Hub
 * @Package: com.example.hub.isNotifiableObserver
 * @ClassName: NotifiableObserver
 * @Description: observer to judge whether it is able to start notifying the BLE devices
 * @Author: GRP_Team14
 * @CreateDate: 2022/3/14
 * @Version: 1.0
 */

package com.example.hub.isNotifiableObserver;

import com.clj.fastble.BleManager;
import com.example.hub.MainActivity;

import java.util.Observable;
import java.util.Observer;

public class NotifiableObserver implements Observer {

    private MainActivity mainActivity;
    private String path;

    public NotifiableObserver(MainActivity mainActivity, String path) {
        this.mainActivity = mainActivity;
        this.path = path;
    }

    @Override
    public void update(Observable o, Object arg) {
        if ((Boolean)arg) {
            mainActivity.enableBLENotify(path);
        }
    }
}
