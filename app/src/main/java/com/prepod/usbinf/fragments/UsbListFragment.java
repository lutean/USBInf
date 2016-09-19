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

import android.widget.ListView;

import com.prepod.usbinf.R;
import com.prepod.usbinf.helpers.DeviceHelper;

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
                if (!manager.hasPermission(deviceList.get(position))) {
                    requestDevicePermission(deviceList.get(position));
                }else {
                    DeviceHelper.connectToDevice(getActivity(), deviceList.get(position));
                }
            }
        });
        getDevices();
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
                           DeviceHelper.connectToDevice(getActivity(), device);
                        }
                    } else {
                        // Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };

}
