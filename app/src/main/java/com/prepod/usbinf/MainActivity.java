package com.prepod.usbinf;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.prepod.usbinf.adapter.CustomDrawerAdapter;
import com.prepod.usbinf.adapter.DrawerItem;
import com.prepod.usbinf.fragments.UsbListFragment;
import com.prepod.usbinf.fragments.WebCamListFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private  CustomDrawerAdapter adapter;
    private String[] mDrawerTitles;
    private TextView titleToolbar;
    private PendingIntent permissionIntent;

    private List<DrawerItem> dataList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(null);

        Toolbar topToolBar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(topToolBar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        titleToolbar = (TextView) findViewById(R.id.textTitleToolbar);
        titleToolbar.setText("Connected devices");

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        LinearLayout drawerBtn = (LinearLayout) findViewById(R.id.drawer_btn);
        drawerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        dataList.add(new DrawerItem(getResources().getString(R.string.usb_list), R.drawable.usb));
        dataList.add(new DrawerItem(getResources().getString(R.string.webcam_list), R.drawable.cam));

        mDrawerTitles = new String[]{"Connected devices"};

        adapter = new CustomDrawerAdapter(this, R.layout.drawe_list_item, dataList);
        mDrawerList.setAdapter(adapter);

        IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        registerReceiver(usbAttachReceiver , filter);
        filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbDetachReceiver , filter);
        permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, filter);
        showUsbList();
    }

    private void showUsbList() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, new UsbListFragment())
                .commit();
    }

    private void showWebCamList() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, new WebCamListFragment())
                .commit();
    }



    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {

            switch (position) {
                case 0:
                    showUsbList();
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                    break;
                case 1:
                    showWebCamList();
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                    break;
            }
        }
    }

    BroadcastReceiver usbAttachReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                showUsbList();
            }
        }
    };

    BroadcastReceiver usbDetachReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                  showUsbList();
                }
            }
        }
    };

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
                            showUsbList();
                        }
                    } else {
                        // Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };
}
