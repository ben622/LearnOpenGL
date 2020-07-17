package com.ben.android.learnopengl.face;

import android.graphics.PointF;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Face {
    /**
     * 人脸区域位置
     */
    private List<PointF> faces;

    /**
     * 人脸关键点位置 <br/>
     * 关键点位置信息取决于人间检测模型 <br/>
     * seetaface2支持5点 81点.
     */
    private List<PointF> marker;

    //检测后人脸区域宽高
    private int faceWidth;
    private int faceHeight;

    //检测图像宽高
    private int inputWidth;
    private int inputHeight;

    private Face(List<PointF> faces, List<PointF> marker, int faceWidth, int faceHeight, int inputWidth, int inputHeight) {
        this.faces = faces;
        this.marker = marker;
        this.faceWidth = faceWidth;
        this.faceHeight = faceHeight;
        this.inputWidth = inputWidth;
        this.inputHeight = inputHeight;
    }

    public static Face empty() {
        return new Face(new ArrayList<>(5), new ArrayList<>(5), 0, 0, 0, 0);
    }

    public void clearFaces() {
        this.faces.clear();
    }

    public void clearMarker() {
        this.marker.clear();

    }
    public void addFacePointF(PointF point) {
        this.faces.add(point);
    }

    public void addMarkerPointF(PointF point) {
        this.marker.add(point);
    }

    public List<PointF> getFaces() {
        return faces;
    }

    public List<PointF> getMarker() {
        return marker;
    }

    public int getFaceWidth() {
        return faceWidth;
    }

    public int getFaceHeight() {
        return faceHeight;
    }

    public int getInputWidth() {
        return inputWidth;
    }

    public int getInputHeight() {
        return inputHeight;
    }

    @Override
    public String toString() {
        return "Face{" +
                "faces=" + faces +
                ", marker=" + marker +
                ", faceWidth=" + faceWidth +
                ", faceHeight=" + faceHeight +
                ", inputWidth=" + inputWidth +
                ", inputHeight=" + inputHeight +
                '}';
    }
}
