package com.prepod.usbinf;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Антон on 09.09.2016.
 */
public class DeviceInfo extends AppCompatActivity {

    private ListView deviceInfo;
    ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        setTitle(null);

        Toolbar topToolBar = (Toolbar) findViewById(R.id.toolbar_actionbar_back);
        setSupportActionBar(topToolBar);

        LinearLayout backBtn = (LinearLayout) findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent intent = getIntent();
        String[] info = intent.getStringArrayExtra("info");
        List<String> infoLsit = Arrays.asList(info);
        deviceInfo = (ListView) findViewById(R.id.deviceInfo);
        adapter = new ArrayAdapter<String>(this, R.layout.list_item, infoLsit);
        deviceInfo.setAdapter(adapter);

    }

}
