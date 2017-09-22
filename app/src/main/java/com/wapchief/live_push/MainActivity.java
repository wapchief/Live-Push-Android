package com.wapchief.live_push;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.wapchief.live_push.model.HDModel;
import com.wapchief.live_push.network.MyMethod;
import com.wapchief.live_push.network.NetWorkManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {


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
    @BindView(R.id.bt_start_http)
    Button mBtStartHttp;

    int userid = 0;
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


    @OnClick({R.id.bt_start, R.id.bt_start_test,R.id.bt_start_http})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bt_start:
                Intent intent = new Intent(this, LiveActivity.class);
                intent.putExtra("ACTIVITYID", mActivityId.getText());
                intent.putExtra("USERID", mUserId.getText());
                intent.putExtra("KEY", mKey.getText());
                intent.putExtra("isVertical", true);
                startActivity(intent);
                break;
            case R.id.bt_start_test:
                showProgressDialog("正在加载直播....");
                pushUrl("A201709180000043",1);
//                Intent intent1 = new Intent(this, LiveActivity.class);
//                intent1.putExtra("ACTIVITYID", "A201709180000043");
//                intent1.putExtra("USERID", "922294");
//                intent1.putExtra("KEY", "891d3747c27e715cf8018eb8352a9d7b");
//                intent1.putExtra("isVertical", true);
//                startActivity(intent1);
                break;
            case R.id.bt_start_http:
                showProgressDialog("正在获取....");
                pushUrl("A201709180000043",0);
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
    HDModel hdModel;
    HDModel.RowsBean bean;
    private void pushUrl(String activityId, final int type) {

        NetWorkManager.searchHD(activityId,getTimestamp(), new Callback<HDModel>() {
            @Override
            public void onResponse(Call<HDModel> call, Response<HDModel> response) {
                dismissProgressDialog();
                Log.e("onResponseList", response+"\n" + response.body());
                if (response.body().equals("")){
                    return;
                }
                hdModel = response.body();
                bean = hdModel.rows.get(0);
                if (type==0) {
                    Intent intent = new Intent(MainActivity.this, HDActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("HD", bean);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }else {
                    Intent intent1 = new Intent(MainActivity.this, LiveActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("HD", bean);
                    intent1.putExtras(bundle);
                    intent1.putExtra("ACTIVITYID", "A201709180000043");
                    intent1.putExtra("USERID", "922294");
                    intent1.putExtra("KEY", "891d3747c27e715cf8018eb8352a9d7b");
                    intent1.putExtra("isVertical", true);
                    startActivity(intent1);
                }


            }

            @Override
            public void onFailure(Call<HDModel> call, Throwable throwable) {
                Log.e("onFailurlList", "" + throwable);

            }
        });
    }
}
