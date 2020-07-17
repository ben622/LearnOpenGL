package com.ben.android.learnopengl.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.ben.android.learnopengl.R;
import com.ben.android.learnopengl.util.AndroidUtilities;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * FBO离屏渲染
 */
public class CameraFilter extends FrameBufferFilter{

    public CameraFilter(Context context) {
        super(context);
    }

    @Override
    protected String getVertexShader(Context context) {
        return AndroidUtilities.readRawTextFile(context, R.raw.camera_vertex);
    }

    @Override
    protected String getFragmentShader(Context context) {
        return AndroidUtilities.readRawTextFile(context, R.raw.camera_fragment);
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

        //矩阵
        GLES20.glUniformMatrix4fv(vMatrix, 1, false, matrix, 0);


        //片元着色器属性
        //激活 texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        // 图像数据
        // 正常：GLES20.GL_TEXTURE_2D
        // surfaceTexure的纹理需要
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture);
        GLES20.glUniform1i(vTexture, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        return frameBufferTextures[0];
    }
}
