package com.prepod.usbinf.fragments;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
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

import com.prepod.usbinf.DeviceInfo;
import com.prepod.usbinf.R;

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

        final UsbManager manager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);

        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), DeviceInfo.class);
                intent.putExtra("info", getDeviceInfo(deviceList.get(position)));
                getActivity().startActivity(intent);
            }
        });
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

}
