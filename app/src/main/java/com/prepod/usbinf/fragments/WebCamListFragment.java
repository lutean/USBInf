package com.prepod.usbinf.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.usb.UsbDevice;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.prepod.usbinf.CameraActivity;
import com.prepod.usbinf.R;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class WebCamListFragment extends Fragment {

    String[] camerasIds;
    List<String> cameraList = new ArrayList<>();

    ListView deviceListView;
    ArrayAdapter<String> adapter;


    public WebCamListFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_webcam_list, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        deviceListView = (ListView) getView().findViewById(R.id.usbList);
        adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item, cameraList);
        deviceListView.setAdapter(adapter);

        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), CameraActivity.class);
                intent.putExtra("cameraId", camerasIds[position]);
                startActivity(intent);
            }
        });

        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            try {
                camerasIds = manager.getCameraIdList();
                Log.v("My", "" + camerasIds);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (camerasIds != null) {
                for (int i = 0; i < camerasIds.length; i++) {
                    CameraCharacteristics chr = null;
                    try {
                        chr = manager.getCameraCharacteristics(camerasIds[i]);
                        switch (chr.get(CameraCharacteristics.LENS_FACING)){
                            case CameraCharacteristics.LENS_FACING_BACK:
                                cameraList.add("Camera " + i + " back");
                                break;
                            case CameraCharacteristics.LENS_FACING_FRONT:
                                cameraList.add("Camera " + i + " front");
                                break;
                            case CameraCharacteristics.LENS_FACING_EXTERNAL:
                                cameraList.add("Camera " + i + " external");
                                break;
                        }
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            }

            adapter.notifyDataSetChanged();

        }

    }
}