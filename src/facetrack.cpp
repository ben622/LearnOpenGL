//
// Created by ben622 on 2020/7/17.
//

#include "facetrack.hpp"

FaceTrack::FaceTrack(const char *model, const char *seeta) {
    //创建跟踪器
    cv::Ptr<CascadeDetectorAdapter> mainDetector = makePtr<CascadeDetectorAdapter>(
            makePtr<CascadeClassifier>(model));
    cv::Ptr<CascadeDetectorAdapter> trackingDetector = makePtr<CascadeDetectorAdapter>(
            makePtr<CascadeClassifier>(model));
    //OpenCV使用的跟踪器
    DetectionBasedTracker::Parameters DetectorParams;
    tracker = makePtr<DetectionBasedTracker>(mainDetector, trackingDetector, DetectorParams);

    //seeta
    seeta::ModelSetting::Device device = seeta::ModelSetting::CPU;
    seeta::ModelSetting FL_model( seeta, device, 0);
    landmarker = makePtr<seeta::FaceLandmarker>(FL_model);
}

void FaceTrack::start() {
    tracker->run();
}

void FaceTrack::stop() {
    tracker->stop();
}

void FaceTrack::detector(Mat src, vector<Rect2f> &rects,int cameraId) {
    Mat gray;
    //NV21  ---> RGBA
    cvtColor(src, src, COLOR_YUV2RGBA_NV21);
    rotate(src, src, cameraId == 1 ? ROTATE_90_COUNTERCLOCKWISE : ROTATE_90_CLOCKWISE);
    //颜色转换
    cvtColor(src, gray, COLOR_RGBA2GRAY);
    //直方图均衡
    equalizeHist(gray, gray);
    //开始人脸追踪
    std::vector<Rect> faces;
    tracker->process(gray);
    tracker->getObjects(faces);

    if (faces.size()) {
        Rect face = faces[0];
        rects.push_back(Rect2f(face.x, face.y, face.width, face.height));
        //人脸关键点检测
        seeta::ImageData simage(src.cols, src.rows, 3);
        simage.data = src.data;
        //限定人脸位置
        SeetaRect bound = {face.x,face.y,face.width,face.height};
        vector<SeetaPointF> points = landmarker->mark(simage, bound);
        for (int i = 0; i < points.size(); ++i) {
            SeetaPointF point = points[i];
            rects.push_back(Rect2f(point.x, point.y, 0, 0));
        }
    }

}
