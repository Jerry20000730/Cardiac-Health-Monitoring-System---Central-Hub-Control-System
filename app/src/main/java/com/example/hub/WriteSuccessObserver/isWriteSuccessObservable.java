/**
 *
 * @ProjectName: Hub
 * @Package: com.example.hub.WriteSuccessObserve
 * @ClassName: isWriteSuccessObservable
 * @Description: observerable to judge whether the written is successful
 * @Author: GRP_Team14
 * @CreateDate: 2022/3/14
 * @Version: 1.0
 */

package com.example.hub.WriteSuccessObserver;

import java.util.Observable;

/**
 * class of observable for observer to observe
 * implement observer design pattern
 */
public class isWriteSuccessObservable extends Observable {
    // variable to be observed
    private Boolean isWriteSuccess;

    /**
     * Constructor of observable class
     * By default, the isWriteSuccess Variable is false
     */
    public isWriteSuccessObservable() {
        this.isWriteSuccess = false;
    }

    /**
     * Constructor of observable class
     * @param isNotifySuccess the status of isWriteSuccess, true if is already written, false otherwise
     */
    public isWriteSuccessObservable(boolean isNotifySuccess) {
        this.isWriteSuccess = isNotifySuccess;
    }

    /**
     * Getter function to know whether the written process is successful
     * @return the boolean value, true if the information is already written, false otherwise
     */
    public boolean getWriteSuccess() {
        return isWriteSuccess;
    }

    /**
     * Setter function to set whether the written process is successful or not
     * @param writeSuccess the boolean value, true if the information is already written, false otherwise
     */
    public void setWriteSuccess(boolean writeSuccess) {
        if (isWriteSuccess != writeSuccess) {
            setChanged();
            notifyObservers(writeSuccess);
        }
        isWriteSuccess = writeSuccess;
    }
}
