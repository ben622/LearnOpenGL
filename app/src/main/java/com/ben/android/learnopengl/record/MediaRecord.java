package com.ben.android.learnopengl.record;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.opengl.EGLContext;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;


public class MediaRecord {
    private Context context;
    private int width;
    private int height;
    private String path;
    private Surface mSurface;
    private Handler mHandler;

    private EGLContext eglContext;
    private EGLHelper eglHelper;
    private MediaCodec mediaCodec;
    private MediaMuxer mediaMuxer;

    private boolean isRecording;
    private int index;

    public MediaRecord(Context context, String path, int width, int height, EGLContext eglContext) {
        this.context = context;
        this.width = width;
        this.height = height;
        this.path = path;
        this.eglContext = eglContext;
    }

    public void render(int textureId, long timestamp) {
        if (!isRecording) return;
        mHandler.post(() -> {
            eglHelper.render(textureId, timestamp);
            //MediaCode 编码
            encode(false);
        });
    }

    private void encode(boolean stop) {

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        while (true) {
            int status = mediaCodec.dequeueOutputBuffer(bufferInfo, 10_000);
            if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!stop) {
                    break;
                }
            } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                //开始编码时就会调用
                MediaFormat outputFormat = mediaCodec.getOutputFormat();
                //添加视频流
                index = mediaMuxer.addTrack(outputFormat);
                mediaMuxer.start();
            } else if (status == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                //todo nothing.
            } else {
                //从MediaCodec输出缓冲区中获取
                ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(status);
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    bufferInfo.size = 0;
                }
                if (bufferInfo.size != 0) {
                    //偏移
                    outputBuffer.position(bufferInfo.offset);
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                    mediaMuxer.writeSampleData(index, outputBuffer, bufferInfo);
                }
                //释放输出缓冲区
                mediaCodec.releaseOutputBuffer(status, false);
                //如果读到结尾了
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break;
                }

            }
        }
    }

    public void start() {
        try {
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
            //从Surface中获取颜色格式
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 400000);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
            if (Build.VERSION.SDK_INT >= 21) {
                mediaFormat.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileHigh);
                if (Build.VERSION.SDK_INT >= 23) {
                    mediaFormat.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel5);
                }
            }
            mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mSurface = mediaCodec.createInputSurface();

            //创建混合器
            mediaMuxer = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            HandlerThread handlerThread = new HandlerThread("MediaRecord");
            handlerThread.start();
            mHandler = new Handler(handlerThread.getLooper());
            mHandler.post(() -> {
                eglHelper = new EGLHelper(context, width, height, mSurface, eglContext);
                mediaCodec.start();
                isRecording = true;
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (!isRecording) return;
        mHandler.post(() -> {
            encode(true);
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
            mediaMuxer.stop();
            mediaMuxer.release();
            mediaMuxer = null;
            eglHelper.release();
            eglHelper = null;
            mSurface = null;
            mHandler.getLooper().quitSafely();
            mHandler = null;
        });

    }

}
