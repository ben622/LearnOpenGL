package com.ben.android.learnopengl.record;

import android.content.Context;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.view.Surface;

import com.ben.android.learnopengl.filter.ScreenFilter;

import javax.microedition.khronos.egl.EGL10;


public class EGLHelper {

    private EGLDisplay mEglDisplay;
    private EGLConfig mEglConfig;
    private EGLContext mEglContext;
    private EGLSurface mEglSurface;
    private ScreenFilter screenFilter;

    public EGLHelper(Context context, int width, int height, Surface surface, EGLContext eglContext) {
        mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);

        if (mEglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed");
        }

        int[] version = new int[2];
        if (!EGL14.eglInitialize(mEglDisplay, version, 0, version, 1)) {
            throw new RuntimeException("eglInitialize failed");
        }

        int[] num_config = new int[1];
        int[] attrib_list = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_NONE};
        EGLConfig[] eglConfigs = new EGLConfig[1];
        if (!EGL14.eglChooseConfig(mEglDisplay, attrib_list, 0, eglConfigs, 0, eglConfigs.length, num_config, 0)) {
            throw new IllegalArgumentException("eglChooseConfig failed");
        }
        mEglConfig = eglConfigs[0];

        //create egl context.
        int[] context_attrib_list = {EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE};
        mEglContext = EGL14.eglCreateContext(mEglDisplay, mEglConfig, eglContext, context_attrib_list, 0);
        if (mEglConfig == null) {
            throw new IllegalArgumentException("create egl context failed");
        }

        //create surface
        mEglSurface = EGL14.eglCreateWindowSurface(mEglDisplay, mEglConfig, surface, null, 0);
        if (mEglSurface == null) {
            throw new IllegalArgumentException("eglCreateWindowSurface  failed");
        }

        /*
         * Before we can issue GL commands, we need to make sure
         * the context is current and bound to a surface.
         */
        if (!EGL14.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
            throw new IllegalArgumentException("eglMakeCurrent  failed");
        }

        screenFilter = new ScreenFilter(context);
        screenFilter.onSurfaceChanged(width, height);
    }

    public void render(int textureId, long timestamp) {
        /*
         * 将当前线程与GL上下文绑定
         */
        if (!EGL14.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
            throw new IllegalArgumentException("eglMakeCurrent  failed");
        }
        screenFilter.render(textureId, null);

        //刷新缓冲区
        EGLExt.eglPresentationTimeANDROID(mEglDisplay, mEglSurface, timestamp);

        EGL14.eglSwapBuffers(mEglDisplay, mEglSurface);
    }

    public void release() {
        EGL14.eglDestroySurface(mEglDisplay, mEglSurface);
        EGL14.eglMakeCurrent(mEglDisplay, mEglSurface, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroyContext(mEglDisplay, mEglContext);
        EGL14.eglReleaseThread();
        EGL14.eglTerminate(mEglDisplay);
    }
}
