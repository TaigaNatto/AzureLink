package com.example.robop;

/**
 * Created by taiga on 2016/10/25.
 */

public class keyManager {

    @com.google.gson.annotations.SerializedName("id")
    private String mId;

    @com.google.gson.annotations.SerializedName("master")
    private boolean mMaster=false;

    //master取得
    public boolean isMaster() {
        return mMaster;
    }

    public void setMaster(boolean master) {
        mMaster = master;
    }

    public String getId() {
        return mId;
    }

    public final void setId(String id) {
        mId = id;
    }
}
