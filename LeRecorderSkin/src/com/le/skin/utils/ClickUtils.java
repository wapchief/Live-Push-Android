package com.le.skin.utils;

/**
 * Created by pys on 2016/12/16.
 */

public class ClickUtils {
    private static long lastClickTime;
    private static long MIN_DELAY_TIME = 700;//小于700毫秒的点击都将取消

    /**
     * 防止快速点击
     *
     * @return
     */
    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < MIN_DELAY_TIME) {
            return true;
        }
        lastClickTime = time;
        return false;
    }
}
