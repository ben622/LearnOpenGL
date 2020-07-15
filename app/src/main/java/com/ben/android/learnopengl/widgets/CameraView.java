package com.ben.android.learnopengl.widgets;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import com.ben.android.learnopengl.filter.Filter;

public class CameraView extends GLSurfaceView {
    private CameraRender renderer;

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
        renderer = new CameraRender(this);
        setRenderer(renderer);
        //渲染方式
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        renderer.surfaceDestroyed();
    }

    public void addFilter(Filter filter) {
        if (renderer != null) {
            renderer.addFilter(filter);
        }
    }

    public void startRecord() {
        renderer.startRecord();

    }

    public void stopRecord() {
        renderer.stopRecord();

    }
}
