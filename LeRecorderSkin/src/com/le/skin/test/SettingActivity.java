package com.le.skin.test;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.le.skin.ui.SkinParams;
import com.letv.recorder.controller.RecorderContext;
import com.letv.recorder.letvrecorderskin.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 测试Activity
 * 配置所有可以配置的参数
 */

public class SettingActivity extends Activity {
     EditText videoBitrate;
     EditText reCount;
     EditText videoWidth;
     EditText videoHeight;
     EditText surfaceWidth;
     EditText surfaceHeight;
     RadioGroup cameraID;
     RadioGroup onAnimation;
     RadioGroup onTouch;
     RadioGroup resume;
     RadioGroup updateLogFile;
     RadioGroup volumeGain;
     RadioGroup mirror;
     RadioGroup firstMachine;
     RadioGroup openGestureZoom;
     RadioGroup publisherModel;
    protected void onCreate( Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED|WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.le_recorder_test);
        initView();
        bindData();
        findViewById(R.id.test_cacel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.test_submit).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                getSkinParams();
                finish();
            }
        });
    }

    private void initView() {
        reCount = (EditText) findViewById(R.id.test_reCount);
         videoBitrate = (EditText) findViewById(R.id.test_videoBitrate);
         videoWidth = (EditText) findViewById(R.id.test_videowidth);
         videoHeight = (EditText) findViewById(R.id.test_videoHeight);
         surfaceWidth = (EditText) findViewById(R.id.test_surfaceViewwidth);
        surfaceHeight = (EditText) findViewById(R.id.test_surfaceViewHeight);
        cameraID = (RadioGroup) findViewById(R.id.test_camera);
         onAnimation = (RadioGroup) findViewById(R.id.test_isOnAnimation);
         onTouch = (RadioGroup) findViewById(R.id.test_isOnTouch);
         resume = (RadioGroup) findViewById(R.id.test_home);
         updateLogFile = (RadioGroup) findViewById(R.id.test_update);
        volumeGain  = (RadioGroup) findViewById(R.id.test_audio);
         mirror = (RadioGroup) findViewById(R.id.test_isMirror);
         firstMachine = (RadioGroup) findViewById(R.id.test_isFirstMachine);
         openGestureZoom = (RadioGroup) findViewById(R.id.test_zoom);
        publisherModel = (RadioGroup) findViewById(R.id.test_publisher_model);
    }

    private SkinParams getSkinParams(){
        SkinParams params = new SkinParams();
        params.setReCount(Integer.valueOf(reCount.getText().toString()));
        params.setVideoBitrate(Integer.valueOf(videoBitrate.getText().toString())*1000);
        params.setVideoWidth(Integer.valueOf(videoWidth.getText().toString()));
        params.setVideoHeight(Integer.valueOf(videoHeight.getText().toString()));
        params.setSurfaceWidth(Integer.valueOf(surfaceWidth.getText().toString()));
        params.setSurfaceHeight(Integer.valueOf(surfaceHeight.getText().toString()));
        params.setCameraId((getRadioResult(cameraID))?Camera.CameraInfo.CAMERA_FACING_BACK:Camera.CameraInfo.CAMERA_FACING_FRONT);
        params.setOnAnimation(getRadioResult(onAnimation));
        params.setOnTouch(getRadioResult(onTouch));
        params.setResume(getRadioResult(resume));
        params.setUpdateLogFile(getRadioResult(updateLogFile));
        params.setVolumeGain(getRadioResult(volumeGain));
        params.setMirror(getRadioResult(mirror));
        params.setFirstMachine(getRadioResult(firstMachine));
        params.setOpenGestureZoom(getRadioResult(openGestureZoom));
        params.setPublisherModel(getPublisherModel());
        String json = objToJson(params);
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("skinparams",json).apply();
        return params;
    }
    private void bindData() {
        SkinParams params = jsonToObj(null,this);
        videoBitrate.setText(params.getVideoBitrate()/1000 + "");
        videoWidth.setText((params.getVideoWidth() <= 0)?"368":params.getVideoWidth()+ "");
        videoHeight.setText((params.getVideoHeight() <= 0)?"640":params.getVideoHeight()+ "");
        surfaceWidth.setText((params.getSurfaceWidth() <= 0)?"-1":params.getSurfaceWidth()+ "");
        surfaceHeight.setText((params.getSurfaceHeight() <= 0)?"-1":params.getSurfaceHeight()+ "");
        reCount.setText(params.getReCount()+"");
        setRadioGroup(cameraID,params.getCameraId() == Camera.CameraInfo.CAMERA_FACING_BACK);
        setRadioGroup(onAnimation,params.isOnAnimation());
        setRadioGroup(onTouch,params.isOnTouch());
        setRadioGroup(resume,params.isResume());
        setRadioGroup(updateLogFile,params.isUpdateLogFile());
        setRadioGroup(volumeGain,params.isVolumeGain());
        setRadioGroup(mirror,params.isMirror());
        setRadioGroup(firstMachine,params.isFirstMachine());
        setRadioGroup(openGestureZoom,params.isOpenGestureZoom());
        setPublisherModel(params.getPublisherModel());
    }
    private void setRadioGroup(RadioGroup view,boolean open){
        if(open) {
            view.check(R.id.test_open);
        }else{
            view.check(R.id.test_close);
        }
    }
    private boolean getRadioResult(RadioGroup view){
        return view.getCheckedRadioButtonId() == R.id.test_open;
    }
    private int getPublisherModel(){
        int i = publisherModel.getCheckedRadioButtonId();
        if (i == R.id.test_ALWAYS_yes) {
            return RecorderContext.ALWAYS_PUBLISHER_MODE;
        } else if (i == R.id.test_audio_yes) {
            return RecorderContext.NOT_VIDEO_NO_PUBLISHER_MODE;
        }else if(i == R.id.test_video_yes){
            return RecorderContext.NOT_AUDIO_NO_PUBLISHER_MODE;
        }else{
            return RecorderContext.ALWAYS_NO_PUBLISHER_MODE;
        }
    }
    private void setPublisherModel(int model){
        switch (model){
            case RecorderContext.ALWAYS_PUBLISHER_MODE:
                publisherModel.check(R.id.test_ALWAYS_yes);
                break;
            case RecorderContext.NOT_VIDEO_NO_PUBLISHER_MODE:
                publisherModel.check(R.id.test_audio_yes);
                break;
            case RecorderContext.NOT_AUDIO_NO_PUBLISHER_MODE:
                publisherModel.check(R.id.test_video_yes);
                break;
            case RecorderContext.ALWAYS_NO_PUBLISHER_MODE:
                publisherModel.check(R.id.test_ALWAYS_no);
                break;
        }
    }
    public String objToJson(SkinParams params){
        if(params == null) params = new SkinParams();
        JSONObject object = new JSONObject();
        try {
            object.put("CameraId",params.getCameraId());
            object.put("FirstMachine",params.isFirstMachine());
            object.put("Mirror",params.isMirror());
            object.put("OnAnimation",params.isOnAnimation());
            object.put("OnTouch",params.isOnTouch());
            object.put("OpenGestureZoom",params.isOpenGestureZoom());
            object.put("Resume",params.isResume());
            object.put("SurfaceHeight",params.getSurfaceHeight());
            object.put("SurfaceWidth",params.getSurfaceWidth());
            object.put("UpdateLogFile",params.isUpdateLogFile());
            object.put("VideoBitrate",params.getVideoBitrate());
            object.put("VideoWidth",params.getVideoWidth());
            object.put("VideoHeight",params.getVideoHeight());
            object.put("VolumeGain",params.isVolumeGain());
            object.put("PublisherModel",params.getPublisherModel());
            object.put("reCount",params.getReCount());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }
    public static SkinParams jsonToObj(SkinParams params, Context context){
        if(params == null) params = new SkinParams();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String json = sp.getString("skinparams",null);
        if(!TextUtils.isEmpty(json)){
            try {
                JSONObject object = new JSONObject(json);
                params.setCameraId(object.optInt("CameraId"));
                params.setFirstMachine(object.optBoolean("FirstMachine"));
                params.setMirror(object.optBoolean("Mirror"));
                params.setOnAnimation(object.optBoolean("OnAnimation"));
                params.setOnTouch(object.optBoolean("OnTouch"));
                params.setOpenGestureZoom(object.optBoolean("OpenGestureZoom"));
                params.setResume(object.optBoolean("Resume"));
                params.setSurfaceHeight(object.optInt("SurfaceHeight"));
                params.setSurfaceWidth(object.optInt("SurfaceWidth"));
                params.setUpdateLogFile(object.optBoolean("UpdateLogFile"));
                params.setVideoBitrate(object.optInt("VideoBitrate"));
                params.setVideoWidth(object.optInt("VideoWidth"));
                params.setVideoHeight(object.optInt("VideoHeight"));
                params.setVolumeGain(object.optBoolean("VolumeGain"));
                params.setPublisherModel(object.optInt("PublisherModel",RecorderContext.ALWAYS_PUBLISHER_MODE));
                params.setReCount(object.optInt("reCount",params.getReCount()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return params;
    }
}
