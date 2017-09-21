package com.wapchief.live_push;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.activity_id)
    EditText mActivityId;
    @BindView(R.id.user_id)
    EditText mUserId;
    @BindView(R.id.key)
    EditText mKey;
    @BindView(R.id.bt_start)
    Button mBtStart;
    @BindView(R.id.bt_start_test)
    Button mBtStartTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();

    }

    private void initView() {
        checkSelfPermission();
    }


    @OnClick({R.id.bt_start, R.id.bt_start_test})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bt_start:
                Intent intent = new Intent(this, LiveActivity.class);
                intent.putExtra("ACTIVITYID", mActivityId.getText());
                intent.putExtra("USERID", mUserId.getText());
                intent.putExtra("KEY", mKey.getText());
                startActivity(intent);
                break;
            case R.id.bt_start_test:
                Intent intent1 = new Intent(this, LiveActivity.class);
                intent1.putExtra("ACTIVITYID", "A201709180000043");
                intent1.putExtra("USERID", "922294");
                intent1.putExtra("KEY", "891d3747c27e715cf8018eb8352a9d7b");
                intent1.putExtra("isVertical", true);
                startActivity(intent1);
                break;
        }
    }

    /**
     * 检查权限,获取所有需要的权限
     * 当targetSdkVersion大于23并且打算在6.0手机上运行时,请动态申请SDK所需要的权限
     */
    public void checkSelfPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_PHONE_STATE}, 0);
        }
    }
}
