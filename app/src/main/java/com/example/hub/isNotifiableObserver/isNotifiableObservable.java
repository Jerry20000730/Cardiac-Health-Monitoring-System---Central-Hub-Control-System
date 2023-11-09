/**
 *
 * @ProjectName: Hub
 * @Package: com.example.hub.isNotifiableObservable
 * @ClassName: isNotifiableObservable
 * @Description: observerable to judge when to notify
 * @Author: GRP_Team14
 * @CreateDate: 2022/3/14
 * @Version: 1.0
 */

package com.example.hub.isNotifiableObserver;

import java.util.Observable;

public class isNotifiableObservable extends Observable {

    // variable to be observed
    private Boolean isNotifiable;

    /**
     * Constructor of observable class
     * By default, the isNotifiable Variable is false
     */
    public isNotifiableObservable() {
        this.isNotifiable = false;
    }

    /**
     * Constructor of observable class
     *
     * @param isNotifySuccess the status of isWriteSuccess, true if is already capable of notifying, false otherwise
     */
    public isNotifiableObservable(boolean isNotifySuccess) {
        this.isNotifiable = isNotifySuccess;
    }

    /**
     * Getter function to know whether the written process is successful
     *
     * @return the boolean value, true if it is already capable of notifying, false otherwise
     */
    public boolean getIsNotifiable() {
        return isNotifiable;
    }

    /**
     * Setter function to set whether it is capable to notify
     *
     * @param isAbleNotify the boolean value, true if is already capable of notifying, false otherwise
     */
    public void setIsNotifiable(boolean isAbleNotify) {
        if (isNotifiable != isAbleNotify) {
            setChanged();
            notifyObservers(isAbleNotify);
        }
        isNotifiable = isAbleNotify;
    }
}
