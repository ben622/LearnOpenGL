package com.ben.android.learnopengl.filter;

import android.content.Context;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public abstract class FrameBufferFilter  extends Filter {
    //着色器中变量的索引
   protected int vPosition;
   protected int vCoord;
   protected int vMatrix;
   protected int vTexture;

   protected int glProgram;
   protected FloatBuffer vertexBuffer;
   protected FloatBuffer textureBuffer;

    protected int[] frameBuffers;
    protected int[] frameBufferTextures;

    public FrameBufferFilter(Context context) {
        super(context);
    }
    @Override
    public void onSurfaceChanged(int width, int height) {
        super.onSurfaceChanged(width, height);
        if (frameBufferTextures != null) {
            //先移除
            GLES20.glDeleteTextures(frameBufferTextures.length, frameBufferTextures, 0);
            frameBufferTextures = null;
        }
        if (frameBuffers != null) {
            //先移除
            GLES20.glDeleteBuffers(frameBuffers.length, frameBuffers, 0);
            frameBuffers = null;

        }
        //创建FBO
        frameBuffers = new int[1];
        GLES20.glGenFramebuffers(frameBuffers.length, frameBuffers, 0);
        //创建FBO纹理
        frameBufferTextures = new int[1];
        GLES20.glGenTextures(frameBufferTextures.length, frameBufferTextures, 0);
        //配置
        for (int i = 0; i < frameBufferTextures.length; i++) {
            // opengl的操作 面向过程的操作
            //bind 就是绑定 ，表示后续的操作就是在这一个 纹理上进行
            // 后面的代码配置纹理，就是配置bind的这个纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,frameBufferTextures[i]);
            /**
             * 过滤参数
             *  当纹理被使用到一个比他大 或者比他小的形状上的时候 该如何处理
             */
            // 放大
            // GLES20.GL_LINEAR  : 使用纹理中坐标附近的若干个颜色，通过平均算法 进行放大
            // GLES20.GL_NEAREST : 使用纹理坐标最接近的一个颜色作为放大的要绘制的颜色
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);

            /*设置纹理环绕方向*/
            //纹理坐标 一般用st表示，其实就是x y
            //纹理坐标的范围是0-1。超出这一范围的坐标将被OpenGL根据GL_TEXTURE_WRAP参数的值进行处理
            //GL_TEXTURE_WRAP_S, GL_TEXTURE_WRAP_T 分别为x，y方向。
            //GL_REPEAT:平铺
            //GL_MIRRORED_REPEAT: 纹理坐标是奇数时使用镜像平铺
            //GL_CLAMP_TO_EDGE: 坐标超出部分被截取成0、1，边缘拉伸
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

            //解绑
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
        }


        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameBufferTextures[0]);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, frameBufferTextures[0], 0);

        //解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

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
        vMatrix = GLES20.glGetUniformLocation(glProgram, "vMatrix");
        vTexture = GLES20.glGetUniformLocation(glProgram, "vTexture");

        //创建数据缓冲器
        vertexBuffer = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.clear();
        float[] vers = {
                -1.0f, -1.0f,
                1.0f, -1.0f,
                -1.0f, 1.0f,
                1.0f, 1.0f};
        vertexBuffer.put(vers);

        textureBuffer = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureBuffer.clear();
        //纹理镜像，旋转.
        float[] frags = {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
        };
        textureBuffer.put(frags);
    }
}
