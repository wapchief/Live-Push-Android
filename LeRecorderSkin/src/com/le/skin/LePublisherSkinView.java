package com.le.skin;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.letv.recorder.bean.LivesInfo;
import com.letv.recorder.callback.LetvRecorderCallback;
import com.letv.recorder.controller.LetvPublisher;
import com.letv.recorder.letvrecorderskin.R;
import com.letv.recorder.ui.logic.RecorderConstance;

import java.util.ArrayList;

public class LePublisherSkinView extends BaseSkinView {
    private String userId;
    private String secretKey;
    private String activityId;
    private final int LE_HTTP_HANDLER = 0x11111;
    private Dialog machineDialog;
    private boolean isRePublisher = false;
    private int reMachine = -1;

    public LePublisherSkinView(Context context) {
        super(context);
    }

    public LePublisherSkinView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LePublisherSkinView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected boolean startPublisher() {
        super.startPublisher();
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(secretKey) || TextUtils.isEmpty(activityId)) {
            Log.d(TAG,"用户ID,秘钥和活动ID不可以为空");
            showErrorDialog("用户ID,秘钥和活动ID不可以为空");
            return false;
        }
        ((LetvPublisher) publisher).handleMachine(new LetvRecorderCallback<ArrayList<LivesInfo>>() {
            @Override
            public void onFailed(int code, String msg) {
                Log.d(TAG,"机位信息获取失败");
            }

            @Override
            public void onSucess(ArrayList<LivesInfo> data) {
                Log.d(TAG,"机位信息获取成功");
                Message message = mHandler.obtainMessage(LE_HTTP_HANDLER);
                message.obj = data;
                mHandler.sendMessage(message);
            }
        });

        return true;
    }

    @Override
    protected void publisherMessage(Message msg) {
        super.publisherMessage(msg);
        Bundle bundle = msg.getData();
        switch (msg.what) {
            case RecorderConstance.LIVE_STATE_END_ERROR:
                hideLoadingDialog();
                showErrorDialog("直播已结束，可再去创建您的直播活动");
                break;
            case RecorderConstance.LIVE_STATE_NOT_STARTED_ERROR:
                showErrorDialog("直播未开始，可在管理平台去开启直播");
                hideLoadingDialog();
                break;
            case RecorderConstance.LIVE_STATE_PUSH_COMPLETE:
                showErrorDialog("直播已结束，可再去创建您的直播活动");
                if(publisher.isRecording()){
                    stopPublisher();
                    openButton.setImageResource(R.drawable.letv_recorder_open);
                }
                break;
            case RecorderConstance.LIVE_STATE_NEED_RECORD:
                isRec = "true".equalsIgnoreCase(bundle.getString("detailObj0"));
                break;
            case RecorderConstance.LIVE_STATE_TIME_REMAINING:
                break;
            case RecorderConstance.LIVE_STATE_OTHER_ERROR:
                isRePublisher = false;
                openButton.setImageResource(R.drawable.letv_recorder_open);
                showErrorDialog("云直播机位状态获取失败");
                hideLoadingDialog();
                break;
            case LE_HTTP_HANDLER:
                leHttpHandler(msg);
                break;
        }
    }

    private void leHttpHandler(Message msg) {
        openButton.setImageResource(R.drawable.letv_recorder_open);
        ArrayList<LivesInfo> data = (ArrayList<LivesInfo>) msg.obj;
        int num = data.size();
        if(isRePublisher && num > reMachine){
            selectMachine(reMachine);
        }else if (skinParams.isFirstMachine() && num > 0) {
            reMachine = 0;
            selectMachine(reMachine);
        } else {
            switch (num) {
                case 0:// 当前无机位
                    hideLoadingDialog();
                    Log.d(TAG, "无可用机位");
                    showErrorDialog("当前无可用机位");
                    break;
                case 1:// 只有一个机位信息
                    reMachine = 0;
                    selectMachine(reMachine);
                    break;
                default:// 多机位
                    hideLoadingDialog();
                    machineDialog = showMachineDialog(data);
                    break;
            }
        }
    }
    private Dialog showMachineDialog(ArrayList<LivesInfo> data) {
        Dialog dialog = new Dialog(getContext(), R.style.letvRecorderDialog);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.le_recorder_machine_layout,null);
        dialog.setContentView(view);
        TextView v1 = (TextView) view.findViewById(R.id.letv_recorder_machine_one);
        v1.setTag(0);
        v1.setOnClickListener(onClickListener);
        TextView v2 = (TextView) view.findViewById(R.id.letv_recorder_machine_two);
        v2.setTag(1);
        v2.setOnClickListener(onClickListener);
        TextView v3 = (TextView) view.findViewById(R.id.letv_recorder_machine_three);
        v3.setTag(2);
        v3.setOnClickListener(onClickListener);
        TextView v4 = (TextView) view.findViewById(R.id.letv_recorder_machine_four);
        v4.setTag(3);
        v4.setOnClickListener(onClickListener);
        for (int i = 0;i <data.size();i++){
            switch (i){
                case 0:
                    v1.setVisibility(View.VISIBLE);
                    if(data.get(0).status != 0){
                        v1.setBackgroundResource(R.drawable.letv_recorder_angle_gray);
                        v1.setClickable(false);
                    }
                    break;
                case 1:
                    v2.setVisibility(View.VISIBLE);
                    if(data.get(1).status != 0){
                        v2.setBackgroundResource(R.drawable.letv_recorder_angle_gray);
                        v2.setClickable(false);
                    }
                    break;
                case 2:
                    v3.setVisibility(View.VISIBLE);
                    if(data.get(2).status != 0){
                        v3.setBackgroundResource(R.drawable.letv_recorder_angle_gray);
                        v3.setClickable(false);
                    }
                    break;
                case 3:
                    v4.setVisibility(View.VISIBLE);
                    if(data.get(3).status != 0){
                        v4.setBackgroundResource(R.drawable.letv_recorder_angle_gray);
                        v4.setClickable(false);
                    }
                    break;
            }
        }
        dialog.show();
        return  dialog;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            showLoadingDialog();
            reMachine = (Integer) v.getTag();
            selectMachine(reMachine);
        }
    };
    private void selectMachine(int numFlag) {
        isRePublisher = false;
        openButton.setImageResource(R.drawable.letv_recorder_stop);
        if(machineDialog != null && machineDialog.isShowing()) {
            machineDialog.dismiss();
            machineDialog = null;
        }
        if (((LetvPublisher) publisher).selectMachine(numFlag)) {
            publisher.publish();
        } else {
            openButton.setImageResource(R.drawable.letv_recorder_open);
            hideLoadingDialog();
            Log.d(TAG,"该机位已经被其他人抢占了,请重新选择");
            showErrorDialog("该机位已经被其他人抢占了,请重新选择");
        }

    }

    public void initPublish(String userId, String secretKey, String activityId) {
        Log.d(TAG,"initPublish，初始化");
        this.userId = userId;
        this.secretKey = secretKey;
        this.activityId = activityId;
        if(TextUtils.isEmpty(skinParams.getTitle())){
            nameView.setText(activityId);
        }
        LetvPublisher.init(activityId,userId,secretKey);
        publisher = LetvPublisher.getInstance();
        super.initPublish();
    }

    @Override
    protected void rePublisher() {
        isRePublisher = true;
        super.rePublisher();
    }
}
