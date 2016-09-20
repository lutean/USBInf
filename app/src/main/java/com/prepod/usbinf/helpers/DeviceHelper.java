package com.prepod.usbinf.helpers;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.prepod.usbinf.Consts;
import com.prepod.usbinf.DeviceInfo;
import com.prepod.usbinf.UsbInf;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class DeviceHelper {

    public static String[] getInfo(Context context, UsbDevice device){
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        if (manager.hasPermission(device)) {
            UsbInterface usbInterface = device.getInterface(0);
            UsbDeviceConnection connection = manager.openDevice(device);
            if(connection != null) {
                connection.claimInterface(usbInterface, true);
                byte[] descriptor = connection.getRawDescriptors();
                byte[] buffer = new byte[255];
                int indexManufacturer = descriptor[14] & 0xff;
                int indexProduct = descriptor[15] & 0xff;
                int indexSerialNumber = descriptor[16] & 0xff;
                HashMap<String, HashMap<String, String>> vendors = UsbInf.getInstance().getVendrors();
                String vidStr = normilize(Integer.toHexString((0xff &  descriptor[8]) | (0xff & descriptor[9]) << 8));
                String pidStr = normilize(Integer.toHexString((0xff & descriptor[10]) | (0xff & descriptor[11]) << 8));
                String vendorName = "";
                String productName = "";

                if (vidStr!= null && !vidStr.equals("")) {
                    HashMap<String, String> vendor = vendors.get(vidStr);
                    if (vendor != null && pidStr != null && !pidStr.equals("")){
                        vendorName = vendor.get("name");
                        productName = vendor.get(pidStr);
                        if (productName == null){
                            productName = "";
                        }
                    }
                } else {
                    vidStr = "";
                }
                vidStr = "Vendor ID: " + vidStr + " (" + vendorName + ")";
                pidStr = "Product ID: " +  pidStr + " (" + productName + ")";;
                String manufacturer = "Manufacturer: " + getStringDescriptor(connection, indexManufacturer);
                String product = "Product: " + getStringDescriptor(connection, indexProduct);
                String serial = "Serial Number: ";
                if (indexSerialNumber != 0) {
                    serial += getStringDescriptor(connection, indexSerialNumber);
                } else serial += "empty";
                connection.releaseInterface(usbInterface);
                connection.close();

                Log.v("My", "" + manufacturer + " " + product);
                String[] info = {vidStr, pidStr, manufacturer, product, serial};
               return info;
            }
        }
        return null;
    }

    private static String getStringDescriptor(UsbDeviceConnection connection, int index){
        byte[] buffer = new byte[255];
        String result = "empty";
        int rdo = connection.controlTransfer(UsbConstants.USB_DIR_IN
                , Consts.STD_USB_REQUEST_GET_DESCRIPTOR,
                (Consts.USB_STRING << 8) | index, 0, buffer, 0xFF, 0);
        if (rdo >= 0 ) {
            try {
                result = new String(buffer, 2, rdo - 2, "UTF-16LE");
                Log.v("My", result);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private static String normilize(String str){
        String result = "";
        switch (str.length()){
            case 1:
                result = "000" + str;
                break;
            case 2:
                result = "00" + str;
                break;
            case 3:
                result = "0" + str;
                break;
            case 4:
                result = str;
                break;
        }

        return result;
    }


}
