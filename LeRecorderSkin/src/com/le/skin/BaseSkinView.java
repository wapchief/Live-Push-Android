package com.le.skin;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.le.skin.ui.FilterListView;
import com.le.skin.ui.RecorderDialogBuilder;
import com.le.skin.ui.Rotate3dAnimation;
import com.le.skin.ui.SkinParams;
import com.le.skin.utils.ClickUtils;
import com.le.skin.utils.ToastUtils;
import com.letv.recorder.bean.AudioParams;
import com.letv.recorder.bean.CameraParams;
import com.letv.recorder.callback.IRawFrameSourceListener;
import com.letv.recorder.callback.ISurfaceCreatedListener;
import com.letv.recorder.callback.LetvRecorderCallback;
import com.letv.recorder.callback.PublishListener;
import com.letv.recorder.controller.CameraSurfaceView;
import com.letv.recorder.controller.Publisher;
import com.letv.recorder.letvrecorderskin.R;
import com.letv.recorder.ui.logic.RecorderConstance;
import com.letv.recorder.ui.logic.RecorderErrorCodec;
import com.letv.recorder.ui.logic.RecorderErrorMessage;
import com.letv.recorder.util.LeLog;
import com.letv.recorder.util.NetworkUtils;

import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

public abstract class BaseSkinView extends FrameLayout {
    protected final static String TAG = "BaseSkinView";
    protected Publisher publisher;
    protected SkinParams skinParams;
    private boolean isback;
    private ImageView backButton;
    protected TextView nameView;
    protected ImageView openButton;
    private ImageView flashButton;
    private ImageView volumeButton;
    private ImageView switchButton;
    private ImageView filterButton;
    private TextView timerView;
    private ImageView thumdButton;
    private ImageView mirrorButton;
    protected TextView recView;
    protected boolean isRec = true;
    private boolean isFlash = false;
    private boolean isVolume = false;
    private Dialog loadingDialog;
    private Dialog errorDialog;
    private Dialog messageDialog;
    private StringBuilder mFormatBuilder = new StringBuilder();
    private Formatter mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    private int time;
    private volatile int video_lose = 0;
    private volatile int audio_lose = 0;
    private FilterListView filterListView;
    private volatile boolean showTimer = false;
    protected CameraSurfaceView surfaceView;
    private RelativeLayout surfaceRoot;
    private boolean isFirstSize = false;
    private volatile int frameLose = 0;
    private final static int UPLOAD_FILE_ERROR = -100001;
    private final static int UPLOAD_FILE_SUCCESS = 100001;
    private SeekBar zoomBar;
    private RelativeLayout rlSeekLayout;
    private int zoomCurrent = 0;
    private final static int RE_TIME = 2*1000;

    public BaseSkinView(Context context) {
        super(context);
        init(null);
    }

