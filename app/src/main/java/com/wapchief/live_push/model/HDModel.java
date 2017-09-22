package com.wapchief.live_push.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wapchief on 2017/9/22.
 */

public class HDModel implements Serializable{


    /**
     * total : 1
     * rows : [{"needRecord":0,"createTime":"1505720114000","needIpWhiteList":0,"pushUrlValidTime":0,"neededPushAuth":1,"endTime":1514563200000,"activityId":"A201709180000043","startTime":1505720113000,"coverImgUrl":"","description":"欢迎观看哈喽的精彩直播","playMode":0,"activityStatus":1,"activityCategory":"001","needTimeShift":0,"liveNum":1,"pushIpWhiteList":"","activityName":"哈喽","needFullView":0}]
     */

    public int total;
    public List<RowsBean> rows;

    public static class RowsBean implements Serializable{
        /**
         * needRecord : 0
         * createTime : 1505720114000
         * needIpWhiteList : 0
         * pushUrlValidTime : 0
         * neededPushAuth : 1
         * endTime : 1514563200000
         * activityId : A201709180000043
         * startTime : 1505720113000
         * coverImgUrl :
         * description : 欢迎观看哈喽的精彩直播
         * playMode : 0
         * activityStatus : 1
         * activityCategory : 001
         * needTimeShift : 0
         * liveNum : 1
         * pushIpWhiteList :
         * activityName : 哈喽
         * needFullView : 0
         */

        public int needRecord;
        public String createTime;
        public int needIpWhiteList;
        public int pushUrlValidTime;
        public int neededPushAuth;
        public long endTime;
        public String activityId;
        public long startTime;
        public String coverImgUrl;
        public String description;
        public int playMode;
        public int activityStatus;
        public String activityCategory;
        public int needTimeShift;
        public int liveNum;
        public String pushIpWhiteList;
        public String activityName;
        public int needFullView;
    }
}
