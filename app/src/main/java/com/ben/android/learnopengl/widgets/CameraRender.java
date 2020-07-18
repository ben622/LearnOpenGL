package com.ben.android.learnopengl.widgets;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.ben.android.learnopengl.face.FaceTracking;
import com.ben.android.learnopengl.filter.BeautyFilter;
import com.ben.android.learnopengl.filter.BigEyesFilter;
import com.ben.android.learnopengl.filter.CameraFilter;
import com.ben.android.learnopengl.filter.Filter;
import com.ben.android.learnopengl.filter.ScreenFilter;
import com.ben.android.learnopengl.filter.StickerEarFilter;
import com.ben.android.learnopengl.record.MediaRecord;
import com.ben.android.learnopengl.util.CameraHelper;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener, Camera.PreviewCallback {
    private CameraView cameraView;
    private CameraHelper cameraHelper;
    private SurfaceTexture surfaceTexture;
    private int[] textures;
    private float[] mtx = new float[16];

    private Filter mCameraFilter;
    private Filter mScreenFilter;
    //叠加的滤镜
    private List<Filter> filters = new ArrayList<>();
    private MediaRecord mediaRecord;
    private FaceTracking faceTracking;

    public CameraRender(CameraView cameraView) {
        this.cameraView = cameraView;
    }

    public void addFilter(Filter filter) {
        this.filters.add(filter);
    }
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        cameraHelper = new CameraHelper(Camera.CameraInfo.CAMERA_FACING_FRONT);
        cameraHelper.setPreviewCallback(this);
        textures = new int[1];
        //创建纹理
        GLES20.glGenTextures(textures.length, textures, 0);
        surfaceTexture = new SurfaceTexture(textures[0]);
        //设置数据可用时回调刷新
        surfaceTexture.setOnFrameAvailableListener(this);
        //init filter
        mCameraFilter = new CameraFilter(cameraView.getContext());
        mScreenFilter = new ScreenFilter(cameraView.getContext());
        filters.add(new BeautyFilter(cameraView.getContext()));
        filters.add(new BigEyesFilter(cameraView.getContext()));
        filters.add(new StickerEarFilter(cameraView.getContext()));
        //filters.add(new LandmarkerFilter(cameraView.getContext()));

    }


    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        if (faceTracking != null) {
            faceTracking.stop();
        }
        faceTracking = FaceTracking.createFaceTracking(cameraView.getContext());

        cameraHelper.startPreview(surfaceTexture);
        mCameraFilter.onSurfaceChanged(width, height);
        mScreenFilter.onSurfaceChanged(width, height);
        for (Filter filter : filters) {
            filter.onSurfaceChanged(width, height);
        }
        if (mediaRecord != null) {
            mediaRecord.stop();
        }
        mediaRecord = new MediaRecord(cameraView.getContext(), "/sdcard/11111.mp4", CameraHelper.HEIGHT, CameraHelper.WIDTH, EGL14.eglGetCurrentContext());



    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        //窗口重置
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //This may only be called while the OpenGL ES context that owns the texture is current on the calling thread. It will implicitly bind its texture to the GL_TEXTURE_EXTERNAL_OES texture target.
        surfaceTexture.updateTexImage();
        surfaceTexture.getTransformMatrix(mtx);
        int textureId = mCameraFilter.render(textures[0], mtx);
        for (Filter filter : filters) {
            if (filter instanceof BigEyesFilter) {
                ((BigEyesFilter) filter).setFace(faceTracking.getFace());
            }
            if (filter instanceof StickerEarFilter) {
                ((StickerEarFilter) filter).setFace(faceTracking.getFace());
            }
            textureId = filter.render(textureId, null);
        }
        textureId = mScreenFilter.render(textureId, null);

        mediaRecord.render(textureId, surfaceTexture.getTimestamp());

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        cameraView.requestRender();
    }

    public void startRecord() {
        mediaRecord.start();
    }

    public void stopRecord() {
        mediaRecord.stop();
    }
    public void surfaceDestroyed() {
        cameraHelper.stopPreview();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (faceTracking != null) {
            faceTracking.detector(data, CameraHelper.WIDTH, CameraHelper.HEIGHT, cameraHelper.getCameraId());
        }
    }
}
