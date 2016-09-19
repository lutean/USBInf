package com.prepod.usbinf.fragments;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.prepod.usbinf.Consts;
import com.prepod.usbinf.DeviceInfo;
import com.prepod.usbinf.R;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class UsbListFragment extends Fragment {

    ListView deviceListView;
    ArrayAdapter<String> adapter;
    List<String> uniList = new ArrayList<>();
    HashMap<String, UsbDevice> deviceMap;
    List<UsbDevice> deviceList = new ArrayList<>();
    private PendingIntent permissionIntent;
    private UsbManager manager;

    public UsbListFragment() {
        super();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_usb_list, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        deviceListView = (ListView) getView().findViewById(R.id.usbList);
        adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item, uniList);
        deviceListView.setAdapter(adapter);


        IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        getActivity().registerReceiver(usbAttachReceiver , filter);
        filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
        getActivity().registerReceiver(usbDetachReceiver , filter);
        permissionIntent = PendingIntent.getBroadcast(getActivity(), 0, new Intent(ACTION_USB_PERMISSION), 0);
        filter = new IntentFilter(ACTION_USB_PERMISSION);
        getActivity().registerReceiver(usbReceiver, filter);



        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               /*Intent intent = new Intent(getActivity(), DeviceInfo.class);
                intent.putExtra("info", getDeviceInfo(deviceList.get(position)));
                getActivity().startActivity(intent);*/
                if (!manager.hasPermission(deviceList.get(position))) {
                    requestDevicePermission(deviceList.get(position));
                }else {
                    connectToDevice(deviceList.get(position));
                }
            }
        });
        getDevices();

    }

    private String[] getDeviceInfo(UsbDevice device){
        if (device != null) {
            String devClass = "Device class: " + device.getDeviceClass();
            String devName = "Device name: " + device.getDeviceName();
            String devId = "Device id: " + device.getDeviceId();
            String devProtocol = "Device Protocol: " + device.getDeviceProtocol();
            String devSubClass = "Device Subclass: " + device.getDeviceSubclass();
            String prodId = "Product Id: " + device.getProductId();
            String vendorId = "Vendor Id: " + device.getVendorId();
            String prodName = "Product Name: ";
            String manName = "Manufacturer Name: ";
            String serNumber = "Serial Number: ";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                prodName = "Product Name: " + device.getProductName();
                manName = "Manufacturer Name: " + device.getManufacturerName();
                serNumber = "Serial Number: " + device.getSerialNumber();
            }

            String[] info = {devClass, devName, devId, devProtocol, devSubClass, prodId, vendorId, prodName, manName, serNumber };
            return info;
        }
        return null;
    }

    private void connectToDevice(UsbDevice device){
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
                int indexSerialNumber = descriptor[16];
                String vidStr = "Vendor ID: ";
                String pidStr = "Product ID: ";
                String manufacturer = "Manufacturer: " + getStringDescriptor(connection, indexManufacturer);
                String product = "Product: " + getStringDescriptor(connection, indexProduct);
                String serial = "";
                if (indexSerialNumber != 0) {
                    serial = "Serial Number: " + getStringDescriptor(connection, indexSerialNumber);
                }

                int vid = (0xff &  descriptor[8]) |  (0xff & descriptor[9]) << 8;
                int pid = (0xff &  descriptor[10]) | (0xff & descriptor[11]) << 8;

                vidStr += Integer.toHexString(vid);
                pidStr += Integer.toHexString(pid);

/*
                int rdo = connection.controlTransfer(UsbConstants.USB_DIR_IN
                                , Consts.STD_USB_REQUEST_GET_DESCRIPTOR,
                        (Consts.USB_STRING << 8) | indexManufacturer, 0, buffer, 0xFF, 0);
                if (rdo >= 0 )
                try {
                    manufacturer += new String(buffer, 2, rdo - 2, "UTF-16LE");
                    Log.v("My", manufacturer);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                rdo = connection.controlTransfer(UsbConstants.USB_DIR_IN
                                , Consts.STD_USB_REQUEST_GET_DESCRIPTOR,
                        (Consts.USB_STRING << 8) | indexProduct, 0, buffer, 0xFF, 0);
                if (rdo >= 0 )
                try {
                    product += new String(buffer, 2, rdo - 2, "UTF-16LE");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (indexSerialNumber != 0 ) {
                    rdo = connection.controlTransfer(UsbConstants.USB_DIR_IN
                            , Consts.STD_USB_REQUEST_GET_DESCRIPTOR,
                            (Consts.USB_STRING << 8) | indexSerialNumber, 0, buffer, 0xFF, 0);
                    if (rdo >= 0)
                        try {
                            serial += new String(buffer, 2, rdo - 2, "UTF-16LE");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                } else serial += "none";*/

                connection.releaseInterface(usbInterface);
                connection.close();

                Log.v("My", "" + manufacturer + " " + product);
                String[] info = {vidStr, pidStr, manufacturer, product, serial};
                Intent intent = new Intent(getActivity(), DeviceInfo.class);
                intent.putExtra("info", info);
                getActivity().startActivity(intent);

            }
        } else {
            Toast.makeText(getActivity(),"No permission!", Toast.LENGTH_SHORT).show();
        }

    }

    private String getStringDescriptor(UsbDeviceConnection connection, int index){
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

    private void getDevices(){
        uniList.clear();
        deviceList.clear();
        manager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
        deviceMap = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceMap.values().iterator();
        while(deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            deviceList.add(device);
            if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP) {
                uniList.add(device.getManufacturerName() + " " + device.getProductName());
            } else {
                uniList.add(device.getDeviceName());
            }
        }
        adapter.notifyDataSetChanged();
        Log.v("My", " " + deviceMap.keySet());
    }

    BroadcastReceiver usbAttachReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                getDevices();
            }
        }
    };

    BroadcastReceiver usbDetachReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    getDevices();
                }
            }
        }
    };

    private void requestDevicePermission(UsbDevice device){
        if (device != null) {
            manager.requestPermission(device, permissionIntent);
           // boolean hasPermision = manager.hasPermission(device);
        }

    }

    private static final String ACTION_USB_PERMISSION = "com.prepod.USB_PERMISSION";
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                           connectToDevice(device);
                        }
                    } else {
                        // Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };

}
