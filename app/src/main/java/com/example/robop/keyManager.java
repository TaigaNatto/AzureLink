package com.example.robop;

/**
 * Created by taiga on 2016/10/25.
 */

public class keyManager {

    @com.google.gson.annotations.SerializedName("id")
    private String mId;

    @com.google.gson.annotations.SerializedName("master")
    private boolean mMaster=false;

    @com.google.gson.annotations.SerializedName("keyFlag")
    private boolean mkeyFlag=false;


    @com.google.gson.annotations.SerializedName("complete")
    private boolean mComplete;

    //master取得
    public boolean isMaster() {
        return mMaster;
    }

    public void setMaster(boolean master) {
        mMaster = master;
    }

    //keyFlag
    public boolean isKeyFlag() {
        return mkeyFlag;
    }

    public void setKeyFlag(boolean keyFlag) {
        mkeyFlag = keyFlag;
    }

    public String getId() {
        return mId;
    }

    public final void setId(String id) {
        mId = id;
    }

    /**
     * Indicates if the item is marked as completed
     */
    public boolean isComplete() {
        return mComplete;
    }

    /**
     * Marks the item as completed or incompleted
     */
    public void setComplete(boolean complete) {
        mComplete = complete;
    }


}
