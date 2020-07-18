package com.ben.android.learnopengl.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.ben.android.learnopengl.R;
import com.ben.android.learnopengl.util.AndroidUtilities;

public class BeautyFilter  extends FrameBufferFilter{
    private int mBeautyWidth;
    private int mBeautyHeight;
    public BeautyFilter(Context context) {
        super(context);
    }

    @Override
    protected String getVertexShader(Context context) {
        return AndroidUtilities.readRawTextFile(context, R.raw.beauty_vertex);
    }

    @Override
    protected String getFragmentShader(Context context) {
        return AndroidUtilities.readRawTextFile(context, R.raw.beauty_fragment2);
    }

    @Override
    protected void initialize() {
        super.initialize();
        textureBuffer.clear();
        //从opengl画到opengl 不是画到屏幕， 修改坐标
        float[] TEXTURE = {
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f
        };
        textureBuffer.put(TEXTURE);

        mBeautyWidth = GLES20.glGetUniformLocation(glProgram, "width");
        mBeautyHeight = GLES20.glGetUniformLocation(glProgram, "height");
    }

    @Override
    public int render(int texture, float[] matrix) {
        //设置窗口
        GLES20.glViewport(0, 0, width, height);

        //操作FBO buffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[0]);

        //使用着色器
        GLES20.glUseProgram(glProgram);

        //将顶点数据添加到着色器中
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);

        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);




        //片元着色器属性
        //激活 texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(mBeautyWidth,width);
        GLES20.glUniform1i(mBeautyHeight,height);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        GLES20.glUniform1i(vTexture, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        return frameBufferTextures[0];
    }
}
