package com.example.floatingwindow;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.example.floatingwindow.service.FloatingWindowService;
import com.example.floatingwindow.utils.ScreenUtil;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String SHAREPREFERENCES_FLOATINGPARAMS = "FloatingParams";
    private static int screenMaxWidth = 0;
    private static int screenMaxHeight = 0;

    private SharedPreferences sp;
    private FloatingParams floatingParams = new FloatingParams();

    private Switch showFloatingWindowSwitch;
    private SeekBar windowWidthBar;
    private SeekBar windosHeightBar;
    private SeekBar windowTransparentBar;
    private SeekBar windowTxtSizeBar;

    public static final int REQUEST_CODE_ALERT_PERMESSION = 0;

    private FloatingWindowService.FloatingWindowBinder windowBinder;

    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            windowBinder = (FloatingWindowService.FloatingWindowBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: 已解绑service");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: main");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        screenMaxWidth = ScreenUtil.getScreenWidth(this)/5;
        screenMaxHeight = ScreenUtil.getScreenHeight(this);
        Log.d(TAG, "onCreate: 获取屏幕宽的1/5：" + screenMaxWidth + " 高度：" + screenMaxHeight );

        windowWidthBar = findViewById(R.id.main_windowWidthBar);
        windosHeightBar = findViewById(R.id.main_windowHeightBar);
        windowTransparentBar = findViewById(R.id.main_windowTransparentBar);
        windowTxtSizeBar = findViewById(R.id.main_textSizeBar);
        windowTxtSizeBar.setMax(72);
        windowWidthBar.setMax(screenMaxWidth);
        windosHeightBar.setMax(screenMaxWidth);
        sp = this.getSharedPreferences(SHAREPREFERENCES_FLOATINGPARAMS,MODE_PRIVATE);
        getSharedPreferences();

        windosHeightBar.setOnSeekBarChangeListener(new HeightBarListener());
        windowWidthBar.setOnSeekBarChangeListener(new WidthTouchListenner());
        windowTxtSizeBar.setOnSeekBarChangeListener(new TextSizeBarChangeListener());

        showFloatingWindowSwitch = findViewById(R.id.main_floatWindowSwitch);
        showFloatingWindowSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    //判断版本是否》=23（6.0以上系统才有动态获取权限功能）
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        //判断是否已经获取到了悬浮窗权限
                        if(!Settings.canDrawOverlays(MainActivity.this)){
                            Toast.makeText(MainActivity.this,"请授予悬浮窗权限",Toast.LENGTH_SHORT).show();
                            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())),REQUEST_CODE_ALERT_PERMESSION);
                        }else{
                            startFloatingWindowService();
                        }
                    } else{
                        startFloatingWindowService();
                    }
                }else{
                    windowBinder.closeFloatingWindow();
                    unbindService(sc);
                    Intent intent = new Intent(MainActivity.this,FloatingWindowService.class);
                    stopService(intent);
                }
            }
        });



    }

    private class WidthTouchListenner implements SeekBar.OnSeekBarChangeListener{

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (FloatingWindowService.isShowed){
                windowBinder.setWindowWidth(progress);
            }
            Log.d(TAG, "onProgressChanged: progress:" + progress);
            floatingParams.setWidth(progress);
            saveSharedPreferences();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    private class HeightBarListener implements SeekBar.OnSeekBarChangeListener{

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (FloatingWindowService.isShowed){
                windowBinder.setWindowHeight(progress);
            }
            floatingParams.setHeight(progress);
            saveSharedPreferences();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    private class TextSizeBarChangeListener implements SeekBar.OnSeekBarChangeListener{

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (FloatingWindowService.isShowed){
                windowBinder.setWindowTextSize(progress);
            }
            floatingParams.setTextSize(progress);
            saveSharedPreferences();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    private class TransparentBarListener implements SeekBar.OnSeekBarChangeListener{

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (FloatingWindowService.isShowed){
                windowBinder.setBackgroundTransparent(progress, 1);
            }
            floatingParams.setTextSize(progress);
            saveSharedPreferences();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ALERT_PERMESSION){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)){
                    //已获取到悬浮窗权限
                    startFloatingWindowService();
                }
            }
        }
    }

    private void startFloatingWindowService() {
        if (!FloatingWindowService.isShowed){
            Log.d(TAG, "startFloatingWindowService: ");
            Intent binderIntent = new Intent(this,FloatingWindowService.class);
            binderIntent.putExtra("floatingParams",floatingParams);
            bindService(binderIntent,sc,BIND_AUTO_CREATE);
            //startService(new Intent(this,FloatingWindowService.class));
        }else{
            Toast.makeText(MainActivity.this,"已开启悬浮窗",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: main");
        windowBinder.closeFloatingWindow();
        unbindService(sc);
        Intent intent = new Intent(MainActivity.this,FloatingWindowService.class);
        stopService(intent);
        saveSharedPreferences();
        super.onDestroy();
    }

    public void saveSharedPreferences(){
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("backgroundColor",floatingParams.getBackgroundColor())
                .putInt("width",floatingParams.getWidth())
                .putInt("height",floatingParams.getHeight())
                .putInt("backgroundColor",floatingParams.getBackgroundColor())
                .putInt("textColor",floatingParams.getTextColor())
                .putInt("textSize",floatingParams.getTextSize())
                .commit();
    }

    public void getSharedPreferences(){
        floatingParams.setBackgroundColor(sp.getInt("backgroundColor",0));
        floatingParams.setWidth(sp.getInt("width",0));
        floatingParams.setHeight(sp.getInt("height",0));
        floatingParams.setTextColor(sp.getInt("textColor",0));
        floatingParams.setTextSize(sp.getInt("textSize",0));
        windowWidthBar.setProgress(floatingParams.getWidth());
        windosHeightBar.setProgress(floatingParams.getHeight());
        windowTxtSizeBar.setProgress(floatingParams.getTextSize());
    }


    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: 拦截返回键");
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
}
