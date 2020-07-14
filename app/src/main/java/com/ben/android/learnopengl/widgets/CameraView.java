package com.ben.android.learnopengl.widgets;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class CameraView extends GLSurfaceView {
    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        //设置GL版本
        setEGLContextClientVersion(2);
        //渲染器
        setRenderer(new CameraRender(this));
        //渲染方式
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }
}
