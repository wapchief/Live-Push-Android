package com.wapchief.live_push.network;

import android.util.Log;

import com.letv.recorder.util.MD5Utls;
import com.wapchief.live_push.model.HDModel;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by wapchief on 2017/9/20.
 * 网络访问封装
 */

public class  NetWorkManager {

    /**
     * 直播活动
     * ver=4.1
     */
    public static void searchHD(String activityId,long timestamp,Callback<HDModel> callback) {
        String signSting = "activityId" + activityId +
                "method" + MyMethod.NAME_SearchHDList +
                "timestamp" + timestamp +
                "userid" + MyMethod.userId +
                "ver" + MyMethod.VER4_1 + MyMethod.KEY;
        String sign = MD5Utls.stringToMD5(signSting);
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Url.LIVE_URL_ROOT)
                .build();
        Api aPi = retrofit.create(Api.class);
        Call<HDModel> call = aPi.getSearchList(MyMethod.NAME_SearchHDList,MyMethod.VER4_1, MyMethod.userId, timestamp, activityId, sign);//传入参数
        call.enqueue(callback);

    }

    /*
    *获取直播推流地址
    * ver=4.0
    */
    public static void getPushUrl(String activityId,long timestamp,Callback<ResponseBody> callback){
        String signSting = "activityId" + activityId +
                "method" + MyMethod.NAME_PushUrl +
                "timestamp" + timestamp +
                "userid" + MyMethod.userId +
                "ver" + MyMethod.VER4_0 + MyMethod.KEY;
        Log.e("onsingString====", signSting);
        String sign = MD5Utls.stringToMD5(signSting);
        Retrofit retrofit = new Retrofit.Builder()//创建Rectfit对象
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Url.LIVE_URL_ROOT)//传入地址
                .build();
        Api aPi = retrofit.create(Api.class);//实例化接口对象
        Call<ResponseBody> call = aPi.getPushUrl2(MyMethod.NAME_PushUrl, MyMethod.VER4_0, MyMethod.userId, timestamp, activityId, sign);
        call.enqueue(callback);
    }

}
