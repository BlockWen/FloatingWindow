package com.example.floatingwindow.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.widget.CardView;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.floatingwindow.FloatingParams;
import com.example.floatingwindow.MainActivity;
import com.example.floatingwindow.R;
import com.example.floatingwindow.utils.DateUtil;

public class FloatingWindowService extends Service {

    private static final int HANDLER_CODE_CHANGETXT = 0;

    private static final int HANDLER_CODE_TEXTSIZE_CHANGE = 1;

    private static final int HANDLER_CODE_BACKGROUDCOLOR_CHANGE = 2;

    private static final int HANDLER_CODE_TEXTCOLOR_CHANGE = 3;

    private int style = 0;

    private static final String TAG = "FloatingWindowService";

    public static boolean isShowed = false;

    public static boolean isMoved = false;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private View layout;
    private CardView cardView;
    private TextView textView ;

    private WindowHandler wHandler = new WindowHandler();

    private FloatingWindowBinder fwBinder = new FloatingWindowBinder();

    private FloatingParams floatingParams;

    public FloatingWindowService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: 执行了");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand: 执行了" );
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d(TAG, "onBind: ");
        floatingParams = (FloatingParams) intent.getSerializableExtra("floatingParams");
        layout = LayoutInflater.from(getApplicationContext()).inflate(R.layout.lay_floating_time,null);
        cardView = layout.findViewById(R.id.time_cardView);
        cardView.setBackgroundColor(getResources().getColor(R.color.colorBlue));
        textView = layout.findViewById(R.id.floating_style_time_text);
        textView.setText("");
        textView.setGravity(Gravity.CENTER);
        textView.setBackgroundColor(getResources().getColor(R.color.colorBlue));
        if (floatingParams.getTextSize() != 0 ){
           textView.setTextSize(floatingParams.getTextSize());
        }
        //textView.setTextSize(floatingParams.getTextSize() == 0 ? textView.getTextSize(): floatingParams.getTextSize());
        layout.setOnTouchListener(new WindowOnTouchListenter());
        layout.setOnClickListener(new WindowOnClickListener());
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else{
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = floatingParams.getWidth() == 0 ? 100 : floatingParams.getWidth();
        layoutParams.height = floatingParams.getHeight() == 0 ? 50 : floatingParams.getHeight();
        layoutParams.x = 0;
        layoutParams.y = 0;
        windowManager.addView(layout,layoutParams);
        isShowed = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    setWindowDate();
                }
            }
        }).start();
        return fwBinder;
    }

    private class WindowOnTouchListenter implements View.OnTouchListener{

        private int x;
        private int y;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    Log.d(TAG, "onTouch: down");
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.d(TAG, "onTouch: move");
                    //当前xy的值
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    //计算移动xy的距离
                    int moveX = nowX - x;
                    int moveY = nowY - y;
                    //赋值给xy
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + moveX;
                    layoutParams.y = layoutParams.y + moveY;
                    windowManager.updateViewLayout(layout,layoutParams);
                    isMoved = true;
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "onTouch: up move:" + isMoved);
                    if (isMoved) {
                        isMoved = false;
                        return true;
                    }
                default:
                    break;
            }
            return false;
        }

    }

    private class WindowOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick: 启动activity");
            Intent intent = new Intent(FloatingWindowService.this,MainActivity.class);
            startActivity(intent);
        }
    }

    public class FloatingWindowBinder extends Binder{

        public void setWindowWidth(int width){
            Log.d(TAG, "setWindowWidth: " + width);
            layoutParams.width = width;
            windowManager.updateViewLayout(layout,layoutParams);
        }

        public void setWindowHeight(int height){
            layoutParams.height = height;
            windowManager.updateViewLayout(layout,layoutParams);
        }

        public void setWindowTextSize(int textSize){
            whandlerMsg(HANDLER_CODE_TEXTSIZE_CHANGE,0,0,textSize);
        }

        public void setBackgroundTransparent(int transparent,int color){
            whandlerMsg(HANDLER_CODE_BACKGROUDCOLOR_CHANGE,0,0,transparent);
        }

        public void closeFloatingWindow(){
            windowManager.removeView(layout);
            isShowed = false;
        }

    }

    public void setWindowDate(){
        whandlerMsg(HANDLER_CODE_CHANGETXT,0,0, DateUtil.getCurrentHHmmDate());
    }

    public void whandlerMsg(int code,int arg1,int arg2,Object obj){
        Message msg = new Message();
        msg.what = code;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.obj = obj;
        wHandler.sendMessage(msg);
    }

    private class WindowHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case HANDLER_CODE_CHANGETXT:
                    textView.setText(msg.obj.toString());
                    break;
                case HANDLER_CODE_TEXTSIZE_CHANGE:
                    textView.setTextSize((int)msg.obj);
                    break;
                case HANDLER_CODE_BACKGROUDCOLOR_CHANGE:
                    cardView.setBackgroundColor(Color.argb(1,1,1,1));
                    break;
                default:
                    break;
            }
        }
    }
}
