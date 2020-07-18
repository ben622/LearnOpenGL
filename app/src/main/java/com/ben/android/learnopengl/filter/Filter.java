package com.ben.android.learnopengl.filter;

import android.content.Context;

public abstract class Filter {
    protected String TAG = this.getClass().getSimpleName();
    protected Context context;
    protected int width;
    protected int height;
    protected String vertexShader;
    protected String fragmentShader;


    public Filter(Context context) {
        this.context = context;
        vertexShader = getVertexShader(context);
        fragmentShader = getFragmentShader(context);
        initialize();
    }

    protected abstract String getVertexShader(Context context);

    protected abstract String getFragmentShader(Context context);

    public void onSurfaceChanged(int width,int height){
        this.width = width;
        this.height = height;
    }

    protected void initialize(){
        //Subclass implementation
    }
    protected void release() {
        //Subclass implementation
    }
    public int render(int texture, float[] matrix){
        //Subclass implementation
        return texture;
    }
}
