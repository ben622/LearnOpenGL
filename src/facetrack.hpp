//
// Created by ben622 on 2020/7/17.
//

#ifndef LEARNOPENGL_FACETRACK_HPP
#define LEARNOPENGL_FACETRACK_HPP

#include <jni.h>
#include <string>
#include "FaceLandmarker.h"
#include <opencv2/opencv.hpp>
#include <android/log.h>

#define LOGS_ENABLED true
#define PRINT_TAG "tracking"
#define LOGI(FORMAT, ...) __android_log_print(ANDROID_LOG_VERBOSE,PRINT_TAG,FORMAT,__VA_ARGS__)
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,PRINT_TAG,FORMAT,__VA_ARGS__)
using namespace std;
using namespace cv;

class CascadeDetectorAdapter : public DetectionBasedTracker::IDetector {
public:
    CascadeDetectorAdapter(cv::Ptr<cv::CascadeClassifier> detector) :
            IDetector(),
            Detector(detector) {}

    void detect(const cv::Mat &Image, std::vector<cv::Rect> &objects) {
        Detector->detectMultiScale(Image, objects, scaleFactor, minNeighbours, 0, minObjSize,
                                   maxObjSize);
    }

    virtual ~CascadeDetectorAdapter() {
    }

private:
    CascadeDetectorAdapter();

    cv::Ptr<cv::CascadeClassifier> Detector;
};

class FaceTrack {
private:
    Ptr<DetectionBasedTracker> tracker;
    Ptr<seeta::FaceLandmarker> landmarker;
public:
    FaceTrack(const char *model, const char *seeta);

    void start();

    void stop();

    void detector(Mat src, vector<Rect2f> &rects, int cameraId);
};


#endif //LEARNOPENGL_FACETRACK_HPP
