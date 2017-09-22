package com.wapchief.live_push.network;

import com.wapchief.live_push.model.HDModel;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by wapchief on 2017/9/18.
 */

public interface Api {

    /*获取推流地址*/
    @GET("live/execute")
    Call<ResponseBody> getPushUrl2(
            @Query("method") String method
            , @Query("ver") String ver
            , @Query("userid") int userid
            , @Query("timestamp") long timestamp
            , @Query("activityId") String activityId
            , @Query("sign") String sign);

    /*查询活动*/
    @GET("live/execute")
    Call<HDModel> getSearchList(
            @Query("method") String method
            , @Query("ver") String ver
            , @Query("userid") int userid
            , @Query("timestamp") long timestamp
            , @Query("activityId") String activityId
            , @Query("sign") String sign);
}
