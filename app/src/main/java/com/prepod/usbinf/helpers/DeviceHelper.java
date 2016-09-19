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

import java.io.UnsupportedEncodingException;

public class DeviceHelper {

    public static void connectToDevice(Context context, UsbDevice device){
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        if (manager.hasPermission(device)) {
            UsbInterface usbInterface = device.getInterface(0);
            int endPonitCount = usbInterface.getEndpointCount();
            UsbDeviceConnection connection = manager.openDevice(device);
            if(connection != null) {
                connection.claimInterface(usbInterface, true);
                byte[] descriptor = connection.getRawDescriptors();
                byte[] buffer = new byte[255];
                int indexManufacturer = descriptor[14] & 0xff;
                int indexProduct = descriptor[15] & 0xff;
                int indexSerialNumber = descriptor[16] & 0xff;
                String vidStr = "Vendor ID: " + Integer.toHexString((0xff &  descriptor[8]) | (0xff & descriptor[9]) << 8);
                String pidStr = "Product ID: " +  Integer.toHexString((0xff & descriptor[10]) | (0xff & descriptor[11]) << 8);
                String manufacturer = "Manufacturer: " + getStringDescriptor(connection, indexManufacturer);
                String product = "Product: " + getStringDescriptor(connection, indexProduct);
                String serial = "";
                if (indexSerialNumber != 0) {
                    serial = "Serial Number: " + getStringDescriptor(connection, indexSerialNumber);
                }

                connection.releaseInterface(usbInterface);
                connection.close();

                Log.v("My", "" + manufacturer + " " + product);
                String[] info = {vidStr, pidStr, manufacturer, product, serial};
                Intent intent = new Intent(context, DeviceInfo.class);
                intent.putExtra("info", info);
                context.startActivity(intent);
            }
        } else {
            Toast.makeText(context,"No permission!", Toast.LENGTH_SHORT).show();
        }
    }

    private static String getStringDescriptor(UsbDeviceConnection connection, int index){
        byte[] buffer = new byte[255];
        String result = "";
        int rdo = connection.controlTransfer(UsbConstants.USB_DIR_IN
                , Consts.STD_USB_REQUEST_GET_DESCRIPTOR,
                (Consts.USB_STRING << 8) | index, 0, buffer, 0xFF, 0);
        if (rdo >= 0 )
            try {
                result = new String(buffer, 2, rdo - 2, "UTF-16LE");
                Log.v("My", result);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        return result;
    }


}
