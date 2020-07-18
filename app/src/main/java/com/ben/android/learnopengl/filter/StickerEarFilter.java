package com.ben.android.learnopengl.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.ben.android.learnopengl.R;
import com.ben.android.learnopengl.face.Face;
import com.ben.android.learnopengl.util.AndroidUtilities;

public class StickerEarFilter extends FrameBufferFilter {
    private Bitmap mEarBitmap;
    private int[] mStickerEarTextureIds;
    private Face mFace;


    public StickerEarFilter(Context context) {
        super(context);
    }

    public void setFace(Face mFace) {
        this.mFace = mFace;
    }

    @Override
    protected String getVertexShader(Context context) {
        return AndroidUtilities.readRawTextFile(context, R.raw.screen_vertex);
    }

    @Override
    protected String getFragmentShader(Context context) {
        return AndroidUtilities.readRawTextFile(context, R.raw.screen_fragment);
    }

    @Override
    protected void initialize() {
        super.initialize();

        mStickerEarTextureIds = new int[1];
        GLES20.glGenTextures(mStickerEarTextureIds.length, mStickerEarTextureIds, 0);
        for (int i = 0; i < mStickerEarTextureIds.length; i++) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mStickerEarTextureIds[i]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mStickerEarTextureIds[0]);
        //将bitmap绑定到纹理上
        mEarBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ear);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mEarBitmap, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

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
    protected void release() {
        super.release();
        mEarBitmap.recycle();
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
        // 图像数据
        // 正常：GLES20.GL_TEXTURE_2D
        // surfaceTexure的纹理需要
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        GLES20.glUniform1i(vTexture, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        //贴图处理
        if (null == mFace || (mFace != null && mFace.getFaces().isEmpty())) {
            return frameBufferTextures[0];
        }
        //开启混合模式
        GLES20.glEnable(GLES20.GL_BLEND);
        //设置贴图模式
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        //设置窗口位置
        //人脸位置
        PointF face = mFace.getFaces().get(0);
        int x = (int) (face.x / mFace.getInputWidth() * width);
        int y = (int) ((face.y / mFace.getInputHeight() * height)) - mEarBitmap.getHeight() / 2;
        int earw = (int) ((float) mFace.getFaceWidth() / mFace.getInputWidth() * width);
        earw = (int) (mFace.getFaceWidth()*1.5);
        int earh = mEarBitmap.getHeight();
        GLES20.glViewport(x, y, earw, earh);

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
        // 图像数据
        // 正常：GLES20.GL_TEXTURE_2D
        // surfaceTexure的纹理需要
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mStickerEarTextureIds[0]);
        GLES20.glUniform1i(vTexture, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        //关闭混合模式
        GLES20.glDisable(GLES20.GL_BLEND);
        return frameBufferTextures[0];
    }
}
