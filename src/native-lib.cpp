#include <facetrack.hpp>

long getNativePtr(JNIEnv *env, jobject thiz) {
    jclass clazz = env->GetObjectClass(thiz);
    jfieldID ptrId = env->GetFieldID(clazz, "ptr", "J");
    return env->GetLongField(thiz, ptrId);
}

FaceTrack *getNativeFaceTracking(JNIEnv *env, jobject thiz) {
    long ptr = getNativePtr(env, thiz);
    if (ptr == 0) {
        if (LOGS_ENABLED) LOGE("%s", "ptr invalid");
        return 0;
    }
    return reinterpret_cast<FaceTrack *>(ptr);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_ben_android_learnopengl_face_FaceTracking_nativeInit(JNIEnv *env, jobject thiz,
                                                        jstring opencv_model, jstring seeta_model) {
    const char *model = env->GetStringUTFChars(opencv_model, JNI_FALSE);
    const char *seeta = env->GetStringUTFChars(seeta_model, JNI_FALSE);
    FaceTrack *faceTrack = new FaceTrack(model, seeta);
    env->ReleaseStringUTFChars(opencv_model, model);
    env->ReleaseStringUTFChars(seeta_model, seeta);
    return reinterpret_cast<jlong>(faceTrack);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ben_android_learnopengl_face_FaceTracking_nativeStart(JNIEnv *env, jobject thiz) {
    FaceTrack *faceTrack = getNativeFaceTracking(env, thiz);
    if (faceTrack) faceTrack->start();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ben_android_learnopengl_face_FaceTracking_nativeStop(JNIEnv *env, jobject thiz) {
    FaceTrack *faceTrack = getNativeFaceTracking(env, thiz);
    if (faceTrack) faceTrack->stop();
}

extern "C"
JNIEXPORT void Java_com_ben_android_learnopengl_face_FaceTracking_nativeDetector
(JNIEnv *env, jobject thiz,
                                                            jbyteArray jdata, jint w, jint h,
                                                            jint cameraId,jobject jface) {
    FaceTrack *faceTrack = getNativeFaceTracking(env, thiz);
    if (faceTrack) {
        jbyte *data = env->GetByteArrayElements(jdata, JNI_FALSE);
        Mat src(h + h / 2, w, CV_8UC1, data);
        vector<Rect2f> rects;
        faceTrack->detector(src, rects, cameraId);
        env->ReleaseByteArrayElements(jdata, data, 0);

        if (rects.size()) {

            Rect2f faceRect = rects[0];
            int faceWidth = faceRect.width;
            int faceHeight = faceRect.height;

            jclass faceClass = env->GetObjectClass(jface);
            jfieldID jInputWidthFieldId = env->GetFieldID(faceClass, "inputWidth", "I");
            jfieldID jInputHeightFieldId = env->GetFieldID(faceClass, "inputHeight", "I");
            jfieldID jFaceWidthFieldId = env->GetFieldID(faceClass, "faceWidth", "I");
            jfieldID jFaceHeightFieldId = env->GetFieldID(faceClass, "faceHeight", "I");
            jmethodID jAddFacePointFMethodId = env->GetMethodID(faceClass,"addFacePointF","(Landroid/graphics/PointF;)V");
            jmethodID jAddMarkerPointFMethodId = env->GetMethodID(faceClass,"addMarkerPointF","(Landroid/graphics/PointF;)V");
            jmethodID jClearFaceMethodId = env->GetMethodID(faceClass,"clearFaces","()V");
            jmethodID jClearMarkerMethodId = env->GetMethodID(faceClass,"clearMarker","()V");

            env->SetIntField(jface, jInputWidthFieldId, src.cols);
            env->SetIntField(jface, jInputHeightFieldId, src.rows);
            env->SetIntField(jface, jFaceWidthFieldId, faceWidth);
            env->SetIntField(jface, jFaceHeightFieldId, faceHeight);

            //clear
            env->CallVoidMethod(jface, jClearFaceMethodId);
            env->CallVoidMethod(jface, jClearMarkerMethodId);

            //reset
            jclass jPointFClass = env->FindClass("android/graphics/PointF");
            jmethodID costruct = env->GetMethodID(jPointFClass, "<init>", "(FF)V");
            jobject jFacePoint = env->NewObject(jPointFClass, costruct, faceRect.x, faceRect.y);
            env->CallVoidMethod(jface, jAddFacePointFMethodId, jFacePoint);

            for (int i = 1; i < rects.size(); ++i) {
                jmethodID costruct = env->GetMethodID(jPointFClass, "<init>", "(FF)V");
                jobject jMarkerPoint = env->NewObject(jPointFClass, costruct, rects[i].x, rects[i].y);
                env->CallVoidMethod(jface, jAddMarkerPointFMethodId, jMarkerPoint);
            }

        }else{
            if (LOGS_ENABLED) LOGI("%s","No face information detected");
        }

    }
}
