package com.le.skin.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.letv.recorder.bean.CameraParams;
import com.letv.recorder.letvrecorderskin.R;
public class FilterListView {
    private FilterListListener listListener;
    private FrameLayout firstView;
    private FrameLayout currentView;
    private RelativeLayout filterLayout;
    private RelativeLayout viewGroup;
    private LayoutInflater inflater;
    private SeekBar levelSeek;
    private final static int[] imagefilters = new int[]{R.drawable.filter_thumb_original,R.drawable.filter_thumb_beautyskin,R.drawable.filter_thumb_romance,R.drawable.filter_thumb_warm,R.drawable.filter_thumb_calm};
    private final static int[] filters = new int[]{CameraParams.FILTER_VIDEO_NONE, CameraParams.FILTER_VIDEO_DEFAULT,CameraParams.FILTER_VIDEO_ROMANCE,CameraParams.FILTER_VIDEO_WARM,CameraParams.FILTER_VIDEO_CALM};
    public FilterListView(RelativeLayout view,Context context,FilterListListener listener){
        this.listListener = listener;
        inflater = LayoutInflater.from(context);
        this.viewGroup = view;
        filterLayout = (RelativeLayout) inflater.inflate(R.layout.le_recorder_filter_list,null);
        LinearLayout ll = (LinearLayout) filterLayout.findViewById(R.id.ll_filter_list);
        ImageView cancle = (ImageView) filterLayout.findViewById(R.id.btn_filter_cancle);
        ImageView save = (ImageView) filterLayout.findViewById(R.id.btn_filter_save);
        firstView =  addImage(ll,imagefilters[0]);
        firstView.setTag(filters[0]);
        filterLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideFilter();
//                currentView.findViewById(R.id.filter_thumb_selected).setVisibility(View.GONE);
//                listListener.selectFilter((Integer) firstView.getTag());
//                currentView = firstView;
//                currentView.findViewById(R.id.filter_thumb_selected).setVisibility(View.VISIBLE);
                return true;
            }
        });
        filterLayout.findViewById(R.id.rl_test).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        for (int i =1;i<imagefilters.length;i++) {
            addImage(ll,imagefilters[i]).setTag(filters[i]);
        }
        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentView.findViewById(R.id.filter_thumb_selected).setVisibility(View.GONE);
                currentView = firstView;
                currentView.findViewById(R.id.filter_thumb_selected).setVisibility(View.VISIBLE);
                hideFilter();
                listListener.selectFilter((Integer) firstView.getTag());
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideFilter();
            }
        });
        levelSeek = (SeekBar) filterLayout.findViewById(R.id.seekB_filter_level);
        levelSeek.setOnSeekBarChangeListener(changeListener);
        filterLayout.findViewById(R.id.rl_filter).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }
    public void showFilter(){
        hideFilter();
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        this.viewGroup.addView(filterLayout,params);
    }
    public void setFilterLevel(int level){
        levelSeek.setProgress(level);
    }
    public void hideFilter(){
        this.viewGroup.removeView(filterLayout);
    }

    private FrameLayout addImage(LinearLayout layout, int imgRes){
        FrameLayout filterRoot = (FrameLayout) inflater.inflate(R.layout.le_filter_item_layout,null);
        ImageView imageView = (ImageView) filterRoot.findViewById(R.id.filter_thumb_image);
        imageView.setImageResource(imgRes);
        layout.addView(filterRoot);
        filterRoot.setOnClickListener(clickListener);
        if(currentView == null){
            currentView = filterRoot;
        }
        currentView.findViewById(R.id.filter_thumb_selected).setVisibility(View.VISIBLE);
        return filterRoot;
    }
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            currentView.findViewById(R.id.filter_thumb_selected).setVisibility(View.GONE);
            listListener.selectFilter((Integer) v.getTag());
            v.findViewById(R.id.filter_thumb_selected).setVisibility(View.VISIBLE);
            currentView = (FrameLayout) v;
        }
    };

    public interface  FilterListListener{
        void selectFilter(int current);
        void chageFilterLevel(int level);
    }
    private SeekBar.OnSeekBarChangeListener changeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser && listListener != null){
                listListener.chageFilterLevel(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
}
