package com.prepod.usbinf.helpers;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.ContentHandler;
import java.util.HashMap;

public class DBIds {

    private Context context;

    public DBIds(Context context){
        this.context = context;
    }

    public HashMap<String, HashMap<String, String>> getDB(){
        String text = "usb.ids";
        byte[] buffer = null;
        InputStream is;
        try {
            is = context.getAssets().open(text);
            int size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        HashMap<String, HashMap<String, String>> vendors = new HashMap<>();
        HashMap<String, String> products = new HashMap<>();
        String[] vendorStrArr = {};
        String[] productStrArr;
        int vid = 0;
        String currentVid = "";

        String str_data = new String(buffer);
        String[] tmp = str_data.split("# List of known device classes");
        tmp = tmp[0].split("\n");
        for (int i=0; i < tmp.length; i++){
            if (!tmp[i].startsWith("#") && !tmp[i].equals("")){
                if (!tmp[i].startsWith("\t")){

                    vendorStrArr = tmp[i].split("  ");
                    if (!currentVid.equals("") && !currentVid.equals(vendorStrArr[0])) {
                        vendors.put(currentVid, products);
                    }
                    currentVid = vendorStrArr[0];
                    products = new HashMap<>();
                    products.put("name", vendorStrArr[1]);
                    Log.v("My", "" + vendorStrArr);
                } else {
                    if (!tmp[i].startsWith("\t\t")) {
                        productStrArr = tmp[i].split("\t");
                        productStrArr = productStrArr[1].split("  ");
                        try {
                            products.put(productStrArr[0], productStrArr[1]);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            Log.e("My", "" + e);
                        }
                    } else {

                    }
                }
            }
            vendors.put(currentVid, products);
        }

        return vendors;
    }
}
