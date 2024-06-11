package kr.ac.duksung.mycol;

import androidx.lifecycle.ViewModel;

/**
 * 이 ViewModel은 얼굴 랜드마커 헬퍼 설정을 저장하는 데 사용됩니다.
 */
public class MainViewModel extends ViewModel {

    private int _delegate = FaceLandmarkerHelper.DELEGATE_GPU;
    private float _minFaceDetectionConfidence = FaceLandmarkerHelper.DEFAULT_FACE_DETECTION_CONFIDENCE;
    private float _minFaceTrackingConfidence = FaceLandmarkerHelper.DEFAULT_FACE_TRACKING_CONFIDENCE;
    private float _minFacePresenceConfidence = FaceLandmarkerHelper.DEFAULT_FACE_PRESENCE_CONFIDENCE;
    private int _maxFaces = FaceLandmarkerHelper.DEFAULT_NUM_FACES;

    // 현재 delegate 값을 반환
    public int getCurrentDelegate() {
        return _delegate;
    }

    // 현재 최소 얼굴 탐지 신뢰도를 반환
    public float getCurrentMinFaceDetectionConfidence() {
        return _minFaceDetectionConfidence;
    }

    // 현재 최소 얼굴 추적 신뢰도를 반환
    public float getCurrentMinFaceTrackingConfidence() {
        return _minFaceTrackingConfidence;
    }

    // 현재 최소 얼굴 존재 신뢰도를 반환
    public float getCurrentMinFacePresenceConfidence() {
        return _minFacePresenceConfidence;
    }

    // 현재 최대 얼굴 수를 반환
    public int getCurrentMaxFaces() {
        return _maxFaces;
    }

    // delegate 값을 설정
    public void setDelegate(int delegate) {
        _delegate = delegate;
    }

    // 최소 얼굴 탐지 신뢰도를 설정
    public void setMinFaceDetectionConfidence(float confidence) {
        _minFaceDetectionConfidence = confidence;
    }

    // 최소 얼굴 추적 신뢰도를 설정
    public void setMinFaceTrackingConfidence(float confidence) {
        _minFaceTrackingConfidence = confidence;
    }

    // 최소 얼굴 존재 신뢰도를 설정
    public void setMinFacePresenceConfidence(float confidence) {
        _minFacePresenceConfidence = confidence;
    }

    // 최대 얼굴 수를 설정
    public void setMaxFaces(int maxResults) {
        _maxFaces = maxResults;
    }
}

