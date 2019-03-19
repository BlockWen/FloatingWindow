package com.example.floatingwindow;

import java.io.Serializable;

/**
 * Created by wendacheng on 2018/12/12.
 * 悬浮窗参数 ，长宽背景颜色，字体颜色大小
 */

public class FloatingParams implements Serializable{

    private int width;
    private int height;
    private int backgroundColor;
    private int textColor;
    private int textSize;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }
}
