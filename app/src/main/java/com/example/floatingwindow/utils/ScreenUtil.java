package com.example.floatingwindow.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * Created by wendacheng on 2018/12/10.
 * 用于获取屏幕信息
 */

public class ScreenUtil {

    public static int getScreenWidth(Context context){
        int width = 0;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dms = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dms);
        width = dms.widthPixels;
        return width;
    }

    public static int getScreenHeight(Context context){
        int height = 0;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dms = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dms);
        height = dms.heightPixels;
        return height;
    }

}
