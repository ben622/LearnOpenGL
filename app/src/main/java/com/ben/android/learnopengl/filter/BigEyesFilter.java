package com.ben.android.learnopengl.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.ben.android.learnopengl.R;
import com.ben.android.learnopengl.face.Face;
import com.ben.android.learnopengl.util.AndroidUtilities;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class BigEyesFilter extends FrameBufferFilter {
    private int left_eye;
    private int right_eye;
    private FloatBuffer left;
    private FloatBuffer right;

    private Face mFace;

    public void setFace(Face mFace) {
        this.mFace = mFace;
    }

    public BigEyesFilter(Context context) {
        super(context);
    }

    @Override
    protected String getVertexShader(Context context) {
        return AndroidUtilities.readRawTextFile(context, R.raw.bigeye_vertex);
    }

    @Override
    protected String getFragmentShader(Context context) {
        return AndroidUtilities.readRawTextFile(context, R.raw.bigeye_fragment);
    }


    @Override
    protected void initialize() {
        super.initialize();
        left_eye = GLES20.glGetUniformLocation(glProgram, "left_eye");
        right_eye = GLES20.glGetUniformLocation(glProgram, "right_eye");

        left = ByteBuffer.allocateDirect(2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        right = ByteBuffer.allocateDirect(2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

        textureBuffer.clear();
        //从opengl画到opengl 不是画到屏幕， 修改坐标
        float[] TEXTURE = {
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f
        };
        textureBuffer.put(TEXTURE);

    }

    @Override
    public int render(int texture, float[] matrix) {
        if (null == mFace || (mFace != null && mFace.getFaces().isEmpty()) || (mFace != null && mFace.getMarker().isEmpty())) {
            return texture;
        }
        //设置显示窗口
        GLES20.glViewport(0, 0, width, height);

        //不调用的话就是默认的操作glsurfaceview中的纹理了。显示到屏幕上了
        //这里我们还只是把它画到fbo中(缓存)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[0]);

        //使用着色器
        GLES20.glUseProgram(glProgram);

        //传递坐标
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);

        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);


        /**
         * 传递眼睛的坐标 给GLSL
         */
        //左眼的x 、y  opengl : 0-1
        float x = mFace.getMarker().get(0).x / mFace.getInputWidth();
        float y = mFace.getMarker().get(0).x / mFace.getInputHeight();
        left.clear();
        left.put(x);
        left.put(y);
        left.position(0);
        GLES20.glUniform2fv(left_eye, 1, left);

        //右眼的x、y
        x = mFace.getMarker().get(1).x / mFace.getInputWidth();
        y = mFace.getMarker().get(1).x / mFace.getInputHeight();
        right.clear();
        right.put(x);
        right.put(y);
        right.position(0);
        GLES20.glUniform2fv(right_eye, 1, right);


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //因为这一层是摄像头后的第一层，所以需要使用扩展的  GL_TEXTURE_EXTERNAL_OES
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        GLES20.glUniform1i(vTexture, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        //返回fbo的纹理id
        return frameBufferTextures[0];
    }
}
