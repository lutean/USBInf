package com.prepod.usbinf;

import android.app.Application;

import com.prepod.usbinf.helpers.DBIds;

import java.util.HashMap;

public class UsbInf extends Application {

    private static UsbInf mInstance;
    private HashMap<String, HashMap<String, String>> vendors;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized UsbInf getInstance() {
        return mInstance;
    }

    public HashMap<String, HashMap<String, String>> getVendrors() {
        if (vendors == null) {
            DBIds db = new DBIds(this);
            vendors = db.getDB();
        }
        return this.vendors;
    }

}