    public BaseSkinView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public BaseSkinView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);

    }

    /**
     * 初始化操作
     *
     * @param attrs
     */
    private void init(AttributeSet attrs) {
        Log.i(TAG, "初始化init");
        skinParams = new SkinParams();
        if (attrs != null) {
            //判断是否使用自定义参数
            Log.i(TAG, "判断是否使用自定义参数");
            decodeSkinParams(attrs);
        }
        isback = false;
        zoomCurrent = 0;
        initView();
    }

    /**
     * 初始化view
     */
    private void initView() {
        Log.i(TAG, "初始化view,initView");
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View skinView = inflater.inflate(R.layout.le_recorder_skin_view, this);
        surfaceRoot = (RelativeLayout) skinView.findViewById(R.id.rl_surface_root);
        surfaceView = (CameraSurfaceView) skinView.findViewById(R.id.camera_surface_view);
        backButton = (ImageView) skinView.findViewById(R.id.imgB_back);
        nameView = (TextView) skinView.findViewById(R.id.tv_title);
        openButton = (ImageView) skinView.findViewById(R.id.imgV_open);
        flashButton = (ImageView) skinView.findViewById(R.id.imgV_flashlight);
        volumeButton = (ImageView) skinView.findViewById(R.id.imgV_voice);
        switchButton = (ImageView) skinView.findViewById(R.id.imgV_postposition_camera);
        filterButton = (ImageView) skinView.findViewById(R.id.imgV_postposition_filter);
        timerView = (TextView) skinView.findViewById(R.id.tv_time);
        mirrorButton = (ImageView) skinView.findViewById(R.id.imgV_mirror);
        rlSeekLayout = (RelativeLayout) skinView.findViewById(R.id.rl_zoom_seek_bar);
        recView = (TextView) skinView.findViewById(R.id.tv_rec);
        thumdButton = (ImageView) skinView.findViewById(R.id.imgV_thumd);
        zoomBar = (SeekBar) skinView.findViewById(R.id.seekB_zoom);
        rlSeekLayout.setVisibility(View.INVISIBLE);
        zoomBar.setOnSeekBarChangeListener(seekBarChangeListener);
        findViewById(R.id.include_top_skin).setOnTouchListener(onTouchListener);
        findViewById(R.id.include_bottom_skin).setOnTouchListener(onTouchListener);
        filterListView = new FilterListView((RelativeLayout) skinView.findViewById(R.id.rl_skin_root), getContext(), new FilterListView.FilterListListener() {
            @Override
            public void selectFilter(int current) {
                switchFilter(current);
                CameraParams p = publisher.getCameraParams();
                if(p != null) {
                    filterListView.setFilterLevel(p.getBeautyFilterLevel());
                }
            }

            @Override
            public void chageFilterLevel(int level) {
                setFilterLevel(level);
            }
        });
    }

    /**
     * 初始化图标状态
     */
    private void initIcon() {
        Log.i(TAG, "初始化图标状态");
        timerView.setVisibility(View.INVISIBLE);
        recView.setVisibility(View.INVISIBLE);
        thumdButton.setVisibility(View.INVISIBLE);
        if (skinParams.isVolumeGain()) {
            volumeButton.setImageResource(R.drawable.letv_recorder_voise_open);
            isVolume = true;
            setVolume(1);
        } else {
            volumeButton.setImageResource(R.drawable.letv_recorder_voise_close);
            isVolume = false;
            setVolume(0);
        }
        if (skinParams.getCameraId() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            flashButton.setVisibility(View.INVISIBLE);
            isFlash = false;
            flashButton.setImageResource(R.drawable.letv_recorder_flash_light_close);
        }
        if (skinParams.isLanscape()) {
            rlSeekLayout.setPadding(dip2px(100), 0, dip2px(100), 0);
        } else {
            rlSeekLayout.setPadding(dip2px(45), 0, dip2px(45), 0);
        }
        if (skinParams.getCameraId() == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mirrorButton.setVisibility(View.INVISIBLE);
        } else {
            enableFrontCameraMirror(skinParams.isMirror());
        }
        rlSeekLayout.setOnTouchListener(onTouchListener);
        volumeButton.setOnClickListener(onClickListener);
        backButton.setOnClickListener(onClickListener);
        openButton.setOnClickListener(onClickListener);
        flashButton.setOnClickListener(onClickListener);
        volumeButton.setOnClickListener(onClickListener);
        switchButton.setOnClickListener(onClickListener);
        filterButton.setOnClickListener(onClickListener);
        mirrorButton.setOnClickListener(onClickListener);
    }

    //拦截点击事件
    private OnTouchListener onTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            v.setEnabled(false);
            int i = v.getId();
            if (i == R.id.imgB_back) {
                Log.i(TAG, "点击后退按钮");
                ((Activity) getContext()).finish();
                v.setEnabled(true);
            } else if (i == R.id.imgV_open) {
                boolean recording = publisher.isRecording();
                reCount = 0;
                Log.i(TAG, "点击开始推流按钮，是否正在推流：" + recording);
                if (recording) {
                    stopPublisher();
                    hideTimerView();
                    openButton.setImageResource(R.drawable.letv_recorder_open);
                } else {
                    time = 0;
                    openClickPublisher();
                }
            } else if (i == R.id.imgV_flashlight) {//点击闪关灯按钮
                Log.i(TAG, "点击闪光灯按钮，当前是否已经开启闪光灯：" + isFlash);
                isFlash = !isFlash;
                changeFlash(isFlash);
                if (isFlash) {
                    flashButton.setImageResource(R.drawable.letv_recorder_flash_light_open);
                } else {
                    flashButton.setImageResource(R.drawable.letv_recorder_flash_light_close);
                }
                v.setEnabled(true);
            } else if (i == R.id.imgV_voice) {
                Log.i(TAG, "点击音量按钮，当前音量状态：" + isVolume);
                isVolume = !isVolume;
                if (isVolume) {
                    volumeButton.setImageResource(R.drawable.letv_recorder_voise_open);
                    setVolume(1);
                } else {
                    volumeButton.setImageResource(R.drawable.letv_recorder_voise_close);
                    setVolume(0);
                }
                v.setEnabled(true);
            } else if (i == R.id.imgV_postposition_camera) {
                CameraParams params = publisher.getCameraParams();
                boolean temp = false;
                if (params != null){
                    temp = params.getCameraId() == Camera.CameraInfo.CAMERA_FACING_BACK;
                 }
                Log.i(TAG, "切换前后摄像头，当前是后置摄像头么？" + temp);
                if (temp) {
                    switchCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                } else {
                    switchCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                }
            } else if (i == R.id.imgV_postposition_filter) {
                Log.i(TAG, "点击选择滤镜框");
                filterListView.showFilter();
                v.setEnabled(true);
            } else if (i == R.id.imgV_mirror) {
                Log.i(TAG, "点击镜像切换");
                enableFrontCameraMirror(!skinParams.isMirror());
                v.setEnabled(true);
            }
        }
    };

    /**
     * 初始化推流
     */
    protected void initPublish() {
        LeLog.d(TAG, "初始化推流器initPublish,所有的初始化参数是：" + skinParams.toString());
//        skinView.setBackgroundColor(0x01000001);
        FrameLayout.LayoutParams params;
        if (skinParams.getSurfaceWidth() <= 0 || skinParams.getSurfaceHeight() <= 0) {
            params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        } else {
            params = new FrameLayout.LayoutParams(skinParams.getSurfaceWidth(), skinParams.getSurfaceHeight(), Gravity.CENTER);
        }
        surfaceRoot.setLayoutParams(params);
        View fouceView = new View(getContext());
        fouceView.setBackgroundResource(skinParams.getFoceView());
        surfaceRoot.addView(fouceView, dip2px(70), dip2px(70));
        publisher.initPublisher((Activity) getContext());
        publisher.getRecorderContext().setUseLanscape(skinParams.isLanscape());//是否横屏
        CameraParams cameraParams = publisher.getCameraParams();
        AudioParams audioParams = publisher.getAudioParams();
        publisher.setPublishListener(listener);
//        publisher.setIRawFrameListener(rawFrameListener);
        publisher.getVideoRecordDevice().bindingGLView(surfaceView);
        publisher.getVideoRecordDevice().setSurfaceCreatedListener(surfaceCreatedListener);
        if (!(skinParams.getVideoWidth() <= 0 || skinParams.getVideoHeight() <= 0)) {
            cameraParams.setWidth(skinParams.getVideoWidth());
            cameraParams.setHeight(skinParams.getVideoHeight());
        }
        cameraParams.setCameraId(skinParams.getCameraId()); //开启默认前置摄像头
        cameraParams.setVideoBitrate(skinParams.getVideoBitrate()); //设置码率
        audioParams.setEnableVolumeGain(true);//开启音量调节,注意,这一点会影响性能,如果没有必要,设置为false
        cameraParams.setFocusOnTouch(skinParams.isOnTouch());//关闭对焦功能
        cameraParams.setFocusOnAnimation(skinParams.isOnAnimation());//关闭对焦动画
        cameraParams.setOpenGestureZoom(skinParams.isOpenGestureZoom());
        publisher.getVideoRecordDevice().setFocusView(fouceView);//设置对焦图片。如果需要对焦功能和对焦动画,请打开上边两个设置,并且在这里传入一个合适的View
        publisher.getRecorderContext().setAutoUpdateLogFile(skinParams.isUpdateLogFile()); //是否开启日志文件自动上报
        publisher.getRecorderContext().setPublisherMode(skinParams.getPublisherModel());//允许单音频和单视频推流
        initIcon();
    }

    /**
     * 显示加载对话框
     */
    protected void showLoadingDialog() {
        Log.i(TAG, "显示加载对话框showLoadingDialog");
        hideLoadingDialog();
        openButton.setClickable(false);
        loadingDialog = RecorderDialogBuilder.showLoadDialog(getContext(), "正在急速加载中,请稍后");
    }

    /**
     * 判断加载对话框是否显示着
     * @return
     */
    protected boolean isShowLoadingDialog(){
        return loadingDialog != null;
    }
    /**
     * 隐藏加载对话框
     */
    protected void hideLoadingDialog() {
        Log.i(TAG, "隐藏加载对话框hideLoadingDialog");
        openButton.setClickable(true);
        openButton.setEnabled(true);
        if (loadingDialog != null && loadingDialog.isShowing()) {
            try {
                loadingDialog.dismiss();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            loadingDialog = null;
        }
    }

    /**
     * 显示错误对话框
     */
    protected void showErrorDialog(String msg) {
        Log.i(TAG, "显示错误对话框showErrorDialog");
        if (errorDialog != null && errorDialog.isShowing()) {
            errorDialog.dismiss();
            errorDialog = null;
        }
        errorDialog = RecorderDialogBuilder.showCommentDialog(getContext(),msg, "我知道了", null, new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(errorDialog!=null){
                    errorDialog.dismiss();
                    errorDialog = null;
                }
            }
        }, null);
    }

    /**
     * 开始推流时间计数
     */
    private void showTimerView() {
        Log.i(TAG, "开始推流时间计数");
        openButton.setImageResource(R.drawable.letv_recorder_stop);
        showTimer = true;
        timerView.setVisibility(View.VISIBLE);
        if (isRec) {
            recView.setVisibility(View.VISIBLE);
        }
        thumdButton.setVisibility(View.VISIBLE);
        timerView.setText(stringForTime(time));
        uiHandler.postDelayed(timeRunnable, 1000);
    }

    /**
     * 关闭推流时间计数
     */
    private void hideTimerView() {
        Log.i(TAG, "关闭推流时间计数");
        showTimer = false;
        if(uiHandler != null) {
            uiHandler.removeCallbacks(timeRunnable);
        }

        timerView.setVisibility(View.INVISIBLE);
        recView.setVisibility(View.INVISIBLE);
        thumdButton.setVisibility(View.INVISIBLE);
    }

    /**
     * 主动上传日志文件
     */
    public void updataFile() {
        Log.i(TAG, "主动上传日志文件");
        publisher.sendLogFile(new LetvRecorderCallback() {
            @Override
            public void onFailed(int code, String msg) {
                Message message = mHandler.obtainMessage(UPLOAD_FILE_ERROR);
                Bundle bundle = new Bundle();
                bundle.putString("detailErrorCode",String.valueOf(code));
                bundle.putString("detailErrorMessage",msg);
                message.setData(bundle);
                mHandler.sendMessage(message);
            }

            @Override
            public void onSucess(Object data) {
                Message message = mHandler.obtainMessage(UPLOAD_FILE_SUCCESS);
                mHandler.sendMessage(message);
            }
        });
    }

    public void onPause() {
        Log.i(TAG, "onPause进入");
        surfaceView.onPause();
        publisher.getVideoRecordDevice().stop();
        mHandler.sendEmptyMessageDelayed(MHandler.PAUSE,1000);
    }

    private  void pause(){
        if (publisher.isRecording()) { //正在推流
            isback = true;
            stopPublisher();//停止推流
        }
        //关闭摄像头
        uiHandler.removeCallbacksAndMessages(null);
        mHandler.removeCallbacksAndMessages(null);
    }
    /**
     * 切换摄像头
     */
    private boolean isSwitchCamera = true;
    private void switchCamera(int cameraId) {
        if (ClickUtils.isFastDoubleClick()) {
            switchButton.setEnabled(true);
            LeLog.d("{action:fastClick,return}");
            return;
        }
        if(!isSwitchCamera){
            switchButton.setEnabled(true);
            ToastUtils.showShort(getContext(),"请勿频繁切换");
            return;
        }
        isSwitchCamera = false;
        Log.i(TAG, "switchCamera切换摄像头:" + cameraId);
         publisher.getVideoRecordDevice().switchCamera(cameraId);//切换摄像头
    }

    private void switchCameraSuccess(int cameraId){
        Rotate3dAnimation animation = new Rotate3dAnimation(0, 180, switchButton.getWidth() / 2f, switchButton.getHeight() / 2f, 0f, true);
        animation.setDuration(500);
        animation.setFillAfter(true);
        switchButton.startAnimation(animation);
        zoomCurrent = 0;
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            isFlash = false;
            flashButton.setImageResource(R.drawable.letv_recorder_flash_light_close);
            flashButton.setVisibility(View.INVISIBLE);
            mirrorButton.setVisibility(View.VISIBLE);
            enableFrontCameraMirror(skinParams.isMirror());
        } else {
            flashButton.setVisibility(View.VISIBLE);
            mirrorButton.setVisibility(View.INVISIBLE);
        }
    }
    /**
     * 开启闪光灯。注意,当使用前置摄像头时不能打开闪光灯
     */
    private void changeFlash(boolean flash) {
        Log.i(TAG, "changeFlash,切换闪光灯：" + flash);
        publisher.getVideoRecordDevice().setFlashFlag(flash);//切换闪关灯
    }

    /**
     * 切换滤镜,设置为0为关闭滤镜
     */
    private void switchFilter(int model) {
        Log.i(TAG, "switchFilter,切换滤镜：" + model);
        publisher.getVideoRecordDevice().setFilterModel(model);//切换滤镜
    }

    private void setFilterLevel(int level) {
        Log.i(TAG, "setFilterLevel,当前美颜级别：" + level);
        publisher.getVideoRecordDevice().setBeautyfilterlevel(level);
    }

    private void openClickPublisher() {
        volumeButton.setImageResource(R.drawable.letv_recorder_voise_open);
        isVolume = true;
        setVolume(1);
        volumeButton.setClickable(true);
        Log.i(TAG, "openClickPublisher，推流前网络判断");
        if (NetworkUtils.getNetType(getContext()) == null) {
            messageDialog = RecorderDialogBuilder.showCommentDialog(getContext(), "本地网络异常", "我知道了", null, new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.i(TAG, "网络连接失败,请检查后重试");
                    if(messageDialog!=null){
                        messageDialog.dismiss();
                        messageDialog = null;
                        openButton.setClickable(true);
                        openButton.setEnabled(true);
                    }
                }
            }, null);
        } else if (!NetworkUtils.isWifiNetType(getContext())) {
            Log.i(TAG, "移动网络推流");
            messageDialog = RecorderDialogBuilder.showMobileNetworkWarningDialog(getContext(), new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (messageDialog != null) {
                        messageDialog.dismiss();
                        messageDialog = null;
                    }
                    if (startPublisher()) {
                        if(!isShowLoadingDialog()) {
                            showLoadingDialog();
                        }
                        openButton.setImageResource(R.drawable.letv_recorder_stop);
                    } else {
                        openButton.setImageResource(R.drawable.letv_recorder_open);
                    }
                }
            }, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    openButton.setImageResource(R.drawable.letv_recorder_open);
                    hideLoadingDialog();
                    if (messageDialog != null) {
                        messageDialog.dismiss();
                        messageDialog = null;
                    }
                    openButton.setClickable(true);
                    openButton.setEnabled(true);
                }
            });
        } else {
            Log.i(TAG, "wifi推流");
            if (startPublisher()) {
                if(!isShowLoadingDialog()) {
                    showLoadingDialog();
                }
                openButton.setImageResource(R.drawable.letv_recorder_stop);
            } else {
                openButton.setImageResource(R.drawable.letv_recorder_open);
            }
        }
    }

    /**
     * 开始推流方法，这个方法需要子类去实现
     *
     * @return
     */
    protected boolean startPublisher() {
        Log.i(TAG, "startPublisher:" + this.getClass().getName());
        return false;
    }

    /**
     * 停止推流。
     */
    protected void stopPublisher() {
        Log.i(TAG, "stopPublisher,停止推流");
        hideTimerView();
        publisher.stopPublish();
    }

    /**
     * 设置声音大小,必须对setEnableVolumeGain设置为true
     *
     * @param volume 0-1为缩小音量,1为正常音量,大于1为放大音量
     */
    private void setVolume(int volume) {
        Log.i(TAG, "setVolume设置音量大小：" + volume);
        publisher.setVolumeGain(volume);//设置声音大小
    }

    /**
     * 切换镜像模式。注意，对于后置摄像头不存在非镜像模式。所以会自动切换回镜像模式
     *
     * @param isMirror
     */
    private void enableFrontCameraMirror(boolean isMirror) {
        CameraParams params = publisher.getCameraParams();
        if(params == null)return;
        if (params.getCameraId() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            Log.i(TAG, "开启镜像模式：" + isMirror);
            publisher.getVideoRecordDevice().enableFrontCameraMirror(isMirror);
            skinParams.setMirror(isMirror);
            if (isMirror) {
                mirrorButton.setImageResource(R.drawable.letv_recorder_mirror_open);
            } else {
                mirrorButton.setImageResource(R.drawable.letv_recorder_mirror_close);
            }
        } else {
            Log.w(TAG, "后置摄像头没有镜像模式");
        }
    }

    /**
     * 测试方法。如果调用，那么请在获取第一帧推流成功事件之后，每隔5秒以上调用一次。不可以重复调用
     *
     * @return
     */
    public int getPushStreamingTime() {
        Log.i(TAG, "getPushStreamingTime：获取每一帧推流时间");
        if (isFirstSize && publisher != null && publisher.isRecording()) {
            return publisher.getStreamingDelay();
        } else {
            isFirstSize = false;
        }
        return -1;
    }

    /**
     * 获取丢包率
     *
     * @return
     */
    public int getFrameLose() {
        Log.i(TAG, "getFrameLose获取丢包率");
        int temp = frameLose;
        frameLose = 0;
        return temp;
    }

    /**
     * VIew生命周期
     */
    public void onResume() {
        if(mHandler.hasMessages(MHandler.PAUSE)){
            mHandler.removeMessages(MHandler.PAUSE);
            Log.w(TAG, "用户暴力按HOME或者电源键");
        }
        Log.i(TAG, "onResume生命周期");
        LinearLayout.LayoutParams params = new  LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
        if (skinParams.isLanscape()) {
            ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            params.weight = 4;
        }else{
            ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            params.weight = 2;
        }
        findViewById(R.id.sdk_view).setLayoutParams(params);
        //给Camera 一个延时打开的时间，解决Activity打开过慢的问题
        uiHandler.post(zoomGone);
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                surfaceView.onResume();
            }
        }, 100);
        if (publisher != null) {
            isFlash = false;
            changeFlash(false);
            CameraParams p = publisher.getCameraParams();
            if (p != null && p.getCameraId() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                flashButton.setVisibility(View.INVISIBLE);
            } else {
                flashButton.setVisibility(View.VISIBLE);
            }
            flashButton.setImageResource(R.drawable.letv_recorder_flash_light_close);
        }
    }

    /**
     * VIew生命周期
     */
    public void onDestroy() {
        Log.i(TAG, "onDestroy进入");
        zoomCurrent = 0;
        stopPublisher();//停止推流
        surfaceView.onDestroy();
        isback = false;
        publisher.release();//销毁推流器
        uiHandler.removeCallbacksAndMessages(null);
        mHandler.removeCallbacksAndMessages(null);
        Log.i(TAG, "onDestroy出去");
    }

    /**
     * 在主线程处理message消息
     * @param msg
     */
    protected void publisherMessage(Message msg){
        String errorCode =  msg.getData().getString("detailObj0");
        String errorMessage =   msg.getData().getString("detailErrorMessage");
        switch (msg.what) {
            case RecorderConstance.RECORDER_OPEN_URL_FAILED:
                Log.i(TAG, "UI mHandler，推流失败" +errorCode +errorMessage);
                ToastUtils.showDebug(getContext(),errorMessage);
                openButton.setImageResource(R.drawable.letv_recorder_open);
                hideLoadingDialog();
                hideTimerView();
                showErrorDialog("无法连接推流服务器");
                openButton.setEnabled(true);
                break;
            case RecorderConstance.RECORDER_OPEN_URL_SUCESS:
                Log.i(TAG, "UI mHandler，RTMP 连接建立成功");
                break;
            case RecorderConstance.RECORDER_PUSH_FIRST_SIZE:
                Log.i(TAG, "UI mHandler，RTMP 第一帧画面推流成功");
                reCount = 0;
                isFirstSize = true;
                openButton.setEnabled(true);
                hideLoadingDialog();
                if (!showTimer) {
                    showTimerView();
                }
                break;
            case RecorderConstance.RECORDER_PUSH_ERROR:
                //云直播参数错误时不重试
                if(skinParams.getReCount() > 0 && !RecorderErrorCodec.LIVE_PARAMS_ERROR.equalsIgnoreCase(errorCode) && !RecorderErrorCodec.URL_NO_RTMP_ADDRESS.equalsIgnoreCase(errorCode)){
                    rePublisher();
                }else {
                    openButton.setEnabled(true);
                    Log.i(TAG, "UI mHandler，RTMP 推流失败");
                    ToastUtils.showDebug(getContext(), errorMessage);
                    if (!TextUtils.isEmpty(errorCode) && errorCode.contains("SDK")) {
                        showErrorDialog(errorMessage);
                    } else {
                        showErrorDialog(errorMessage);
                    }
                    openButton.setImageResource(R.drawable.letv_recorder_open);
                    hideLoadingDialog();
                    hideTimerView();
                }
                break;
            case RecorderConstance.RECORDER_PUSH_STOP_SUCCESS:
                Log.i(TAG, "UI mHandler，停止推流");
                openButton.setImageResource(R.drawable.letv_recorder_open);
                hideLoadingDialog();
                hideTimerView();
                break;
            case RecorderConstance.RECORDER_PUSH_AUDIO_PACKET_LOSS_RATE:
                //Log.i()(TAG,"UI mHandler，音频丢包");
                audio_lose++;
                break;
            case RecorderConstance.RECORDER_PUSH_VIDEO_PACKET_LOSS_RATE:
                //Log.i()(TAG,"UI mHandler，视频丢包");
                video_lose++;
                frameLose++;
                break;
            case UPLOAD_FILE_ERROR:
                Log.i(TAG, "UI mHandler，文件上传失败，状态吗:" + errorCode + ",失败原因：" + errorMessage);
                ToastUtils.showDebug(getContext(),"文件上传失败，状态吗:" + errorCode + ",失败原因：" + errorMessage);
                break;
            case UPLOAD_FILE_SUCCESS:
                Log.i(TAG, "UI mHandler，文件上传成功");
                ToastUtils.showShort(getContext(),"切换摄像头失败");
                ToastUtils.showDebug(getContext(),"日志文件上传成功" + errorMessage);
                break;
            case RecorderConstance.RECORDER_PUSH_AUDIO_OPEN_FAILED:
                volumeButton.setImageResource(R.drawable.letv_recorder_voise_close);
                volumeButton.setClickable(false);
                isVolume = false;
                setVolume(0);
            case RecorderConstance.RECORDER_PUSH_CAMERA_OPEN_FAILED:
                isSwitchCamera = true;
                switchButton.setEnabled(true);
                if(RecorderErrorCodec.SWITCH_CAMERA_FAILE_NO_PERMISSION.equalsIgnoreCase(errorCode)){
                    showErrorDialog("请打开您的摄像头权限");
                }else {
                    showErrorDialog(errorMessage);
                }
                break;
            case RecorderConstance.RECORDER_PUSH_FLASH_OPEN_FAILED:
                isFlash = false;
                flashButton.setImageResource(R.drawable.letv_recorder_flash_light_close);
                ToastUtils.showDebug(getContext(),errorMessage);
                break;
            case RecorderConstance.RECORDER_PUSH_ALLOW_SWITCH_CAMERA:
                //允许再次切换摄像头
                isSwitchCamera = true;
                switchButton.setEnabled(true);
                break;
            case RecorderConstance.RECORDER_PUSH_CAMERA_OPEN_SUCCESS:
                CameraParams p = publisher.getCameraParams();
                if(p != null) {
                    switchCameraSuccess(p.getCameraId());
                }
                isSwitchCamera = true;
                switchButton.setEnabled(true);
                break;
        }
    }


    private PublishListener listener = new PublishListener() {
        @Override
        public void onPublish(int code, String msg, Object... obj) {
            Message message = mHandler.obtainMessage(code);
            Bundle bundle = new Bundle();
            bundle.putString("detailErrorMessage", msg);
            if(obj != null) {
                for(int i = 0;i<obj.length;i++) {
                    bundle.putString("detailObj"+i, String.valueOf(obj[0]));
                }
            }
            message.setData(bundle);
            mHandler.sendMessage(message);
        }
    };
    private static Handler uiHandler = new Handler();
    protected final Handler mHandler = new MHandler(BaseSkinView.this);
    private ISurfaceCreatedListener surfaceCreatedListener = new ISurfaceCreatedListener() {
        @Override
        public void onGLSurfaceCreatedListener() {
            Log.i(TAG, "ISurfaceCreatedListener 创建成功");
            boolean temp = publisher.getVideoRecordDevice().setZoom(zoomCurrent);
            publisher.getVideoRecordDevice().start();//打开摄像头
            Log.i(TAG, "打开摄像头，设置zoom值:" + zoomCurrent + "设置是否生效：" + temp);
            //是否是推流过程中断，然后又恢复
            if(publisher== null || !publisher.isRecording()){
                openButton.setImageResource(R.drawable.letv_recorder_open);
            }
            if (isback && skinParams.isResume()) {
                isback = false;
                openClickPublisher();
            }
            //在获取最大Zoom值得时候，必须保证Camera已经成功打开。所以把获取方法放在这个方法中
            int maxZoom = publisher.getVideoRecordDevice().getMaxZoom();
            zoomBar.setMax(maxZoom);
            zoomBar.setProgress(publisher.getVideoRecordDevice().getZoom());
        }

        @Override
        public void zoomOnTouch(int state, int zoom, int maxZoom) {
            switch (state) {
                case STATE_ZOOM_DOWN:
                    uiHandler.removeCallbacks(zoomGone);
                    if (rlSeekLayout.getVisibility() != View.VISIBLE) {
                        rlSeekLayout.setVisibility(View.VISIBLE);
                    }
                    break;
                case STATE_ZOOM_UP:
                    uiHandler.postDelayed(zoomGone, 3000);
                    break;
                case STATE_ZOOM_MOVE:
                    break;
            }
            zoomCurrent = zoom;
            zoomBar.setProgress(zoom);
        }
    };
    private Runnable zoomGone = new Runnable() {
        @Override
        public void run() {
            if (rlSeekLayout != null)
                rlSeekLayout.setVisibility(View.INVISIBLE);
        }
    };

    public SkinParams getSkinParams() {
        return skinParams;
    }

    private void decodeSkinParams(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.publisher_skin);
        skinParams.setVideoBitrate(typedArray.getInt(R.styleable.publisher_skin_videoBitrate, skinParams.getVideoBitrate()));
        skinParams.setCameraId(typedArray.getInt(R.styleable.publisher_skin_cameraId, skinParams.getCameraId()));
        skinParams.setFoceView(typedArray.getResourceId(R.styleable.publisher_skin_foceView, skinParams.getFoceView()));
        skinParams.setLanscape(typedArray.getBoolean(R.styleable.publisher_skin_isLanscape, skinParams.isLanscape()));
