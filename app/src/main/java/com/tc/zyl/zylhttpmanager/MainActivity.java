package com.tc.zyl.zylhttpmanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.tc.zyl.httpmanager.HttpManager;

public class MainActivity extends AppCompatActivity {


//    String url = "http://admin.kkptu.com/interfaceDOC/firstProject/PerSongs.ashx?action=Get_Profit&Type=1";
    String url = "http://192.168.2.26:8088/api/auth/gettoken?type=11-10-1-1";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HttpManager.get().url(url)
                .build().execute(new HttpManager.ResponseCallback<Object>() {
            @Override
            public void onError() {

            }

            @Override
            public void onSuccess(Object o) {

            }
        });

        initData();
    }

    private void initData() {
    }
}
