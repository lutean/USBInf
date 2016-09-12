package com.prepod.usbinf.fragments;

import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.prepod.usbinf.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UsbDetails extends Fragment {

    ListView deviceListView;
    ArrayAdapter<String> adapter;
    List<String> uniList = new ArrayList<>();

    public UsbDetails() {
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

    }
}
