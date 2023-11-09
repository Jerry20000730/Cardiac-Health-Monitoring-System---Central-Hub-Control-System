/**
 *
 * @ProjectName: Hub
 * @Package: com.example.hub.isInitialBindingSuccess
 * @ClassName: BindingSuccessObservable
 * @Description: observer to judge whether the binding process is successful
 * @Author: GRP_Team14
 * @CreateDate: 2022/3/14
 * @Version: 1.0
 */

package com.example.hub.isInitialBindingSuccess;

import com.example.hub.MainActivity;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

public class BindingSuccessObserver implements Observer {

    private MainActivity mainActivity;

    public BindingSuccessObserver(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void update(Observable o, Object arg) {
        if ((boolean)arg) {
            try {
                mainActivity.enableSettingSensorList();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mainActivity.enableBindingSensorNames();
            mainActivity.enableSocketAsyncTask();
        }

    }
}
