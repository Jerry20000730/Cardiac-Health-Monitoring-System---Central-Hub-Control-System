/**
 *
 * @ProjectName: Hub
 * @Package: com.example.hub.isInitialBindingSuccess
 * @ClassName: isInitialBindingSuccessObservable
 * @Description: observerable to judge whether the initial binding is successful
 * @Author: GRP_Team14
 * @CreateDate: 2022/3/14
 * @Version: 1.0
 */

package com.example.hub.isInitialBindingSuccess;

import com.example.hub.DeviceManager.DeviceManager;

import java.util.Observable;

public class isInitialBindingSuccessObservable extends Observable {

    // variable to be observed
    private boolean isInitialBindingSuccess;
    private static isInitialBindingSuccessObservable isInitialBindingSuccessObservableInstance = new isInitialBindingSuccessObservable();

    private isInitialBindingSuccessObservable() {
        this.isInitialBindingSuccess = false;
    }

    /**
     * Singleton design pattern
     * @return isInitialBindingSuccessObservable
     */
    public static isInitialBindingSuccessObservable getInstance(){
        return isInitialBindingSuccessObservableInstance;
    }

    /**
     * Constructor of observable class
     *
     * @param isInitialBindingSuccess the status of isWriteSuccess, true if is already paired with Edge, false otherwise
     */
    public isInitialBindingSuccessObservable(boolean isInitialBindingSuccess) {
        this.isInitialBindingSuccess = isInitialBindingSuccess;
    }

    /**
     * Getter function to know whether the written process is successful
     *
     * @return the boolean value, true if it is already paired with Edge, false otherwise
     */
    public boolean getisInitialBindingSuccess() {
        return isInitialBindingSuccess;
    }

    /**
     * Setter function to set whether it is capable to notify
     *
     * @param isAbleBinding the boolean value, true if is already paired with Edge, false otherwise
     */
    public void setIsInitialBindingSuccess(boolean isAbleBinding) {
        if (isInitialBindingSuccess != isAbleBinding) {
            setChanged();
            notifyObservers(isAbleBinding);
        }
        isInitialBindingSuccess = isAbleBinding;
    }


}
