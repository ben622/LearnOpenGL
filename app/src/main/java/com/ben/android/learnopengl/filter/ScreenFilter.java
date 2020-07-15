package com.ben.android.learnopengl.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.ben.android.learnopengl.R;
import com.ben.android.learnopengl.util.AndroidUtilities;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class ScreenFilter extends Filter {
    //着色器中变量的索引
    private int vPosition;
    private int vCoord;
    private int vTexture;


    private int glProgram;
    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;

    public ScreenFilter(Context context) {
        super(context);
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
        //创建定点着色器
        int vertexShaderId = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        //绑定顶点着色器到GL
        GLES20.glShaderSource(vertexShaderId, vertexShader);
        //编译定点着色器
        GLES20.glCompileShader(vertexShaderId);
        //验证顶点着色器编译是否成功
        int[] status = new int[1];
        GLES20.glGetShaderiv(vertexShaderId, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            throw new IllegalArgumentException("vertex shader complie error!");
        }

        //创建片元着色器
        int fragmentShaderId = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        //绑定片元着色器到GL
        GLES20.glShaderSource(fragmentShaderId, fragmentShader);
        //编译片元着色器
        GLES20.glCompileShader(fragmentShaderId);
        //验证片元着色器编译是否成功
        GLES20.glGetShaderiv(fragmentShaderId, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            throw new IllegalArgumentException("fragment shader complie error!");
        }

        //创建着色器程序
        glProgram = GLES20.glCreateProgram();
        //将着色器附加到program
        GLES20.glAttachShader(glProgram, vertexShaderId);
        GLES20.glAttachShader(glProgram, fragmentShaderId);

        //连接着色器程序
        GLES20.glLinkProgram(glProgram);
        //验证着色器程序是否成功
        GLES20.glGetProgramiv(glProgram, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            throw new IllegalArgumentException("link program error!");
        }

        //移除shander
        GLES20.glDeleteShader(vertexShaderId);
        GLES20.glDeleteShader(fragmentShaderId);

        //获得Vertex和Fragment着色器中的变量索引
        vPosition = GLES20.glGetAttribLocation(glProgram, "vPosition");
        vCoord = GLES20.glGetAttribLocation(glProgram, "vCoord");
        vTexture = GLES20.glGetUniformLocation(glProgram, "vTexture");

        //创建数据缓冲器
        vertexBuffer = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.clear();
        float[] vers = {-1.0f, -1.0f,
                1.0f, -1.0f,
                -1.0f, 1.0f,
                1.0f, 1.0f};
        vertexBuffer.put(vers);

        textureBuffer = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureBuffer.clear();

        float[] frags = {
                0.0f, 1.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f
        };
        textureBuffer.put(frags);
    }

    @Override
    public int render(int texture, float[] matrix) {
        //设置窗口
        GLES20.glViewport(0, 0, width, height);
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
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        GLES20.glUniform1i(vTexture, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return texture;
    }
}
