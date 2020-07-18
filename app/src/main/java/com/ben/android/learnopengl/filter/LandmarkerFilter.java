package com.ben.android.learnopengl.filter;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLES20;

import com.ben.android.learnopengl.R;
import com.ben.android.learnopengl.util.AndroidUtilities;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class LandmarkerFilter extends FrameBufferFilter {
    private PointF point = new PointF(500f, 500f);
    private int mPosition;
    private int pColor;
    private FloatBuffer pointBuffer;

    public LandmarkerFilter(Context context) {
        super(context);
    }

    @Override
    protected String getVertexShader(Context context) {
        return AndroidUtilities.readRawTextFile(context, R.raw.landmarker_vertex);
    }

    @Override
    protected String getFragmentShader(Context context) {
        return AndroidUtilities.readRawTextFile(context, R.raw.landmarker_fragment);
    }

    @Override
    protected void initialize() {
        super.initialize();

        mPosition = GLES20.glGetAttribLocation(glProgram, "mPosition");
        pColor = GLES20.glGetUniformLocation(glProgram, "pColor");

        pointBuffer = ByteBuffer.allocateDirect(2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    @Override
    public int render(int texture, float[] matrix) {
        //设置显示窗口
        GLES20.glViewport(0, 0, width, height);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[0]);

        //使用着色器
        GLES20.glUseProgram(glProgram);

        pointBuffer.clear();
        pointBuffer.put(point.x);
        pointBuffer.put(point.y);
        GLES20.glVertexAttribPointer(mPosition, 2, GLES20.GL_FLOAT, false, 0, pointBuffer);
        GLES20.glEnableVertexAttribArray(mPosition);


        GLES20.glUniform4f(pColor, 0.0f, 0.0f, 1.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 1);

        //返回fbo的纹理id
        return frameBufferTextures[0];
    }
}
