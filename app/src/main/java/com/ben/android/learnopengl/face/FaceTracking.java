package com.ben.android.learnopengl.face;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.ben.android.learnopengl.BuildConfig;
import com.ben.android.learnopengl.util.AndroidUtilities;

import java.lang.invoke.MethodHandle;
import java.lang.ref.WeakReference;

public class FaceTracking {
    private static final String OPENCV_MODEL = "lbpcascade_frontalface.xml";
    private static final String SEETAFACE_5_MODEL = "pd_2_00_pts5.dat";
    private static final String SEETAFACE_81_MODEL = "pd_2_00_pts81.dat";
    private static final int WHAT_DETECTOR = 0X10;
    private Handler mHandler;

    private String opencvModelPath;
    private String seetaModelPath;
    private WeakReference<Context> contextWeakReference;

    private Face face;
    private long ptr;

    static {
        System.loadLibrary("learnopengl");
    }

    private FaceTracking(Context context) {
        contextWeakReference = new WeakReference<>(context);

        //在子线程中进行关键点追踪
        HandlerThread handlerThread = new HandlerThread("FaceTracking");
        handlerThread.start();

        mHandler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == WHAT_DETECTOR) {
                    Object[] args = (Object[]) msg.obj;
                    byte[] data = (byte[]) args[0];
                    int width = (int) args[1];
                    int height = (int) args[2];
                    int cameraId = (int) args[3];
                    nativeDetector(data, width, height, cameraId, (face = Face.empty()));
                    if (BuildConfig.DEBUG) Log.i("FaceTracking", "detector:" + face.toString());
                }
            }
        };
        mHandler.post(this::inital);
    }

    private void inital() {
        opencvModelPath = AndroidUtilities.copyAssetsFileToSdcard(contextWeakReference.get(), OPENCV_MODEL);
        seetaModelPath = AndroidUtilities.copyAssetsFileToSdcard(contextWeakReference.get(), SEETAFACE_5_MODEL);
        if (TextUtils.isEmpty(opencvModelPath) || TextUtils.isEmpty(seetaModelPath))
            Log.e("FaceTracking", "model path error!");
        ptr = nativeInit(opencvModelPath, seetaModelPath);
        if (BuildConfig.DEBUG)  Log.i("FaceTracking", "native tracking ptr:" + ptr);
        if (ptr == 0) {
            Log.e("FaceTracking", "create ptr error!");
        }
    }


    public static FaceTracking createFaceTracking(Context context) {
        return new FaceTracking(context);
    }


    public void start() {
        if (ptr == 0) return;
        synchronized (this) {
            nativeStart();
        }
    }

    public void stop() {
        if (ptr == 0) return;
        synchronized (this) {
            nativeStop();
        }
    }

    public void detector(byte[] data, int wdith, int height, int cameraId) {
        if (ptr == 0) return;
        //移除队列中待检测的任务
        mHandler.removeMessages(WHAT_DETECTOR);
        Message message = Message.obtain();
        message.what = WHAT_DETECTOR;
        message.obj = new Object[]{data, wdith, height, cameraId};
        mHandler.sendMessage(message);
    }

    public Face getFace() {
        return face;
    }

    private native long nativeInit(String opencvModel, String seetaModel);

    private native void nativeStart();

    private native void nativeStop();

    private native void nativeDetector(byte[] data, int wdith, int height, int cameraId,Face face);

}