//        skinParams.setNetResume(typedArray.getBoolean(R.styleable.publisher_skin_isNetResume,skinParams.isNetResume()));
//        skinParams.setNowPush(typedArray.getBoolean(R.styleable.publisher_skin_isNowPush,skinParams.isNowPush()));
        skinParams.setOnAnimation(typedArray.getBoolean(R.styleable.publisher_skin_isOnAnimation, skinParams.isOnAnimation()));
        skinParams.setOnTouch(typedArray.getBoolean(R.styleable.publisher_skin_isOnTouch, skinParams.isOnTouch()));
        skinParams.setResume(typedArray.getBoolean(R.styleable.publisher_skin_isResume, skinParams.isResume()));
        skinParams.setTitle(typedArray.getString(R.styleable.publisher_skin_pushTitle));
        skinParams.setUpdateLogFile(typedArray.getBoolean(R.styleable.publisher_skin_updateLogFile, skinParams.isUpdateLogFile()));
        skinParams.setVolumeGain(typedArray.getBoolean(R.styleable.publisher_skin_isVolumeGain, skinParams.isVolumeGain()));
        skinParams.setVideoHeight(typedArray.getInt(R.styleable.publisher_skin_videoHeight, skinParams.getVideoHeight()));
        skinParams.setVideoWidth(typedArray.getInt(R.styleable.publisher_skin_videoWidth, skinParams.getVideoWidth()));
        skinParams.setFirstMachine(typedArray.getBoolean(R.styleable.publisher_skin_isFirstMachine, skinParams.isFirstMachine()));
        skinParams.setSurfaceWidth(typedArray.getInt(R.styleable.publisher_skin_surfaceWidth, skinParams.getSurfaceWidth()));
        skinParams.setSurfaceHeight(typedArray.getInt(R.styleable.publisher_skin_surfaceHeight, skinParams.getSurfaceHeight()));
        skinParams.setMirror(typedArray.getBoolean(R.styleable.publisher_skin_mirror, skinParams.isMirror()));
        skinParams.setOpenGestureZoom(typedArray.getBoolean(R.styleable.publisher_skin_isOpenGestureZoom, skinParams.isOpenGestureZoom()));
        skinParams.setPublisherModel(typedArray.getInt(R.styleable.publisher_skin_publisherModel,skinParams.getPublisherModel()));
        skinParams.setReCount(typedArray.getInt(R.styleable.publisher_skin_reCount,skinParams.getReCount()));
        typedArray.recycle();
    }

    Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            if (!showTimer) return;
            uiHandler.postDelayed(timeRunnable, 1000);
            time++;
            timerView.setText(stringForTime(time));
            if (time % 2 == 0) {
                thumdButton.setVisibility(View.VISIBLE);
            } else {
                thumdButton.setVisibility(View.INVISIBLE);
            }
        }
    };

    private String stringForTime(int timeMs) {
        int seconds = timeMs % 60;
        int minutes = (timeMs / 60) % 60;
        int hours = timeMs / 3600;
        mFormatBuilder.setLength(0);
        return mFormatter.format("%02d:%02d:%02d", hours, minutes, seconds).toString();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (publisher.isRecording()) {
                if (video_lose > 3 || audio_lose > 3) {
                    ToastUtils.showDebug(getContext(),"当前网络较差,请更换网络环境");
                }
                video_lose = 0;
                audio_lose = 0;
                uiHandler.postDelayed(runnable, 30 * 1000);
            }
        }
    };

    private int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private RePublisherRunnable reRunnable;
    private int reCount = 0;

    /**
     * 重新推流
     */
    protected void rePublisher() {
        if (reCount == 0) {
            hideTimerView();
            ToastUtils.showShort(getContext(), "推流出错，自动开始重试");
        }
        if (reCount < skinParams.getReCount()) {
            Log.w(TAG, "自动重试,当前重试次数：" + reCount);
            reCount++;
            if (reRunnable != null) {
                mHandler.removeCallbacks(reRunnable);
            }
            if (!isShowLoadingDialog()) {
                showLoadingDialog();
            }
            reRunnable = new RePublisherRunnable(this);
            mHandler.postDelayed(reRunnable, RE_TIME);
        } else {
            hideLoadingDialog();
            reCount = 0;
            Log.i(TAG, "UI mHandler，RTMP 推流失败");
            ToastUtils.showDebug(getContext(), "重试失败");
            openButton.setImageResource(R.drawable.letv_recorder_open);
            if (!NetworkUtils.isNetAvailable(getContext())) {
                showErrorDialog("本地网络异常");
            } else {
                showErrorDialog("推流重试失败");
            }
        }
    }
   private static class RePublisherRunnable implements Runnable{
        WeakReference<BaseSkinView> reference;
       public RePublisherRunnable(BaseSkinView skinView){
            reference = new WeakReference<BaseSkinView>(skinView);
       }
        @Override
        public void run() {
            BaseSkinView skinView = reference.get();
            if(skinView != null && !skinView.publisher.isRecording()){
                if(NetworkUtils.isNetAvailable(skinView.getContext())) {
                    skinView.openClickPublisher();
                }else{
                    skinView. hideLoadingDialog();
                    skinView.reCount = 0;
                    Log.i(TAG, "本地网络异常本地网络异常");
                    ToastUtils.showDebug(skinView.getContext(), "本地网络异常,重试失败");
                    skinView.openButton.setImageResource(R.drawable.letv_recorder_open);
                    skinView.showErrorDialog("本地网络异常");
                }
            }
        }
    }
    /**
     * Seekbar操作
     */
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser && publisher != null) {
                if (!publisher.getVideoRecordDevice().setZoom(progress)) {
                    seekBar.setProgress(zoomCurrent);
                } else {
                    zoomCurrent = progress;
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            uiHandler.removeCallbacks(zoomGone);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            uiHandler.postDelayed(zoomGone, 3000);
        }
    };
    /**
     *  获取原始音视频数据
     */
    private IRawFrameSourceListener rawFrameListener = new IRawFrameSourceListener() {
        @Override
        public void onVideoParam(byte[] var1, int var2) {
            Log.w(TAG, "onVideoParam: var1="+var1.length+",var2= "+var2);
        }

        @Override
        public void onVideoFrame(byte[] var1, int var2, long var3) {
            Log.d(TAG, "onVideoFrame: var1="+var1.length +",var2 = "+var2 +",var3="+var3);
        }

        @Override
        public void onAudioParam(int var1, int var2) {
            Log.w(TAG, "onAudioParam: var1="+var1+",var2="+var2);
        }

        @Override
        public void onAudioFrame(byte[] var1, int var2, long var3) {
            Log.d(TAG, "onAudioFrame: var1="+var1.length+",var2="+var2+",var3="+var3);
        }
    };

    private static class MHandler extends Handler{
        final static int PAUSE = 1;
        private WeakReference<BaseSkinView> wView;
        public MHandler(BaseSkinView view){
            wView = new WeakReference<BaseSkinView>(view);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(wView != null && wView.get() != null){
                if(msg.what == PAUSE){
                    wView.get().pause();
                }else {
                    wView.get().publisherMessage(msg);
                }
            }
        }
    }
}
