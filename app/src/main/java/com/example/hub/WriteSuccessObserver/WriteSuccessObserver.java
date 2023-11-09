/**
 *
 * @ProjectName: Hub
 * @Package: com.example.hub.WriteSuccessObserver
 * @ClassName: WriteSuccessObserver
 * @Description: observer to judge whether write is successful
 * @Author: GRP_Team14
 * @CreateDate: 2022/3/14
 * @Version: 1.0
 */

package com.example.hub.WriteSuccessObserver;

import com.clj.fastble.BleManager;

import java.util.Observable;
import java.util.Observer;

public class WriteSuccessObserver implements Observer {

    @Override
    public void update(Observable o, Object arg) {
        if ((Boolean)arg) {
            BleManager.getInstance().disconnectAllDevice();
            BleManager.getInstance().destroy();
        }
    }
}
