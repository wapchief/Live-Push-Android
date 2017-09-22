package com.wapchief.live_push;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.le.skin.LePublisherSkinView;
import com.le.skin.test.SettingActivity;
import com.le.skin.ui.SkinParams;
import com.letv.recorder.letvrecorderskin.*;
import com.wapchief.live_push.model.HDModel;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by wapchief on 2017/9/21.
 * 标准直播推流必传三个参数，可通过后台获取
 * 活动id：activityId
 * 用户id：userid
 * 密钥key: key
 */

public class LiveActivity extends Activity {


    String activityId = "";
    String userid;
    String key = "";
    boolean isVertical;
    @BindView(R.id.lpsv_stream_recorder)
    LePublisherSkinView skinView;
    HDModel.RowsBean bean;
    String activityName = "";
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        super.onCreate(savedInstanceState);
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        win.requestFeature(Window.FEATURE_NO_TITLE);
        activityId = getIntent().getStringExtra("ACTIVITYID");
        userid = getIntent().getStringExtra("USERID");
        key = getIntent().getStringExtra("KEY");
        isVertical = getIntent().getBooleanExtra("isVertical", false);

        setContentView(R.layout.activity_live);
        ButterKnife.bind(this);
        try {
            bean = (HDModel.RowsBean) getIntent().getSerializableExtra("HD");
            activityName = bean.activityName;
        }catch (Exception e){

        }
        SkinParams params = skinView.getSkinParams();
        params.setLanscape(!isVertical);
        test(params);
        initData();
        skinView.initPublish(userid, key, activityId,activityName+"的直播间");

    }

    private void initData() {


    }

    @Override
    protected void onResume() {
        super.onResume();
        skinView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        skinView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        skinView.onDestroy();
    }

    private void test(SkinParams params) {
        if (com.letv.recorder.letvrecorderskin.BuildConfig.DEBUG) {
            SettingActivity.jsonToObj(params, this);

        }


    }


}
