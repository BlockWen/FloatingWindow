package com.example.floatingwindow.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wendacheng on 2018/12/7.
 */

public class DateUtil {

    public static String getCurrentHHmmDate(){
        String dateStr = "";
        Date date = new Date();
        SimpleDateFormat sf = new SimpleDateFormat("HH:mm");
        dateStr = sf.format(date);
        return dateStr;
    }

}
