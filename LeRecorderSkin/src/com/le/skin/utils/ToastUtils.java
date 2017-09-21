package com.le.skin.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by xiaoqiang on 2017/4/5.
 */

public class ToastUtils {
    public static boolean isShowDebugToast =  true;
    public static void showShort(Context mContext,String msg){
        Log.i("ToastUtils",msg);
        showToast(mContext,msg);
    }
    public static void showDebug(Context mContext,String msg){
        if(msg == null) return;
        if(isShowDebugToast){
            showShort(mContext,msg);
        }
    }
    private static void showToast(final Context mContext, String msg){
        Toast.makeText(mContext,msg,Toast.LENGTH_SHORT).show();
    }
}
