package com.wapchief.live_push.network;

/**
 * Created by wapchief on 2017/9/20.
 * 用户请求的方法名
 */

public class MyMethod {
    //活动列表
    public static final String NAME_SearchHDList = "lecloud.cloudlive.activity.search";
    //获取直播活动流的信息
    public static final String NAME_Search = "lecloud.cloudlive.vrs.activity.streaminfo.search";
    //获取直播流的Token
    public static final String NAME_GetPushToken = "lecloud.cloudlive.activity.getPushToken";
    //活动修改
    public static final String NAME_Update_HD = "lecloud.cloudlive.activity.modify";

    public static final String NAME_PushUrl = "lecloud.cloudlive.activity.getPushUrl";

    public static final String VER4_0 = "4.0";
    public static final String VER4_1 = "4.1";
    public static int userId = 922294;
    public static String KEY = "891d3747c27e715cf8018eb8352a9d7b";


}
