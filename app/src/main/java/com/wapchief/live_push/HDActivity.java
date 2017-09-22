package com.wapchief.live_push;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import com.wapchief.live_push.model.HDModel;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by wapchief on 2017/9/22.
 * 活动
 */

public class HDActivity extends BaseActivity {

    @BindView(R.id.hd_title)
    TextView mHdTitle;
    @BindView(R.id.hd_content)
    TextView mHdContent;

    HDModel.RowsBean model;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hd);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        model = (HDModel.RowsBean) getIntent().getSerializableExtra("HD");
        Log.e("modelHD", model.activityName.toString());
        mHdTitle.setText(model.activityName);

        mHdContent.setText("介绍："+model.description+"\n"
                +"活动id："+model.activityId+"\n"
                +"创建时间："+TimeUtils.ms2date("yyyy-MM-dd HH:mm:ss",model.startTime)+"\n"
                +"开始时间："+TimeUtils.ms2date("yyyy-MM-dd HH:mm:ss",model.startTime)+"\n"
                +"结束时间："+TimeUtils.ms2date("yyyy-MM-dd HH:mm:ss",model.endTime));
    }
}
